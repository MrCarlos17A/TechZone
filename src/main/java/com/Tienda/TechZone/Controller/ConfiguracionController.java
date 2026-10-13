package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.ConfiguracionSitio;
import com.Tienda.TechZone.Service.ConfiguracionSitioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/admin/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private static final Path UPLOADS_DIR = Paths.get("uploads");
    private final ConfiguracionSitioService configuracionService;

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
                String ruta = guardarArchivo(archivoLogo);
                configuracion.setLogoUrl(ruta);
            }
            configuracionService.GuardarConfiguracion(configuracion);
            flash.addFlashAttribute("exito", "Configuracion del sitio guardada correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo guardar: " + e.getMessage());
        }
        return "redirect:/admin/configuracion";
    }

    private String guardarArchivo(MultipartFile archivo) throws IOException {
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new RuntimeException("El archivo no tiene nombre valido");
        }
        String nombreLimpio = Paths.get(nombreOriginal).getFileName().toString();
        String extension = nombreLimpio.contains(".")
                ? nombreLimpio.substring(nombreLimpio.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!java.util.Set.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
            throw new RuntimeException("Formato no valido. Usa JPG, PNG, GIF o WEBP");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }
        String nombreUnico = System.currentTimeMillis() + "_" + nombreLimpio.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!Files.exists(UPLOADS_DIR)) {
            Files.createDirectories(UPLOADS_DIR);
        }
        Path destino = UPLOADS_DIR.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + nombreUnico;
    }
}
