package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.DetalleVenta;
import com.tienda.backend_techzone.Service.DetalleVentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/detalles-venta")
public class DetalleVentaAdminApiController {

    private final DetalleVentaService detalleVentaService;

    public DetalleVentaAdminApiController(DetalleVentaService detalleVentaService) {
        this.detalleVentaService = detalleVentaService;
    }

    @GetMapping
    public ResponseEntity<List<DetalleVenta>> listar(@RequestParam(required = false) Long idVenta) {
        return ResponseEntity.ok(detalleVentaService.ListarPorVenta(idVenta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleVenta> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(detalleVentaService.ObtenerDetallePorId(id));
    }
}
