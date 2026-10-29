package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.ConfiguracionSitio;
import com.tienda.backend_techzone.Service.ConfiguracionSitioService;
import com.tienda.backend_techzone.Service.SupabaseStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class ConfiguracionApiController {

    private final ConfiguracionSitioService configuracionSitioService;
    private final SupabaseStorageService supabaseStorageService;

    public ConfiguracionApiController(ConfiguracionSitioService configuracionSitioService,
                                     SupabaseStorageService supabaseStorageService) {
        this.configuracionSitioService = configuracionSitioService;
        this.supabaseStorageService = supabaseStorageService;
    }

    @GetMapping("/api/configuracion")
    public ResponseEntity<ConfiguracionSitio> obtener() {
        return ResponseEntity.ok(configuracionSitioService.obtenerConfiguracion());
    }

    @PutMapping("/api/admin/configuracion")
    public ResponseEntity<?> guardar(@RequestBody ConfiguracionSitio config) {
        try {
            ConfiguracionSitio guardada = configuracionSitioService.guardarConfiguracion(config);
            return ResponseEntity.ok(guardada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping(value = "/api/admin/configuracion/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirImagenes(
            @RequestParam(value = "archivoLogo", required = false) MultipartFile archivoLogo,
            @RequestParam(value = "archivoLibro", required = false) MultipartFile archivoLibro) {
        try {
            ConfiguracionSitio config = configuracionSitioService.obtenerConfiguracion();
            if (archivoLogo != null && !archivoLogo.isEmpty()) {
                if (config.getLogoUrl() != null && !config.getLogoUrl().isBlank()) {
                    supabaseStorageService.EliminarArchivo(config.getLogoUrl());
                }
                String urlLogo = supabaseStorageService.SubirImagen(archivoLogo, "sitio");
                config.setLogoUrl(urlLogo);
            }
            if (archivoLibro != null && !archivoLibro.isEmpty()) {
                if (config.getLibroReclamacionesImg() != null && !config.getLibroReclamacionesImg().isBlank()) {
                    supabaseStorageService.EliminarArchivo(config.getLibroReclamacionesImg());
                }
                String urlLibro = supabaseStorageService.SubirImagen(archivoLibro, "sitio");
                config.setLibroReclamacionesImg(urlLibro);
            }
            ConfiguracionSitio guardada = configuracionSitioService.guardarConfiguracion(config);
            return ResponseEntity.ok(guardada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
