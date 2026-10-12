package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Repository.UbigeoRepository;
import com.Tienda.TechZone.Service.DniService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DniController {

    private final DniService dniService;
    private final UbigeoRepository ubigeoRepository;

    @GetMapping("/api/mis-datos")
    public ResponseEntity<?> misDatos(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "mensaje", "No autenticado"));
        }
        return ResponseEntity.ok(dniService.obtenerMisDatos(userDetails.getUsername()));
    }

    @PostMapping("/api/dni")
    public ResponseEntity<?> buscarDni(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "mensaje", "No autenticado"));
        }
        String dni = body.get("dni");
        if (dni == null || !dni.matches("\\d{8}")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "mensaje", "El DNI debe tener 8 digitos"));
        }
        return ResponseEntity.ok(dniService.buscarDni(userDetails.getUsername(), dni));
    }

    @GetMapping("/api/ubigeos")
    public ResponseEntity<?> ubigeos() {
        List<Map<String, Object>> lista = ubigeoRepository.findAllByVigenteTrue().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getIdUbigeo(),
                        "departamento", u.getDepartamento() != null ? u.getDepartamento() : "",
                        "provincia", u.getProvincia() != null ? u.getProvincia() : "",
                        "distrito", u.getDistrito() != null ? u.getDistrito() : ""))
                .toList();
        return ResponseEntity.ok(lista);
    }
}
