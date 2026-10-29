package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Ubigeo;
import com.tienda.backend_techzone.Service.UbigeoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/ubigeos")
public class UbigeoApiController {

    private final UbigeoService ubigeoService;

    public UbigeoApiController(UbigeoService ubigeoService) {
        this.ubigeoService = ubigeoService;
    }

    @GetMapping
    public ResponseEntity<List<Ubigeo>> listar(
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String provincia) {
        List<Ubigeo> lista = ubigeoService.listarUbigeos();
        if (departamento != null && !departamento.isBlank()) {
            lista = lista.stream()
                    .filter(u -> departamento.equalsIgnoreCase(u.getDepartamento()))
                    .toList();
        }
        if (provincia != null && !provincia.isBlank()) {
            lista = lista.stream()
                    .filter(u -> provincia.equalsIgnoreCase(u.getProvincia()))
                    .toList();
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/departamentos")
    public ResponseEntity<List<String>> listarDepartamentos() {
        List<String> deps = ubigeoService.listarUbigeos().stream()
                .map(Ubigeo::getDepartamento)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(deps);
    }

    @GetMapping("/provincias")
    public ResponseEntity<List<String>> listarProvincias(@RequestParam String departamento) {
        List<String> provs = ubigeoService.listarUbigeos().stream()
                .filter(u -> departamento.equalsIgnoreCase(u.getDepartamento()))
                .map(Ubigeo::getProvincia)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(provs);
    }

    @GetMapping("/distritos")
    public ResponseEntity<List<Ubigeo>> listarDistritos(
            @RequestParam String departamento,
            @RequestParam String provincia) {
        List<Ubigeo> dists = ubigeoService.listarUbigeos().stream()
                .filter(u -> departamento.equalsIgnoreCase(u.getDepartamento())
                        && provincia.equalsIgnoreCase(u.getProvincia()))
                .sorted(Comparator.comparing(u -> u.getDistrito() != null ? u.getDistrito() : "", String.CASE_INSENSITIVE_ORDER))
                .toList();
        return ResponseEntity.ok(dists);
    }

    @PostMapping
    public ResponseEntity<Ubigeo> crear(@RequestBody Ubigeo ubigeo) {
        return ResponseEntity.ok(ubigeoService.CrearUbigeo(ubigeo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ubigeo> actualizar(@PathVariable Long id, @RequestBody Ubigeo ubigeo) {
        return ResponseEntity.ok(ubigeoService.ActualizarUbigeo(id, ubigeo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ubigeoService.EliminarUbigeo(id);
        return ResponseEntity.noContent().build();
    }
}
