package com.tienda.backend_techzone.Controller;

import com.tienda.backend_techzone.Model.ConfiguracionSitio;
import com.tienda.backend_techzone.Service.ConfiguracionSitioService;
import com.tienda.backend_techzone.Service.SupabaseStorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracion")
public class ConfiguracionController {

    private final ConfiguracionSitioService configuracionService;
    private final SupabaseStorageService supabaseStorageService;

    public ConfiguracionController(ConfiguracionSitioService configuracionService,
            SupabaseStorageService supabaseStorageService) {
        this.configuracionService = configuracionService;
        this.supabaseStorageService = supabaseStorageService;
    }

    @GetMapping
    public String Index(Model model) {
        model.addAttribute("configuracion", configuracionService.ObtenerConfiguracion());
        return "admin/configuracion/index";
    }

    @PostMapping("/guardar")
    public String Guardar(@ModelAttribute ConfiguracionSitio configuracion,
            @RequestParam(value = "archivoLogo", required = false) MultipartFile archivoLogo,
            RedirectAttributes flash) {
        try {
            if (archivoLogo != null && !archivoLogo.isEmpty()) {
                String ruta = supabaseStorageService.SubirImagen(archivoLogo, "configuracion");
                configuracion.setLogoUrl(ruta);
            }
            configuracionService.GuardarConfiguracion(configuracion);
            flash.addFlashAttribute("exito", "Configuracion del sitio guardada correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo guardar: " + e.getMessage());
        }
        return "redirect:/admin/configuracion";
    }
}
