package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> ListarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario CrearUsuario(Usuario usuario) {
        validarUsuario(usuario, null);
        if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
            throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        if (usuario.getEstado() == null) {
            usuario.setEstado("activo");
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario ActualizarUsuario(Long id, Usuario usuario) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        validarUsuario(usuario, id);
        existente.setCorreo(usuario.getCorreo().trim());
        if (usuario.getPersona() != null) {
            existente.setPersona(usuario.getPersona());
        }
        if (usuario.getRol() != null) {
            existente.setRol(usuario.getRol());
        }
        if (usuario.getEstado() != null && !usuario.getEstado().isBlank()) {
            existente.setEstado(usuario.getEstado());
        }
        String nuevaClave = usuario.getPassword();
        if (nuevaClave != null && !nuevaClave.isBlank() && !nuevaClave.equals(existente.getPassword())) {
            if (nuevaClave.length() < 6) {
                throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
            }
            existente.setPassword(passwordEncoder.encode(nuevaClave));
        }
        return usuarioRepository.save(existente);
    }

    public void EliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario ObtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    private void validarUsuario(Usuario usuario, Long idActual) {
        if (usuario.getCorreo() == null || !usuario.getCorreo().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new RuntimeException("Ingresa un correo valido que contenga @");
        }
        Optional<Usuario> duplicado = usuarioRepository.findByCorreo(usuario.getCorreo().trim());
        if (duplicado.isPresent() && (idActual == null || !duplicado.get().getIdUsuario().equals(idActual))) {
            throw new RuntimeException("El correo ya esta registrado");
        }
        if (usuario.getRol() == null) {
            throw new RuntimeException("Selecciona un rol para el usuario");
        }
        if (usuario.getPersona() == null) {
            throw new RuntimeException("Selecciona una persona para el usuario");
        }
    }
}
