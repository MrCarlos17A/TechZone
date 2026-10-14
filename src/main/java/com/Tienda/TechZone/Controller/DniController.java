package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Repository.UbigeoRepository;
import com.Tienda.TechZone.Service.DniService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor

public class DniController {

    private static final int MAX_POR_MINUTO = 10;

    private final DniService dniService;
    private final UbigeoRepository ubigeoRepository;

    // Control basico de consultas por IP (la API externa es de pago)
    private final Map<String, long[]> consultasPorIp = new ConcurrentHashMap<>();

    @GetMapping("/api/mis-datos")
    public ResponseEntity<?> misDatos(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "mensaje", "No autenticado"));
        }
        return ResponseEntity.ok(dniService.obtenerMisDatos(userDetails.getUsername()));
    }

    /**
     * Consulta de documento SIN escribir en la base.
     * Primero busca en la BD local; solo si no existe consulta la API externa.
     */
    @PostMapping("/api/consulta-documento")
    public ResponseEntity<?> consultarDocumento(HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        if (!permitido(request.getRemoteAddr())) {
            return ResponseEntity.status(429)
                    .body(Map.of("ok", false, "mensaje", "Demasiadas consultas. Intenta en un minuto."));
        }

        String numero = body != null ? body.get("numero") : null;
        Long idTipo = null;
        String tipo = body != null ? body.get("tipoDocumento") : null;
        if (tipo != null && !tipo.isBlank()) {
            try {
                idTipo = Long.parseLong(tipo.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return ResponseEntity.ok(dniService.consultarDocumento(idTipo, numero));
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

    private boolean permitido(String ip) {
        long ahora = System.currentTimeMillis();
        if (consultasPorIp.size() > 10_000) {
            consultasPorIp.clear();
        }
        long[] registro = consultasPorIp.compute(ip, (k, v) -> {
            if (v == null || ahora - v[0] > 60_000L) {
                return new long[] { ahora, 1 };
            }
            v[1]++;
            return v;
        });
        return registro[1] <= MAX_POR_MINUTO;
    }
}
