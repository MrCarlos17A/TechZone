package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.MedioPago;
import com.tienda.backend_techzone.Service.MedioPagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MedioPagoApiController {

    private final MedioPagoService medioPagoService;

    public MedioPagoApiController(MedioPagoService medioPagoService) {
        this.medioPagoService = medioPagoService;
    }

    @GetMapping("/api/medios-pago")
    public ResponseEntity<List<MedioPago>> listarPublico() {
        List<MedioPago> activos = medioPagoService.listarMediosPago().stream()
                .filter(m -> m.getVigente() == null || m.getVigente())
                .collect(Collectors.toList());
        return ResponseEntity.ok(activos);
    }

    @GetMapping("/api/admin/medios-pago")
    public ResponseEntity<List<MedioPago>> listarAdmin() {
        return ResponseEntity.ok(medioPagoService.listarMediosPago());
    }

    @PostMapping("/api/admin/medios-pago")
    public ResponseEntity<?> crear(@RequestBody MedioPago medioPago) {
        try {
            return ResponseEntity.status(201).body(medioPagoService.crearMedioPago(medioPago));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/medios-pago/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody MedioPago medioPago) {
        try {
            return ResponseEntity.ok(medioPagoService.actualizarMedioPago(id, medioPago));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/medios-pago/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            medioPagoService.eliminarMedioPago(id);
            return ResponseEntity.ok(Map.of("mensaje", "Medio de pago eliminado"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
