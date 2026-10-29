package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Marca;
import com.tienda.backend_techzone.Service.MarcaService;
import com.tienda.backend_techzone.Service.SupabaseStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class MarcaApiController {

    private final MarcaService marcaService;
    private final SupabaseStorageService supabaseStorageService;

    public MarcaApiController(MarcaService marcaService, SupabaseStorageService supabaseStorageService) {
        this.marcaService = marcaService;
        this.supabaseStorageService = supabaseStorageService;
    }

    // --- Endpoints Publicos ---
    @GetMapping("/api/marcas")
    public ResponseEntity<List<Marca>> listarPublicas() {
        return ResponseEntity.ok(marcaService.listarMarcasActivas());
    }

    @GetMapping("/api/marcas/{id}")
    public ResponseEntity<?> obtenerPublica(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(marcaService.obtenerMarcaPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensaje", e.getMessage()));
        }
    }

    // --- Endpoints Administrativos ---
    @GetMapping("/api/admin/marcas")
    public ResponseEntity<List<Marca>> listarAdmin() {
        return ResponseEntity.ok(marcaService.listarMarcas());
    }

    @PostMapping("/api/admin/marcas")
    public ResponseEntity<?> crearAdmin(@RequestBody Marca marca) {
        try {
            if (marca.getLogoUrl() != null && !marca.getLogoUrl().isBlank()) {
                String urlFinal = supabaseStorageService.SubirImagenDesdeUrl(marca.getLogoUrl(), "marcas");
                marca.setLogoUrl(urlFinal);
            }
            Marca creada = marcaService.crearMarca(marca);
            return ResponseEntity.status(201).body(creada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/marcas/{id}")
    public ResponseEntity<?> actualizarAdmin(@PathVariable Long id, @RequestBody Marca marca) {
        try {
            if (marca.getLogoUrl() != null && !marca.getLogoUrl().isBlank()) {
                String urlFinal = supabaseStorageService.SubirImagenDesdeUrl(marca.getLogoUrl(), "marcas");
                marca.setLogoUrl(urlFinal);
            }
            Marca actualizada = marcaService.actualizarMarca(id, marca);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping(value = "/api/admin/marcas/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirLogoMarca(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            Marca marca = marcaService.obtenerMarcaPorId(id);
            if (archivo == null || archivo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "No se envió ningún archivo de imagen"));
            }
            if (marca.getLogoUrl() != null && !marca.getLogoUrl().isBlank()) {
                try {
                    supabaseStorageService.EliminarArchivo(marca.getLogoUrl());
                } catch (Exception ignored) {
                }
            }
            String urlLogo = supabaseStorageService.SubirImagen(archivo, "marcas");
            marca.setLogoUrl(urlLogo);
            Marca guardada = marcaService.actualizarMarca(id, marca);
            return ResponseEntity.ok(guardada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Error al subir logo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/marcas/{id}")
    public ResponseEntity<?> eliminarAdmin(@PathVariable Long id) {
        try {
            marcaService.eliminarMarca(id);
            return ResponseEntity.ok(Map.of("mensaje", "Marca eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
