package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.TipoDocumento;
import com.tienda.backend_techzone.Service.TipoDocumentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class TipoDocumentoApiController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoApiController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @GetMapping("/api/tipos-documento")
    public ResponseEntity<List<TipoDocumento>> listarPublico() {
        List<TipoDocumento> activos = tipoDocumentoService.listarTiposDocumento().stream()
                .filter(t -> t.getVigente() == null || t.getVigente())
                .collect(Collectors.toList());
        return ResponseEntity.ok(activos);
    }

    @GetMapping("/api/admin/tipos-documento")
    public ResponseEntity<List<TipoDocumento>> listarAdmin() {
        return ResponseEntity.ok(tipoDocumentoService.listarTiposDocumento());
    }

    @PostMapping("/api/admin/tipos-documento")
    public ResponseEntity<?> crear(@RequestBody TipoDocumento tipoDocumento) {
        try {
            return ResponseEntity.status(201).body(tipoDocumentoService.crearTipoDocumento(tipoDocumento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/tipos-documento/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody TipoDocumento tipoDocumento) {
        try {
            return ResponseEntity.ok(tipoDocumentoService.actualizarTipoDocumento(id, tipoDocumento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/tipos-documento/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            tipoDocumentoService.eliminarTipoDocumento(id);
            return ResponseEntity.ok(Map.of("mensaje", "Tipo de documento eliminado"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
