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
                    .max(Integer::compareTo)
                    .orElse(0) + 1);
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria ActualizarCategoria(Integer id, Categoria categoria) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + id));
        existente.setNombre(categoria.getNombre().trim());
        if (categoria.getVigente() != null) {
            existente.setVigente(categoria.getVigente());
        }
        validarCategoria(existente);
        return categoriaRepository.save(existente);
    }

    public void EliminarCategoria(Integer id) {
        categoriaRepository.deleteById(id);
    }

    public Categoria ObtenerCategoriaPorId(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + id));
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la categoria es obligatorio");
        }
    }
}
