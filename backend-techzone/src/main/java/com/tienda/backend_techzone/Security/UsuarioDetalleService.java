package com.tienda.backend_techzone.Security;

import com.tienda.backend_techzone.Model.Usuario;
import com.tienda.backend_techzone.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Carga el usuario de la base de datos para Spring Security.
 * El "usuario" es el correo; el rol aporta la autoridad ROLE_ + nombre.
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetalleService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() == null || !Boolean.TRUE.equals(usuario.getRol().getVigente())) {
            throw new UsernameNotFoundException("Rol inactivo");
        }
        if (usuario.getEstado() == null || !"activo".equalsIgnoreCase(usuario.getEstado())) {
            throw new UsernameNotFoundException("Cuenta inactiva");
        }
        if (usuario.estaBloqueado()) {
            throw new LockedException("Cuenta bloqueada temporalmente por intentos fallidos");
        }

        List<SimpleGrantedAuthority> autoridades = List.of(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getContrasena() == null ? "" : usuario.getContrasena())
                .authorities(autoridades)
                .disabled(false)
                .build();
    }
}
