package com.Tienda.TechZone.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.Tienda.TechZone.Model.Persona;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.PersonaRepository;
import com.Tienda.TechZone.Repository.TipoDocumentoRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DniService {

    private static final int MAX_CACHE = 500;

    @Value("${dni.api.url}")
    private String apiUrl;

    @Value("${dni.api.token}")
    private String apiToken;

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Caché de respuestas de la API externa (evita repetir llamadas pagadas)
    private final Map<String, DocumentoData> cacheApi = new ConcurrentHashMap<>();

    public record DocumentoData(String numero, String nombres, String apellidoPaterno, String apellidoMaterno,
            String telefono, String fechaNacimiento, Long idUbigeo, String direccion, String referencia) {
    }

    public record CuentaExistente(String correo, String rol, String estado) {
    }

    public record DocumentoResponse(boolean ok, String mensaje, String origen,
            Long idPersona, boolean yaRegistrado, DocumentoData datos, CuentaExistente cuenta) {
    }

    public record MisDatos(boolean ok, String dni, String nombres, String apellidoPaterno,
            String apellidoMaterno, String telefono, String correo,
            Long idUbigeo, String departamento, String provincia,
            String distrito, String direccionExacta, String referencia) {
    }

    public MisDatos obtenerMisDatos(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null || usuario.getPersona() == null) {
            return new MisDatos(false, null, null, null, null, null, correo,
                    null, null, null, null, null, null);
        }
        Persona p = usuario.getPersona();
        return new MisDatos(true,
                p.getNumeroDocumento(),
                p.getNombres(),
                p.getApellidoPaterno(),
                p.getApellidoMaterno(),
                p.getTelefono(),
                usuario.getCorreo(),
                p.getUbigeo() != null ? p.getUbigeo().getIdUbigeo() : null,
                p.getUbigeo() != null ? p.getUbigeo().getDepartamento() : null,
                p.getUbigeo() != null ? p.getUbigeo().getProvincia() : null,
                p.getUbigeo() != null ? p.getUbigeo().getDistrito() : null,
                p.getDireccion(),
                p.getReferencia());
    }

    /**
     * Consulta de documento SIN escribir en la base.
     * 1) Primero busca en la BD local (no gasta la API externa).
     * 2) Solo si no existe y el tipo es DNI, consulta la API externa.
     */
    public DocumentoResponse consultarDocumento(Long idTipoDocumento, String numero) {
        if (numero == null || numero.isBlank()) {
            return new DocumentoResponse(false, "El numero de documento es obligatorio", null, null, false, null, null);
        }
        String num = numero.trim();

        String nombreTipo = null;
        if (idTipoDocumento != null) {
            nombreTipo = tipoDocumentoRepository.findById(idTipoDocumento)
                    .map(td -> td.getNombre() != null ? td.getNombre().toUpperCase() : null)
                    .orElse(null);
        }

        // ---- 1) LOCAL ----
        Persona local = null;
        try {
            if (idTipoDocumento != null) {
                local = personaRepository.findFirstByTipoDocumentoAndNumeroDocumento(idTipoDocumento, num)
                        .orElse(null);
            }
            if (local == null) {
                local = personaRepository.findFirstByNumeroDocumento(num).orElse(null);
            }
        } catch (RuntimeException e) {
            log.warn("Error buscando documento en local: {}", e.getMessage());
        }

        if (local != null) {
            CuentaExistente cuenta = usuarioRepository.findFirstByPersona(local)
                    .map(u -> new CuentaExistente(
                            u.getCorreo(),
                            u.getRol() != null ? u.getRol().getNombre() : null,
                            u.getEstado()))
                    .orElse(null);
            boolean conCuenta = cuenta != null;
            return new DocumentoResponse(true,
                    conCuenta ? "El documento ya esta registrado" : "Datos encontrados en el sistema",
                    "local", local.getIdPersona(), conCuenta,
                    new DocumentoData(num,
                            local.getNombres(),
                            local.getApellidoPaterno(),
                            local.getApellidoMaterno(),
                            local.getTelefono(),
                            local.getFechaNacimiento() != null ? local.getFechaNacimiento().toString() : null,
                            local.getUbigeo() != null ? local.getUbigeo().getIdUbigeo() : null,
                            local.getDireccion(),
                            local.getReferencia()),
                    cuenta);
        }

        // ---- 2) API EXTERNA (solo DNI de 8 digitos) ----
        boolean esDni = nombreTipo == null || nombreTipo.contains("DNI");
        if (!esDni || !num.matches("\\d{8}")) {
            return new DocumentoResponse(false,
                    "No hay datos locales y este tipo de documento no admite consulta automatica",
                    null, null, false, null, null);
        }

        DocumentoData cacheado = cacheApi.get(num);
        if (cacheado != null) {
            return new DocumentoResponse(true, null, "api", null, false, cacheado, null);
        }

        try {
            String url = apiUrl + "?numero=" + num;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiToken)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return new DocumentoResponse(false,
                        "Error del servicio de DNI (HTTP " + response.statusCode() + ")", null, null, false, null, null);
            }

            JsonNode json = objectMapper.readTree(response.body());
            String nombres = null;
            String apPaterno = null;
            String apMaterno = null;

            if (json.has("nombres")) {
                nombres = json.path("nombres").asString("");
                apPaterno = json.path("apellidoPaterno").asString("");
                apMaterno = json.path("apellidoMaterno").asString("");
            } else if (json.has("success") && json.get("success").asBoolean()) {
                JsonNode data = json.get("data");
                if (data != null && data.has("nombres")) {
                    nombres = data.path("nombres").asString("");
                    apPaterno = data.path("apellidoPaterno").asString("");
                    apMaterno = data.path("apellidoMaterno").asString("");
                }
            }

            if (nombres == null || nombres.isBlank()) {
                return new DocumentoResponse(false, "Documento no encontrado", null, null, false, null, null);
            }

            DocumentoData datos = new DocumentoData(num, nombres, apPaterno, apMaterno,
                    null, null, null, null, null);

            if (cacheApi.size() >= MAX_CACHE) {
                cacheApi.clear();
            }
            cacheApi.put(num, datos);

            return new DocumentoResponse(true, null, "api", null, false, datos, null);
        } catch (Exception e) {
            log.error("Error consultando API DNI: {}", e.getMessage());
            return new DocumentoResponse(false,
                    "No se pudo consultar el documento. Puedes escribirlo manualmente.", null, null, false, null, null);
        }
    }
}
