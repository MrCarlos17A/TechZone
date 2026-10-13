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
        if (categoria.getVigente() == null) {
            categoria.setVigente(true);
        }
        if (categoria.getIdCategoria() == null) {
            categoria.setIdCategoria(categoriaRepository.findAll().stream()
                    .map(Categoria::getIdCategoria)
                    .max(Long::compareTo)
                    .orElse(0L) + 1);
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria ActualizarCategoria(Long id, Categoria categoria) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + id));
        validarCategoria(categoria);
        existente.setNombre(categoria.getNombre().trim());
        if (categoria.getVigente() != null) {
            existente.setVigente(categoria.getVigente());
        }
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
        if (categoria.getNombre().trim().length() > 50) {
            throw new RuntimeException("El nombre no puede superar 50 caracteres");
        }
    }
}
