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
        if (usuario.getEstadoUsuario() == null) {
            usuario.setEstadoUsuario(true);
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario ActualizarUsuario(Long id, Usuario usuario) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        validarUsuario(usuario, id);
        existente.setNombres(usuario.getNombres().trim());
        existente.setApellidos(usuario.getApellidos().trim());
        existente.setCorreo(usuario.getCorreo().trim());
        existente.setTelefono(usuario.getTelefono());
        if (usuario.getEstadoUsuario() != null) {
            existente.setEstadoUsuario(usuario.getEstadoUsuario());
        }
        existente.setRol(usuario.getRol());
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
        if (usuario.getNombres() == null || usuario.getNombres().isBlank()) {
            throw new RuntimeException("Los nombres son obligatorios");
        }
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            throw new RuntimeException("Los apellidos son obligatorios");
        }
        if (usuario.getCorreo() == null || !usuario.getCorreo().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new RuntimeException("Ingresa un correo valido que contenga @");
        }
        Optional<Usuario> duplicado = usuarioRepository.findByCorreo(usuario.getCorreo().trim());
        if (duplicado.isPresent() && (idActual == null || !duplicado.get().getIdUsuario().equals(idActual))) {
            throw new RuntimeException("El correo ya esta registrado");
        }
        if (usuario.getTelefono() != null && !usuario.getTelefono().isBlank()
                && !usuario.getTelefono().matches("[0-9]{9}")) {
            throw new RuntimeException("El telefono debe tener 9 digitos");
        }
        if (usuario.getRol() == null) {
            throw new RuntimeException("Selecciona un rol para el usuario");
        }
    }
}
