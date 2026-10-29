package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.ProductoProveedor;
import com.tienda.backend_techzone.Service.ProductoProveedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/productos-proveedor")
public class ProductoProveedorAdminApiController {

    private final ProductoProveedorService productoProveedorService;

    public ProductoProveedorAdminApiController(ProductoProveedorService productoProveedorService) {
        this.productoProveedorService = productoProveedorService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoProveedor>> listar() {
        return ResponseEntity.ok(productoProveedorService.ListarPorProducto(null));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ProductoProveedor productoProveedor) {
        try {
            return ResponseEntity.status(201).body(productoProveedorService.CrearRelacion(productoProveedor));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/{idProducto}/{idProveedor}")
    public ResponseEntity<?> eliminar(@PathVariable Long idProducto, @PathVariable Long idProveedor) {
        try {
            productoProveedorService.EliminarRelacion(idProducto, idProveedor);
            return ResponseEntity.ok(Map.of("mensaje", "Relación eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
