package com.tienda.backend_techzone.Security;

import com.tienda.backend_techzone.Model.Usuario;
import com.tienda.backend_techzone.Repository.UsuarioRepository;
import com.tienda.backend_techzone.Repository.MedioPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class SecurityModelAdvice {

    private final UsuarioRepository usuarioRepository;
    private final MedioPagoRepository medioPagoRepository;

    @ModelAttribute("usuarioLogueado")
    public Optional<Usuario> usuarioLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return usuarioRepository.findByCorreo(auth.getName());
        }
        return Optional.empty();
    }

    @ModelAttribute("mediosPago")
    public java.util.List<com.tienda.backend_techzone.Model.MedioPago> mediosPago() {
        return medioPagoRepository.findAllByVigenteTrue();
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin() {
        return hasRole("Administrador") || hasRole("ADMINISTRADOR");
    }

    @ModelAttribute("esTrabajador")
    public boolean esTrabajador() {
        return hasRole("Trabajador") || hasRole("TRABAJADOR") || hasRole("EMPLEADO") || hasRole("COLABORADOR") || hasRole("Empleado");
    }

    @ModelAttribute("esAdminOTrabajador")
    public boolean esAdminOTrabajador() {
        return esAdmin() || esTrabajador();
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equalsIgnoreCase("ROLE_" + role));
    }
}
