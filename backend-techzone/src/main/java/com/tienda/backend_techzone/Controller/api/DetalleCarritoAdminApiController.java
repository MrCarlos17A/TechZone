package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.DetalleCarrito;
import com.tienda.backend_techzone.Service.DetalleCarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/detalles-carrito")
public class DetalleCarritoAdminApiController {

    private final DetalleCarritoService detalleCarritoService;

    public DetalleCarritoAdminApiController(DetalleCarritoService detalleCarritoService) {
        this.detalleCarritoService = detalleCarritoService;
    }

    @GetMapping
    public ResponseEntity<List<DetalleCarrito>> listar(@RequestParam(required = false) Long idCarrito) {
        return ResponseEntity.ok(detalleCarritoService.ListarPorCarrito(idCarrito));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        detalleCarritoService.EliminarDetalle(id);
        return ResponseEntity.noContent().build();
    }
}
