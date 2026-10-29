package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Persona;
import com.tienda.backend_techzone.Model.Usuario;
import com.tienda.backend_techzone.Repository.PersonaRepository;
import com.tienda.backend_techzone.Repository.TipoDocumentoRepository;
import com.tienda.backend_techzone.Repository.UbigeoRepository;
import com.tienda.backend_techzone.Repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
public class PerfilApiController {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UbigeoRepository ubigeoRepository;
    private final PasswordEncoder passwordEncoder;

    public PerfilApiController(UsuarioRepository usuarioRepository,
            PersonaRepository personaRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            UbigeoRepository ubigeoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.ubigeoRepository = ubigeoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> obtenerPerfil(@AuthenticationPrincipal UserDetails detalles) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesión"));
        }
        Usuario usuario = usuarioRepository.findByCorreo(detalles.getUsername()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body(Map.of("mensaje", "Usuario no encontrado"));
        }

        Persona p = usuario.getPersona();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("idUsuario", usuario.getIdUsuario());
        resp.put("correo", usuario.getCorreo());
        resp.put("rol", usuario.getRol() != null ? usuario.getRol().getNombre() : null);
        resp.put("idPersona", p != null ? p.getIdPersona() : null);
        resp.put("nombres", p != null ? p.getNombres() : null);
        resp.put("apellidoPaterno", p != null ? p.getApellidoPaterno() : null);
        resp.put("apellidoMaterno", p != null ? p.getApellidoMaterno() : null);
        resp.put("idTipoDocumento",
                p != null && p.getTipoDocumento() != null ? p.getTipoDocumento().getIdTipoDocumento() : null);
        resp.put("numeroDocumento", p != null ? p.getNumeroDocumento() : null);
        resp.put("telefono", p != null ? p.getTelefono() : null);
        resp.put("direccion", p != null ? p.getDireccion() : null);
        resp.put("referencia", p != null ? p.getReferencia() : null);
        resp.put("idUbigeo", p != null && p.getUbigeo() != null ? p.getUbigeo().getIdUbigeo() : null);
        resp.put("departamento", p != null && p.getUbigeo() != null ? p.getUbigeo().getDepartamento() : null);
        resp.put("provincia", p != null && p.getUbigeo() != null ? p.getUbigeo().getProvincia() : null);
        resp.put("distrito", p != null && p.getUbigeo() != null ? p.getUbigeo().getDistrito() : null);
        resp.put("fechaNacimiento",
                p != null && p.getFechaNacimiento() != null ? p.getFechaNacimiento().toString() : null);
        return ResponseEntity.ok(resp);
    }

    public record PerfilActualizarDto(
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            Long idTipoDocumento,
            String numeroDocumento,
            String telefono,
            String direccion,
            String referencia,
            Long idUbigeo,
            String fechaNacimiento,
            String passwordActual,
            String passwordNuevo) {
    }

    @PutMapping
    @Transactional
    public ResponseEntity<?> actualizarPerfil(@AuthenticationPrincipal UserDetails detalles,
            @RequestBody PerfilActualizarDto dto) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesión"));
        }
        Usuario usuario = usuarioRepository.findByCorreo(detalles.getUsername()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body(Map.of("mensaje", "Usuario no encontrado"));
        }

        Persona persona = usuario.getPersona();
        if (persona == null) {
            persona = new Persona();
            persona.setFechaRegistro(java.time.LocalDateTime.now());
            persona.setVigente(true);
        }

        if (dto.nombres() != null && !dto.nombres().isBlank()) {
            persona.setNombres(dto.nombres().trim().toUpperCase());
        }
        if (dto.apellidoPaterno() != null && !dto.apellidoPaterno().isBlank()) {
            persona.setApellidoPaterno(dto.apellidoPaterno().trim().toUpperCase());
        }
        if (dto.apellidoMaterno() != null && !dto.apellidoMaterno().isBlank()) {
            persona.setApellidoMaterno(dto.apellidoMaterno().trim().toUpperCase());
        }
        if (dto.telefono() != null && !dto.telefono().isBlank()) {
            String tel = dto.telefono().trim();
            if (!tel.matches("^9\\d{8}$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("mensaje", "El teléfono debe tener 9 dígitos y empezar con 9"));
            }
            persona.setTelefono(tel);
        }
        if (dto.numeroDocumento() != null && !dto.numeroDocumento().isBlank()) {
            String num = dto.numeroDocumento().trim();
            persona.setNumeroDocumento(num);
        }
        if (dto.idTipoDocumento() != null) {
            tipoDocumentoRepository.findById(dto.idTipoDocumento()).ifPresent(persona::setTipoDocumento);
        }
        if (dto.direccion() != null && !dto.direccion().isBlank()) {
            persona.setDireccion(dto.direccion().trim().toUpperCase());
        }
        if (dto.referencia() != null && !dto.referencia().isBlank()) {
            persona.setReferencia(dto.referencia().trim().toUpperCase());
        }
        if (dto.idUbigeo() != null) {
            ubigeoRepository.findById(dto.idUbigeo()).ifPresent(persona::setUbigeo);
        }
        if (dto.fechaNacimiento() != null && !dto.fechaNacimiento().isBlank()) {
            try {
                persona.setFechaNacimiento(LocalDate.parse(dto.fechaNacimiento().trim()));
            } catch (Exception ignored) {
            }
        }

        persona = personaRepository.save(persona);
        usuario.setPersona(persona);

        // Cambio de contraseña si se proporciona
        if (dto.passwordNuevo() != null && !dto.passwordNuevo().isBlank()) {
            String nuevo = dto.passwordNuevo().trim();
            if (nuevo.length() < 8) {
                return ResponseEntity.badRequest()
                        .body(Map.of("mensaje", "La nueva contraseña debe tener al menos 8 caracteres"));
            }
            if (!nuevo.chars().anyMatch(Character::isUpperCase)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("mensaje", "La nueva contraseña debe incluir al menos una letra mayúscula (A-Z)"));
            }
            if (!nuevo.chars().anyMatch(Character::isDigit)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("mensaje", "La nueva contraseña debe incluir al menos un número (0-9)"));
            }
            if (!nuevo.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`].*")) {
                return ResponseEntity.badRequest().body(Map.of("mensaje",
                        "La nueva contraseña debe incluir al menos un símbolo especial (ej. ! @ # $ % * -)"));
            }
            if (dto.passwordActual() == null || dto.passwordActual().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("mensaje", "Debes ingresar tu contraseña actual para cambiarla"));
            }
            if (!passwordEncoder.matches(dto.passwordActual(), usuario.getContrasena())) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "La contraseña actual es incorrecta"));
            }
            usuario.setContrasena(passwordEncoder.encode(dto.passwordNuevo().trim()));
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("mensaje", "Perfil actualizado exitosamente"));
    }
}
