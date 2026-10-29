package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Service.DniService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class DniApiController {

    private final DniService dniService;

    public DniApiController(DniService dniService) {
        this.dniService = dniService;
    }

    @GetMapping("/api/consulta-documento")
    public ResponseEntity<?> consultarDocumento(
            @RequestParam(required = false) Long tipoDocumento,
            @RequestParam String numero) {
        DniService.DocumentoResponse respuesta = dniService.consultarDocumento(tipoDocumento, numero);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/api/mis-datos")
    public ResponseEntity<?> obtenerMisDatos(@AuthenticationPrincipal UserDetails detalles) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesion"));
        }
        DniService.MisDatos datos = dniService.obtenerMisDatos(detalles.getUsername());
        return ResponseEntity.ok(datos);
    }
}
