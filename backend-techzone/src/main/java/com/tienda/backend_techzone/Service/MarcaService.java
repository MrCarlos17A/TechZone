package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Marca;
import com.tienda.backend_techzone.Repository.MarcaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@org.springframework.transaction.annotation.Transactional
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    public List<Marca> ListarMarcas() {
        return marcaRepository.findAll();
    }

    public List<Marca> listarMarcas() {
        return ListarMarcas();
    }

    // Solo marcas vigentes (para la tienda publica)
    public List<Marca> ListarMarcasActivas() {
        return marcaRepository.findAll().stream()
                .filter(m -> m.getVigente() == null || m.getVigente())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Marca> listarMarcasActivas() {
        return ListarMarcasActivas();
    }

    public Marca obtenerMarcaPorId(Long id) {
        return ObtenerMarcaPorId(id);
    }

    public Marca crearMarca(Marca marca) {
        return CrearMarca(marca);
    }

    public Marca actualizarMarca(Long id, Marca marca) {
        return ActualizarMarca(id, marca);
    }

    public void eliminarMarca(Long id) {
        EliminarMarca(id);
    }

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @org.springframework.transaction.annotation.Transactional
    public Marca CrearMarca(Marca marca) {
        validarMarca(marca);
        if (marca.getVigente() == null) {
            marca.setVigente(true);
        }
        marca.setLogoUrl(normalizarLogoUrl(marca.getLogoUrl()));
        Long nextId = ((Number) entityManager.createNativeQuery(
                "SELECT COALESCE(MAX(id_marca), 0) + 1 FROM marca").getSingleResult()).longValue();
        entityManager.createNativeQuery(
                "INSERT INTO marca (id_marca, nombre, logo_url, vigente) VALUES (:id, :nombre, :logo, :vigente)")
                .setParameter("id", nextId)
                .setParameter("nombre", marca.getNombre().trim())
                .setParameter("logo", marca.getLogoUrl())
                .setParameter("vigente", marca.getVigente())
                .executeUpdate();
        marca.setIdMarca(nextId);
        return marca;
    }

    public Marca ActualizarMarca(Long id, Marca marca) {
        Marca existente = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + id));
        validarMarca(marca);
        existente.setNombre(marca.getNombre().trim());
        existente.setLogoUrl(normalizarLogoUrl(marca.getLogoUrl()));
        if (marca.getVigente() != null) {
            existente.setVigente(marca.getVigente());
        }
        return marcaRepository.save(existente);
    }

    // Vacio o nulo -> null (el template entonces usa la imagen local de respaldo)
    private String normalizarLogoUrl(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return null;
        }
        return logoUrl.trim();
    }

    public void EliminarMarca(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + id));
        marca.setVigente(false);
        marcaRepository.save(marca);
    }

    public Marca ObtenerMarcaPorId(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + id));
    }

    private void validarMarca(Marca marca) {
        if (marca.getNombre() == null || marca.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la marca es obligatorio");
        }
        if (marca.getNombre().trim().length() > 20) {
            throw new RuntimeException("El nombre de la marca no puede superar 20 caracteres");
        }
    }
}
