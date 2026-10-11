package com.Tienda.TechZone.Security;

import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UsuarioDetalleService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioDetalleService.class);
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        log.info("=== BUSCANDO USUARIO: {} ===", correo);

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> {
                    log.error("USUARIO NO ENCONTRADO: {}", correo);
                    return new UsernameNotFoundException("Usuario no encontrado: " + correo);
                });

        log.info("Usuario encontrado: {} | Estado: {} | Rol: {}",
                usuario.getCorreo(), usuario.getEstadoUsuario(), usuario.getRol().getNombre());

        if (!usuario.getEstadoUsuario()) {
            log.error("USUARIO DESHABILITADO: {}", correo);
            throw new UsernameNotFoundException("Usuario deshabilitado: " + correo);
        }

        if (usuario.getRol() == null || Boolean.FALSE.equals(usuario.getRol().getEstadoRol())) {
            log.error("ROL INACTIVO para el usuario: {}", correo);
            throw new UsernameNotFoundException("Rol deshabilitado: " + correo);
        }

        log.info("Credenciales OK para: {} | Rol: {}", usuario.getCorreo(), usuario.getRol().getNombre());

        return new User(
                usuario.getCorreo(),
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()))
        );
    }
}
