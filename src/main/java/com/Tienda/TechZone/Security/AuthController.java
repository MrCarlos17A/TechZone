package com.Tienda.TechZone.Security;

import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    @ModelAttribute("usuarioLogueado")
    public Optional<Usuario> usuarioLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return usuarioRepository.findByCorreo(auth.getName());
        }
        return Optional.empty();
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin() {
        return hasRole("Administrador");
    }

    @ModelAttribute("esTrabajador")
    public boolean esTrabajador() {
        return hasRole("Trabajador");
    }

    @ModelAttribute("esAdminOTrabajador")
    public boolean esAdminOTrabajador() {
        return hasRole("Administrador") || hasRole("Trabajador");
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_" + role));
    }
}
