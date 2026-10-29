package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Categoria;
import com.tienda.backend_techzone.Service.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CategoriaApiController {

    private final CategoriaService categoriaService;

    public CategoriaApiController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // --- Endpoints Publicos ---
    @GetMapping("/api/categorias")
    public ResponseEntity<List<Categoria>> listarPublicas() {
        return ResponseEntity.ok(categoriaService.listarCategoriasActivas());
    }

    @GetMapping("/api/categorias/{id}")
    public ResponseEntity<?> obtenerPublica(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(categoriaService.obtenerCategoriaPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensaje", e.getMessage()));
        }
    }

    // --- Endpoints Administrativos ---
    @GetMapping("/api/admin/categorias")
    public ResponseEntity<List<Categoria>> listarAdmin() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    @PostMapping("/api/admin/categorias")
    public ResponseEntity<?> crearAdmin(@RequestBody Categoria categoria) {
        try {
            Categoria creada = categoriaService.crearCategoria(categoria);
            return ResponseEntity.status(201).body(creada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/categorias/{id}")
    public ResponseEntity<?> actualizarAdmin(@PathVariable Long id, @RequestBody Categoria categoria) {
        try {
            Categoria actualizada = categoriaService.actualizarCategoria(id, categoria);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/categorias/{id}")
    public ResponseEntity<?> eliminarAdmin(@PathVariable Long id) {
        try {
            categoriaService.eliminarCategoria(id);
            return ResponseEntity.ok(Map.of("mensaje", "Categoria eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
