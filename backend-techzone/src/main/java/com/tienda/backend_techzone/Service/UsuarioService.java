package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Rol;
import com.tienda.backend_techzone.Model.Usuario;
import com.tienda.backend_techzone.Repository.RolRepository;
import com.tienda.backend_techzone.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final DatabaseBootstrapService databaseBootstrapService;

    public List<Usuario> ListarUsuarios() {
        databaseBootstrapService.sincronizarTodo();
        return usuarioRepository.findAllConRelaciones();
    }

    public List<Usuario> listarUsuarios() {
        return ListarUsuarios();
    }

    public Usuario crearUsuario(Usuario usuario) {
        return CrearUsuario(usuario);
    }

    public Usuario actualizarUsuario(Long id, Usuario usuario) {
        return ActualizarUsuario(id, usuario);
    }

    public static void validarContrasenaSegura(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!password.chars().anyMatch(Character::isUpperCase)) {
            throw new RuntimeException("La contraseña debe incluir al menos una letra mayúscula (A-Z)");
        }
        if (!password.chars().anyMatch(Character::isDigit)) {
            throw new RuntimeException("La contraseña debe incluir al menos un número (0-9)");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`].*")) {
            throw new RuntimeException("La contraseña debe incluir al menos un carácter especial (ej. ! @ # $ % * -)");
        }
    }

    public Usuario CrearUsuario(Usuario usuario) {
        validarUsuario(usuario, null);
        validarRolAsignable(usuario.getRol());
        validarContrasenaSegura(usuario.getPassword());
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        if (usuario.getEstado() == null) {
            usuario.setEstado("activo");
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario ActualizarUsuario(Long id, Usuario usuario) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        if (usuario.getPersona() == null && existente.getPersona() != null) {
            usuario.setPersona(existente.getPersona());
        }
        validarUsuario(usuario, id);
        // Solo se valida si el rol cambio: los roles "Usuario"/"Cliente" asignados
        // por el sistema no deben bloquear una edicion o deshabilitacion comun.
        if (usuario.getRol() != null && usuario.getRol().getIdRol() != null
                && (existente.getRol() == null || existente.getRol().getIdRol() == null
                        || !existente.getRol().getIdRol().equals(usuario.getRol().getIdRol()))) {
            validarRolAsignable(usuario.getRol());
        }
        existente.setCorreo(usuario.getCorreo().trim());
        if (usuario.getPersona() != null) {
            existente.setPersona(usuario.getPersona());
        }
        if (usuario.getRol() != null) {
            existente.setRol(usuario.getRol());
        }
        if (usuario.getEstado() != null && !usuario.getEstado().isBlank()) {
            existente.setEstado(usuario.getEstado());
            if ("activo".equalsIgnoreCase(usuario.getEstado())) {
                existente.setIntentosFallidos(0);
                existente.setBloqueadoHasta(null);
            }
        }
        String nuevaClave = usuario.getPassword();
        if (nuevaClave != null && !nuevaClave.isBlank() && !nuevaClave.equals(existente.getPassword())) {
            validarContrasenaSegura(nuevaClave);
            String encoded = passwordEncoder.encode(nuevaClave);
            existente.setPassword(encoded);
            existente.setContrasena(encoded);
            existente.setIntentosFallidos(0);
            existente.setBloqueadoHasta(null);
        }
        return usuarioRepository.save(existente);
    }

    public void EliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        usuario.setEstado("inactivo");
        usuarioRepository.save(usuario);
    }

    public Usuario ObtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    private void validarUsuario(Usuario usuario, Long idActual) {
        if (usuario.getCorreo() == null || !usuario.getCorreo().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new RuntimeException("Ingresa un correo valido que contenga @");
        }
        if (usuario.getCorreo().trim().length() > 100) {
            throw new RuntimeException("El correo no puede superar 100 caracteres");
        }
        Optional<Usuario> duplicado = usuarioRepository.findByCorreo(usuario.getCorreo().trim());
        if (duplicado.isPresent() && (idActual == null || !duplicado.get().getIdUsuario().equals(idActual))) {
            throw new RuntimeException("El correo ya esta registrado");
        }
        if (usuario.getRol() == null) {
            throw new RuntimeException("Selecciona un rol para el usuario");
        }
        if (usuario.getPersona() == null) {
            if (idActual != null) {
                Usuario enBd = usuarioRepository.findById(idActual).orElse(null);
                if (enBd != null && enBd.getPersona() != null) {
                    usuario.setPersona(enBd.getPersona());
                } else {
                    throw new RuntimeException("Selecciona una persona para el usuario");
                }
            } else {
                throw new RuntimeException("Selecciona una persona para el usuario");
            }
        }
        if (usuario.getPersona().getIdPersona() != null) {
            Optional<Usuario> conCuenta = usuarioRepository
                    .findByPersona_IdPersona(usuario.getPersona().getIdPersona());
            if (conCuenta.isPresent() && (idActual == null
                    || !conCuenta.get().getIdUsuario().equals(idActual))) {
                throw new RuntimeException("Esa persona ya tiene una cuenta de acceso");
            }
        }
        if (usuario.getEstado() != null && !usuario.getEstado().isBlank()) {
            String est = usuario.getEstado().trim().toLowerCase();
            if (!est.equals("activo") && !est.equals("inactivo")) {
                throw new RuntimeException("El estado debe ser 'activo' o 'inactivo'");
            }
            usuario.setEstado(est);
        }
    }

    /**
     * El rol de un usuario solo puede ser Administrador o Trabajador. Los roles
     * "Usuario" (se asigna al registrarse) y "Cliente" (se asigna al comprar por
     * primera vez) los otorga unicamente el sistema.
     */
    private void validarRolAsignable(Rol rol) {
        if (rol == null || rol.getIdRol() == null) {
            throw new RuntimeException("Selecciona un rol para el usuario");
        }
        Rol completo = rolRepository.findById(rol.getIdRol())
                .orElseThrow(() -> new RuntimeException("El rol seleccionado no existe"));
        if (completo.getVigente() != null && !completo.getVigente()) {
            throw new RuntimeException("El rol seleccionado está inactivo en el sistema");
        }
    }
}
