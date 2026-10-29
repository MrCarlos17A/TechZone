package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.CentroAyuda;
import com.tienda.backend_techzone.Repository.CentroAyudaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AyudaApiController {

    private final CentroAyudaRepository centroAyudaRepository;

    public AyudaApiController(CentroAyudaRepository centroAyudaRepository) {
        this.centroAyudaRepository = centroAyudaRepository;
    }

    @GetMapping("/api/ayuda")
    public ResponseEntity<List<CentroAyuda>> listarTodo() {
        return ResponseEntity.ok(centroAyudaRepository.findByActivoTrueOrderByOrdenAsc());
    }

    @GetMapping("/api/ayuda/{seccion}")
    public ResponseEntity<List<CentroAyuda>> listarPorSeccion(@PathVariable String seccion) {
        return ResponseEntity.ok(centroAyudaRepository.findBySeccionAndActivoTrueOrderByOrdenAsc(seccion));
    }
}
