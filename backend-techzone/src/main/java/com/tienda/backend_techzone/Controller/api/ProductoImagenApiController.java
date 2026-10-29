package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Producto;
import com.tienda.backend_techzone.Model.ProductoImagen;
import com.tienda.backend_techzone.Service.ProductoImagenService;
import com.tienda.backend_techzone.Service.ProductoService;
import com.tienda.backend_techzone.Service.SupabaseStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ProductoImagenApiController {

    private final ProductoImagenService productoImagenService;
    private final ProductoService productoService;
    private final SupabaseStorageService supabaseStorageService;

    public ProductoImagenApiController(ProductoImagenService productoImagenService,
                                       ProductoService productoService,
                                       SupabaseStorageService supabaseStorageService) {
        this.productoImagenService = productoImagenService;
        this.productoService = productoService;
        this.supabaseStorageService = supabaseStorageService;
    }

    @GetMapping("/api/admin/imagenes")
    public ResponseEntity<List<ProductoImagen>> listarTodas() {
        return ResponseEntity.ok(productoImagenService.ListarImagenesPorProducto(null));
    }

    @GetMapping("/api/productos/{idProducto}/imagenes")
    public ResponseEntity<List<ProductoImagen>> listarPorProducto(@PathVariable Long idProducto) {
        return ResponseEntity.ok(productoImagenService.listarPorProducto(idProducto));
    }

    @PostMapping(value = "/api/admin/productos/{idProducto}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirImagen(
            @PathVariable Long idProducto,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "esPrincipal", defaultValue = "false") Boolean esPrincipal) {
        try {
            Producto producto = productoService.obtenerProductoPorId(idProducto);
            String carpeta = producto.getCategoria() != null
                    ? supabaseStorageService.CarpetaDeCategoria(producto.getCategoria().getNombre())
                    : "productos";

            String urlPublica = supabaseStorageService.SubirImagen(archivo, carpeta);
            if (urlPublica == null) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "No se envio ningun archivo"));
            }

            ProductoImagen imagen = ProductoImagen.builder()
                    .producto(producto)
                    .urlImagen(urlPublica)
                    .esPrincipal(esPrincipal)
                    .build();

            ProductoImagen guardada = productoImagenService.crearImagen(imagen);
            return ResponseEntity.status(201).body(guardada);
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/productos/{idProducto}/imagenes/url")
    public ResponseEntity<?> agregarPorUrl(
            @PathVariable Long idProducto,
            @RequestBody Map<String, Object> body) {
        try {
            Producto producto = productoService.obtenerProductoPorId(idProducto);
            String url = (String) body.get("urlImagen");
            Boolean esPrincipal = Boolean.TRUE.equals(body.get("esPrincipal"));

            if (url == null || url.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "La URL es obligatoria"));
            }

            String carpeta = producto.getCategoria() != null
                    ? supabaseStorageService.CarpetaDeCategoria(producto.getCategoria().getNombre())
                    : "productos";

            String urlFinal = supabaseStorageService.SubirImagenDesdeUrl(url.trim(), carpeta);

            ProductoImagen imagen = ProductoImagen.builder()
                    .producto(producto)
                    .urlImagen(urlFinal)
                    .esPrincipal(esPrincipal)
                    .build();

            ProductoImagen guardada = productoImagenService.crearImagen(imagen);
            return ResponseEntity.status(201).body(guardada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/imagenes/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Long idProducto = body.get("idProducto") != null ? Long.valueOf(body.get("idProducto").toString()) : null;
            String url = (String) body.get("urlImagen");
            Boolean esPrincipal = body.get("esPrincipal") != null ? Boolean.valueOf(body.get("esPrincipal").toString()) : null;

            String urlFinal = url;
            if (url != null && !url.isBlank()) {
                String carpeta = "productos";
                if (idProducto != null) {
                    try {
                        Producto p = productoService.obtenerProductoPorId(idProducto);
                        if (p.getCategoria() != null) {
                            carpeta = supabaseStorageService.CarpetaDeCategoria(p.getCategoria().getNombre());
                        }
                    } catch (Exception ignored) {}
                }
                urlFinal = supabaseStorageService.SubirImagenDesdeUrl(url.trim(), carpeta);
            }

            ProductoImagen actualizada = productoImagenService.actualizarCompleto(id, idProducto, urlFinal, esPrincipal);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping(value = "/api/admin/imagenes/{id}/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarConArchivo(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "idProducto", required = false) Long idProducto,
            @RequestParam(value = "esPrincipal", required = false) Boolean esPrincipal) {
        try {
            String carpeta = "productos";
            if (idProducto != null) {
                try {
                    Producto p = productoService.obtenerProductoPorId(idProducto);
                    if (p.getCategoria() != null) {
                        carpeta = supabaseStorageService.CarpetaDeCategoria(p.getCategoria().getNombre());
                    }
                } catch (Exception ignored) {}
            }

            String urlPublica = supabaseStorageService.SubirImagen(archivo, carpeta);
            ProductoImagen actualizada = productoImagenService.actualizarCompleto(id, idProducto, urlPublica, esPrincipal);
            return ResponseEntity.ok(actualizada);
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/imagenes/{id}/principal")
    public ResponseEntity<?> establecerPrincipal(@PathVariable Long id) {
        try {
            ProductoImagen actualizada = productoImagenService.establecerComoPrincipal(id);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/imagenes/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            productoImagenService.eliminarImagen(id);
            return ResponseEntity.ok(Map.of("mensaje", "Imagen eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
