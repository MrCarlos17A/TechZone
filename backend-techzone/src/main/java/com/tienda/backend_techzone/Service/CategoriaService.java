package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Categoria;
import com.tienda.backend_techzone.Repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@org.springframework.transaction.annotation.Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> ListarCategorias() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> listarCategorias() {
        return ListarCategorias();
    }

    // Solo categorias vigentes (para la tienda publica)
    public List<Categoria> ListarCategoriasActivas() {
        return categoriaRepository.findAll().stream()
                .filter(c -> c.getVigente() == null || c.getVigente())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Categoria> listarCategoriasActivas() {
        return ListarCategoriasActivas();
    }

    public Categoria obtenerCategoriaPorId(Long id) {
        return ObtenerCategoriaPorId(id);
    }

    public Categoria crearCategoria(Categoria categoria) {
        return CrearCategoria(categoria);
    }

    public Categoria actualizarCategoria(Long id, Categoria categoria) {
        return ActualizarCategoria(id, categoria);
    }

    public void eliminarCategoria(Long id) {
        EliminarCategoria(id);
    }

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @org.springframework.transaction.annotation.Transactional
    public Categoria CrearCategoria(Categoria categoria) {
        validarCategoria(categoria);
        if (categoria.getVigente() == null) {
            categoria.setVigente(true);
        }
        Long nextId = ((Number) entityManager.createNativeQuery(
                "SELECT COALESCE(MAX(id_categoria), 0) + 1 FROM categoria").getSingleResult()).longValue();
        entityManager.createNativeQuery(
                "INSERT INTO categoria (id_categoria, nombre, vigente) VALUES (:id, :nombre, :vigente)")
                .setParameter("id", nextId)
                .setParameter("nombre", categoria.getNombre().trim())
                .setParameter("vigente", categoria.getVigente())
                .executeUpdate();
        categoria.setIdCategoria(nextId);
        return categoria;
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
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + id));
        categoria.setVigente(false);
        categoriaRepository.save(categoria);
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
