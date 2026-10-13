package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Service.ConfiguracionSitioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final ConfiguracionSitioService configuracionService;

    @ModelAttribute("configSitio")
    public com.Tienda.TechZone.Model.ConfiguracionSitio configuracionSitio() {
        try {
            return configuracionService.ObtenerConfiguracion();
        } catch (Exception e) {
            return com.Tienda.TechZone.Model.ConfiguracionSitio.builder().idConfig(1L).build();
        }
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_Administrador".equals(a.getAuthority()));
    }
}
