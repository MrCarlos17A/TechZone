package com.Tienda.TechZone.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.Tienda.TechZone.Model.Persona;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.PersonaRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class DniService {

    @Value("${dni.api.url}")
    private String apiUrl;

    @Value("${dni.api.token}")
    private String apiToken;

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record DniData(String dni, String nombres, String apellidoPaterno, String apellidoMaterno) {
    }

    public record DniResponse(boolean ok, String mensaje, DniData datos) {
    }

    public record MisDatos(boolean ok, String dni, String nombres, String apellidoPaterno,
            String apellidoMaterno, String telefono, String correo,
            Integer idUbigeo, String departamento, String provincia,
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

    public DniResponse buscarDni(String correo, String dni) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

        if (usuario != null && usuario.getPersona() != null
                && dni.equals(usuario.getPersona().getNumeroDocumento())) {
            Persona p = usuario.getPersona();
            return new DniResponse(true, "Datos cargados desde tu registro",
                    new DniData(dni, p.getNombres(), p.getApellidoPaterno(), p.getApellidoMaterno()));
        }

        try {
            String url = apiUrl + "?numero=" + dni;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiToken)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return new DniResponse(false, "Error del servicio de DNI (HTTP " + response.statusCode() + ")", null);
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
                return new DniResponse(false, "DNI no encontrado", null);
            }

            if (usuario != null && usuario.getPersona() != null) {
                Persona p = usuario.getPersona();
                p.setNumeroDocumento(dni);
                p.setNombres(nombres);
                p.setApellidoPaterno(apPaterno);
                p.setApellidoMaterno(apMaterno);
                personaRepository.save(p);
            }

            return new DniResponse(true, null, new DniData(dni, nombres, apPaterno, apMaterno));
        } catch (Exception e) {
            log.error("Error consultando API DNI: {}", e.getMessage());
            return new DniResponse(false, "No se pudo consultar el DNI. Puedes escribirlo manualmente.", null);
        }
    }
}
