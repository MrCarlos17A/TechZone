package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.RegistroAcceso;
import com.tienda.backend_techzone.Model.RegistroAuditoria;
import com.tienda.backend_techzone.Repository.RegistroAccesoRepository;
import com.tienda.backend_techzone.Repository.RegistroAuditoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/auditoria")
public class AuditoriaAdminApiController {

    private final RegistroAccesoRepository registroAccesoRepository;
    private final RegistroAuditoriaRepository registroAuditoriaRepository;

    public AuditoriaAdminApiController(RegistroAccesoRepository registroAccesoRepository,
                                       RegistroAuditoriaRepository registroAuditoriaRepository) {
        this.registroAccesoRepository = registroAccesoRepository;
        this.registroAuditoriaRepository = registroAuditoriaRepository;
    }

    @GetMapping("/accesos")
    public ResponseEntity<List<RegistroAcceso>> listarAccesos() {
        return ResponseEntity.ok(registroAccesoRepository.findTop100ByOrderByFechaHoraDesc());
    }

    @GetMapping("/acciones")
    public ResponseEntity<List<RegistroAuditoria>> listarAcciones(@RequestParam(required = false) String modulo) {
        if (modulo != null && !modulo.isBlank()) {
            return ResponseEntity.ok(registroAuditoriaRepository.findByModuloOrderByFechaHoraDesc(modulo));
        }
        return ResponseEntity.ok(registroAuditoriaRepository.findTop100ByOrderByFechaHoraDesc());
    }
}
