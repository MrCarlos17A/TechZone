package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Categoria;
import com.Tienda.TechZone.Repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> ListarCategorias() {
        return categoriaRepository.findAll();
    }

    public Categoria CrearCategoria(Categoria categoria) {
        validarCategoria(categoria);
        if (categoria.getEstadoCategoria() == null) {
            categoria.setEstadoCategoria(true);
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria ActualizarCategoria(Long id, Categoria categoria) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + id));
        existente.setNombre(categoria.getNombre().trim());
        existente.setDescripcion(categoria.getDescripcion());
        if (categoria.getEstadoCategoria() != null) {
            existente.setEstadoCategoria(categoria.getEstadoCategoria());
        }
        validarCategoria(existente);
        return categoriaRepository.save(existente);
    }

    public void EliminarCategoria(Long id) {
        categoriaRepository.deleteById(id);
    }

    public Categoria ObtenerCategoriaPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + id));
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la categoria es obligatorio");
        }
    }
}
