package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Producto;
import com.tienda.backend_techzone.Service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ProductoApiController {

    private final ProductoService productoService;

    public ProductoApiController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // --- Endpoints Publicos ---
    @GetMapping("/api/productos")
    public ResponseEntity<List<Producto>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) Long marca) {

        List<Producto> productos;

        if (q != null && !q.isBlank()) {
            productos = productoService.buscarProductos(q);
        } else if (categoria != null) {
            productos = productoService.listarProductosActivosPorCategoria(categoria);
        } else if (marca != null) {
            productos = productoService.listarProductosPorMarca(marca).stream()
                    .filter(p -> p.getVigente() == null || p.getVigente())
                    .collect(Collectors.toList());
        } else {
            productos = productoService.listarProductosActivos();
        }

        // Si se paso marca junto con categoria/q, filtrar en memoria
        if (marca != null && (q != null || categoria != null)) {
            productos = productos.stream()
                    .filter(p -> p.getMarca() != null && p.getMarca().getIdMarca().equals(marca))
                    .collect(Collectors.toList());
        }
        if (categoria != null && q != null) {
            productos = productos.stream()
                    .filter(p -> p.getCategoria() != null && p.getCategoria().getIdCategoria().equals(categoria))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(productos);
    }

    @GetMapping("/api/productos/destacados")
    public ResponseEntity<List<Producto>> destacados() {
        try {
            return ResponseEntity.ok(productoService.listarProductosDestacados());
        } catch (Exception e) {
            return ResponseEntity.ok(productoService.listarProductosActivos().stream().limit(9).collect(Collectors.toList()));
        }
    }

    @GetMapping("/api/productos/novedades")
    public ResponseEntity<List<Producto>> novedades() {
        try {
            return ResponseEntity.ok(productoService.listarProductosNovedades());
        } catch (Exception e) {
            return ResponseEntity.ok(productoService.listarProductosActivos());
        }
    }

    @GetMapping("/api/productos/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.obtenerProductoPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensaje", e.getMessage()));
        }
    }

    // --- Endpoints Administrativos ---
    @GetMapping("/api/admin/productos")
    public ResponseEntity<List<Producto>> listarAdmin() {
        return ResponseEntity.ok(productoService.listarProductos());
    }

    @PostMapping("/api/admin/productos")
    public ResponseEntity<?> crearAdmin(@RequestBody Producto producto) {
        try {
            Producto nuevo = productoService.crearProducto(producto);
            return ResponseEntity.status(201).body(nuevo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/productos/{id}")
    public ResponseEntity<?> actualizarAdmin(@PathVariable Long id, @RequestBody Producto producto) {
        try {
            Producto actualizado = productoService.actualizarProducto(id, producto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/productos/{id}/toggle-destacado")
    public ResponseEntity<?> toggleDestacado(@PathVariable Long id) {
        try {
            Producto actualizado = productoService.toggleDestacado(id);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "mensaje", e.getMessage() != null ? e.getMessage() : "Error al alternar estado destacado"
            ));
        }
    }

    @PutMapping("/api/admin/productos/{id}/toggle-novedad")
    public ResponseEntity<?> toggleNovedad(@PathVariable Long id) {
        try {
            Producto actualizado = productoService.toggleNovedad(id);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "mensaje", e.getMessage() != null ? e.getMessage() : "Error al alternar estado novedad"
            ));
        }
    }

    public record SolicitudSustitucionDestacado(Long idSaliente, Long idEntrante) {}

    @PutMapping("/api/admin/productos/sustituir-destacado")
    public ResponseEntity<?> sustituirDestacado(@RequestBody SolicitudSustitucionDestacado req) {
        try {
            if (req == null || req.idSaliente() == null || req.idEntrante() == null) {
                return ResponseEntity.badRequest().body(Map.of("ok", false, "mensaje", "Parámetros incompletos para la sustitución"));
            }
            List<Producto> actualizados = productoService.sustituirDestacado(req.idSaliente(), req.idEntrante());
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "mensaje", "Sustitución de producto destacado realizada exitosamente",
                    "productos", actualizados
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "mensaje", e.getMessage() != null ? e.getMessage() : "Error al sustituir producto destacado"
            ));
        }
    }

    @DeleteMapping("/api/admin/productos/{id}")
    public ResponseEntity<?> eliminarAdmin(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
