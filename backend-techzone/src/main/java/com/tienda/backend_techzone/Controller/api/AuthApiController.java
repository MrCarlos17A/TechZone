package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Persona;
import com.tienda.backend_techzone.Model.RegistroAcceso;
import com.tienda.backend_techzone.Model.Rol;
import com.tienda.backend_techzone.Model.Usuario;
import com.tienda.backend_techzone.Repository.*;
import com.tienda.backend_techzone.Security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UbigeoRepository ubigeoRepository;
    private final RolRepository rolRepository;
    private final RegistroAccesoRepository registroAccesoRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthApiController(AuthenticationManager authenticationManager,
                             JwtService jwtService,
                             UsuarioRepository usuarioRepository,
                             PersonaRepository personaRepository,
                             TipoDocumentoRepository tipoDocumentoRepository,
                             UbigeoRepository ubigeoRepository,
                             RolRepository rolRepository,
                             RegistroAccesoRepository registroAccesoRepository,
                             PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.ubigeoRepository = ubigeoRepository;
        this.rolRepository = rolRepository;
        this.registroAccesoRepository = registroAccesoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record SolicitudLogin(String username, String correo, String password) {
        public String identificador() {
            if (username != null && !username.isBlank()) return username.trim().toLowerCase();
            if (correo != null && !correo.isBlank()) return correo.trim().toLowerCase();
            return null;
        }
    }

    public record SolicitudRegistro(
            String nombres,
            String apellidos,
            String apellidoPaterno,
            String apellidoMaterno,
            String correo,
            String telefono,
            String password,
            Long idTipoDocumento,
            String numeroDocumento,
            String fechaNacimiento,
            String direccion,
            String referencia,
            Long idUbigeo
    ) {
        public String obtenerApellidoPaterno() {
            if (apellidos != null && !apellidos.isBlank()) return apellidos.trim();
            if (apellidoPaterno != null && !apellidoPaterno.isBlank()) return apellidoPaterno.trim();
            return null;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody SolicitudLogin solicitud,
            HttpServletRequest request) {

        String identificador = solicitud != null ? solicitud.identificador() : null;
        if (identificador == null || solicitud.password() == null || solicitud.password().isBlank()) {
            return respuesta(401, "Credenciales inválidas. Ingresa tu correo y contraseña.");
        }

        String correoLimpio = identificador;
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        String agente = request.getHeader("User-Agent");
        if (agente != null && agente.length() > 255) {
            agente = agente.substring(0, 255);
        }

        Usuario usuario = usuarioRepository.findByCorreo(correoLimpio).orElse(null);

        if (usuario == null) {
            registrarAcceso(null, correoLimpio, ip, agente, false, "usuario_inexistente");
            return respuesta(401, "Correo o contraseña incorrectos");
        }

        if (usuario.estaBloqueado()) {
            registrarAcceso(usuario, correoLimpio, ip, agente, false, "cuenta_bloqueada");
            return respuesta(423, "Cuenta bloqueada temporalmente por 15 minutos debido a 5 intentos fallidos consecutivos.");
        } else if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isBefore(LocalDateTime.now())) {
            usuario.setBloqueadoHasta(null);
            usuario.setIntentosFallidos(0);
            usuarioRepository.save(usuario);
        }

        String estadoUsuario = usuario.getEstado() != null ? usuario.getEstado().trim() : "activo";
        if (!"activo".equalsIgnoreCase(estadoUsuario)) {
            registrarAcceso(usuario, correoLimpio, ip, agente, false, "cuenta_inactiva");
            return ResponseEntity.status(403).body(Map.of("mensaje", "Tu cuenta se encuentra inactiva o suspendida. Contacta a administración."));
        }

        if (usuario.getRol() != null && Boolean.FALSE.equals(usuario.getRol().getVigente())) {
            registrarAcceso(usuario, correoLimpio, ip, agente, false, "rol_inactivo");
            return ResponseEntity.status(403).body(Map.of("mensaje", "El rol asignado a tu cuenta no se encuentra vigente."));
        }

        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correoLimpio, solicitud.password()));

            usuario.setFechaUltimoAcceso(LocalDateTime.now());
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);

            // Auto-upgrade transparente de claves en texto plano a BCrypt
            if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2")) {
                String hashActualizado = passwordEncoder.encode(usuario.getPassword());
                usuario.setPassword(hashActualizado);
                usuario.setContrasena(hashActualizado);
            }
            usuarioRepository.save(usuario);

            registrarAcceso(usuario, correoLimpio, ip, agente, true, "ok");

            List<String> roles = autenticacion.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = jwtService.generarToken(usuario.getCorreo(), roles, usuario.getIdUsuario());

            boolean esAdmin = usuario.getRol() != null && (
                    Boolean.TRUE.equals(usuario.getRol().getEsAdministrativo())
                    || (usuario.getRol().getNombre() != null && usuario.getRol().getNombre().toUpperCase().contains("ADMIN"))
                    || Long.valueOf(1L).equals(usuario.getRol().getIdRol())
            );
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("token", token);
            cuerpo.put("tipo", "Bearer");
            cuerpo.put("idUsuario", usuario.getIdUsuario());
            cuerpo.put("correo", usuario.getCorreo());
            cuerpo.put("roles", roles);
            cuerpo.put("esStaff", esAdmin);
            cuerpo.put("esAdministrativo", esAdmin);
            cuerpo.put("permisos", usuario.getRol() != null ? usuario.getRol().getPermisos() : null);
            cuerpo.put("nombre", nombreCompleto(usuario));
            return ResponseEntity.ok(cuerpo);

        } catch (AuthenticationException e) {
            int intentos = (usuario.getIntentosFallidos() != null ? usuario.getIntentosFallidos() : 0) + 1;
            usuario.setIntentosFallidos(intentos);
            if (intentos >= 5) {
                usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
            }
            usuarioRepository.save(usuario);

            registrarAcceso(usuario, correoLimpio, ip, agente, false, intentos >= 5 ? "cuenta_bloqueada" : "clave_incorrecta");

            if (intentos >= 5) {
                return respuesta(423, "Cuenta bloqueada temporalmente por 15 minutos debido a 5 intentos fallidos consecutivos.");
            }
            return respuesta(401, "Correo o contraseña incorrectos. Intentos restantes: " + (5 - intentos));
        }
    }

    @PostMapping("/registro")
    @Transactional
    public ResponseEntity<Map<String, Object>> registro(@RequestBody SolicitudRegistro req) {
        if (req == null) {
            return respuesta(400, "Los datos de registro son requeridos");
        }

        // 1. Nombres y Apellidos
        if (req.nombres() == null || req.nombres().trim().length() < 2) {
            return respuesta(400, "Los nombres son obligatorios y deben tener al menos 2 caracteres");
        }
        String apPat = req.obtenerApellidoPaterno();
        if (apPat == null || apPat.length() < 2) {
            return respuesta(400, "El apellido paterno es obligatorio y debe tener al menos 2 caracteres");
        }
        if (req.apellidoMaterno() == null || req.apellidoMaterno().trim().length() < 2) {
            return respuesta(400, "El apellido materno es obligatorio y debe tener al menos 2 caracteres");
        }

        // 2. Correo electrónico y unicidad
        if (req.correo() == null || !req.correo().trim().toLowerCase().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return respuesta(400, "Ingresa un formato de correo electrónico válido");
        }
        String correoLimpio = req.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correoLimpio)) {
            return respuesta(400, "El correo electrónico ya se encuentra registrado con otra cuenta");
        }

        // 3. Contraseña Segura (mín 8 car, 1 mayúscula, 1 número, 1 símbolo)
        if (req.password() == null || req.password().length() < 8) {
            return respuesta(400, "La contraseña debe tener al menos 8 caracteres");
        }
        if (!req.password().chars().anyMatch(Character::isUpperCase)) {
            return respuesta(400, "La contraseña debe incluir al menos una letra mayúscula (A-Z)");
        }
        if (!req.password().chars().anyMatch(Character::isDigit)) {
            return respuesta(400, "La contraseña debe incluir al menos un número (0-9)");
        }
        if (!req.password().matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`].*")) {
            return respuesta(400, "La contraseña debe incluir al menos un símbolo especial (ej. ! @ # $ % * -)");
        }

        // 4. Teléfono (obligatorio para contacto y notificaciones, 9 dígitos empezando en 9)
        String telLimpio = null;
        if (req.telefono() != null && !req.telefono().isBlank()) {
            telLimpio = req.telefono().replaceAll("\\D", "").trim();
            if (!telLimpio.matches("^9\\d{8}$")) {
                return respuesta(400, "El teléfono debe tener 9 dígitos y empezar con 9 (ej. 987654321)");
            }
            if (personaRepository.existsByTelefono(telLimpio)) {
                return respuesta(400, "El número de teléfono ya está registrado por otro usuario");
            }
        }

        // 5. Documento de Identidad (DNI: 8 dígitos obligatorio y único)
        if (req.numeroDocumento() == null || req.numeroDocumento().isBlank()) {
            return respuesta(400, "El número de documento (DNI) es obligatorio");
        }
        String numDocLimpio = req.numeroDocumento().replaceAll("\\D", "").trim();
        if (!numDocLimpio.matches("^\\d{8}$")) {
            return respuesta(400, "El DNI debe tener exactamente 8 dígitos numéricos");
        }
        if (personaRepository.existsByNumeroDocumento(numDocLimpio)) {
            return respuesta(400, "El número de documento ya está registrado por otro usuario");
        }

        // 6. Dirección exacta (Obligatoria, mínimo 10 caracteres)
        if (req.direccion() == null || req.direccion().trim().length() < 10) {
            return respuesta(400, "La dirección exacta es obligatoria y debe tener al menos 10 caracteres (ej. Av. Javier Prado 1234)");
        }
        String dirLimpia = req.direccion().trim();

        Rol rolUsuario = rolRepository.findByNombre("Usuario")
                .orElseGet(() -> rolRepository.findAll().stream().findFirst().orElse(null));

        Persona persona = new Persona();
        persona.setFechaRegistro(LocalDateTime.now());
        persona.setVigente(true);
        persona.setNombres(req.nombres().trim().toUpperCase());
        persona.setApellidoPaterno(apPat.toUpperCase());
        if (req.apellidoMaterno() != null && !req.apellidoMaterno().isBlank()) {
            persona.setApellidoMaterno(req.apellidoMaterno().trim().toUpperCase());
        }
        if (telLimpio != null) {
            persona.setTelefono(telLimpio);
        }
        if (dirLimpia != null) {
            persona.setDireccion(dirLimpia.toUpperCase());
        }
        if (req.referencia() != null && !req.referencia().isBlank()) {
            persona.setReferencia(req.referencia().trim().toUpperCase());
        }
        if (req.idTipoDocumento() != null) {
            tipoDocumentoRepository.findById(req.idTipoDocumento()).ifPresent(persona::setTipoDocumento);
        }
        if (numDocLimpio != null) {
            persona.setNumeroDocumento(numDocLimpio);
        }
        if (req.idUbigeo() != null) {
            ubigeoRepository.findById(req.idUbigeo()).ifPresent(persona::setUbigeo);
        }
        LocalDate fNac = EmpleadoAdminApiController.parsearFechaSegura(req.fechaNacimiento());
        if (fNac != null) {
            persona.setFechaNacimiento(fNac);
        }
        persona = personaRepository.save(persona);

        Usuario usuario = new Usuario();
        usuario.setCorreo(correoLimpio);
        String claveCodificada = passwordEncoder.encode(req.password());
        usuario.setPassword(claveCodificada);
        usuario.setContrasena(claveCodificada);
        usuario.setRol(rolUsuario);
        usuario.setPersona(persona);
        usuario.setEstado("activo");
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setIntentosFallidos(0);
        usuario = usuarioRepository.save(usuario);

        List<String> roles = List.of(rolUsuario != null ? "ROLE_" + rolUsuario.getNombre() : "ROLE_Usuario");
        String token = jwtService.generarToken(usuario.getCorreo(), roles, usuario.getIdUsuario());

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("token", token);
        cuerpo.put("tipo", "Bearer");
        cuerpo.put("idUsuario", usuario.getIdUsuario());
        cuerpo.put("correo", usuario.getCorreo());
        cuerpo.put("roles", roles);
        cuerpo.put("esStaff", false);
        cuerpo.put("esAdministrativo", false);
        cuerpo.put("permisos", "{\"productos\":[\"ver\"]}");
        cuerpo.put("nombre", nombreCompleto(usuario));
        return ResponseEntity.ok(cuerpo);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> yo(@AuthenticationPrincipal UserDetails detalles) {
        if (detalles == null) {
            return respuesta(401, "Debes iniciar sesion para continuar");
        }
        Usuario usuario = usuarioRepository.findByCorreo(detalles.getUsername()).orElse(null);
        if (usuario == null) {
            return respuesta(401, "Usuario no encontrado");
        }

        boolean esAdmin = usuario.getRol() != null && (
                Boolean.TRUE.equals(usuario.getRol().getEsAdministrativo())
                || (usuario.getRol().getNombre() != null && usuario.getRol().getNombre().toUpperCase().contains("ADMIN"))
                || Long.valueOf(1L).equals(usuario.getRol().getIdRol())
        );
        List<String> roles = (detalles.getAuthorities() != null)
                ? detalles.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
                : List.of();
        String permisos = (usuario.getRol() != null && usuario.getRol().getPermisos() != null)
                ? usuario.getRol().getPermisos()
                : "{}";

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("idUsuario", usuario.getIdUsuario());
        cuerpo.put("correo", usuario.getCorreo());
        cuerpo.put("roles", roles);
        cuerpo.put("esStaff", esAdmin);
        cuerpo.put("esAdministrativo", esAdmin);
        cuerpo.put("permisos", permisos);
        cuerpo.put("nombre", nombreCompleto(usuario));
        return ResponseEntity.ok(cuerpo);
    }

    private void registrarAcceso(Usuario usuario, String correo, String ip, String agente, boolean exitoso, String motivo) {
        try {
            RegistroAcceso reg = new RegistroAcceso(usuario, correo, ip, agente, exitoso, motivo);
            registroAccesoRepository.save(reg);
        } catch (Exception ignored) {
        }
    }

    private String nombreCompleto(Usuario usuario) {
        if (usuario.getPersona() != null) {
            String completo = usuario.getPersona().getNombreCompleto();
            if (completo != null && !completo.isBlank()) {
                return completo;
            }
        }
        return usuario.getCorreo();
    }

    private ResponseEntity<Map<String, Object>> respuesta(int estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
