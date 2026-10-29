package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.TipoDocumento;
import com.tienda.backend_techzone.Repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public List<TipoDocumento> ListarTiposDocumento() {
        return tipoDocumentoRepository.findAll();
    }

    public List<TipoDocumento> listarTiposDocumento() {
        return ListarTiposDocumento();
    }

    public TipoDocumento crearTipoDocumento(TipoDocumento td) {
        return CrearTipoDocumento(td);
    }

    public TipoDocumento actualizarTipoDocumento(Long id, TipoDocumento td) {
        return ActualizarTipoDocumento(id, td);
    }

    public void eliminarTipoDocumento(Long id) {
        EliminarTipoDocumento(id);
    }

    @org.springframework.transaction.annotation.Transactional
    public TipoDocumento CrearTipoDocumento(TipoDocumento tipoDocumento) {
        validarTipoDocumento(tipoDocumento);
        if (tipoDocumento.getVigente() == null) {
            tipoDocumento.setVigente(true);
        }
        Long nextId = ((Number) entityManager.createNativeQuery(
                "SELECT COALESCE(MAX(id_tipo_documento), 0) + 1 FROM tipo_documento").getSingleResult()).longValue();
        entityManager.createNativeQuery(
                "INSERT INTO tipo_documento (id_tipo_documento, nombre, vigente) VALUES (:id, :nombre, :vigente)")
                .setParameter("id", nextId)
                .setParameter("nombre", tipoDocumento.getNombre().trim())
                .setParameter("vigente", tipoDocumento.getVigente())
                .executeUpdate();
        tipoDocumento.setIdTipoDocumento(nextId);
        return tipoDocumento;
    }

    public TipoDocumento ActualizarTipoDocumento(Long id, TipoDocumento tipoDocumento) {
        TipoDocumento existente = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con id: " + id));
        validarTipoDocumento(tipoDocumento);
        existente.setNombre(tipoDocumento.getNombre().trim());
        if (tipoDocumento.getVigente() != null) {
            existente.setVigente(tipoDocumento.getVigente());
        }
        return tipoDocumentoRepository.save(existente);
    }

    public void EliminarTipoDocumento(Long id) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con id: " + id));
        tipoDocumento.setVigente(false);
        tipoDocumentoRepository.save(tipoDocumento);
    }

    public TipoDocumento ObtenerTipoDocumentoPorId(Long id) {
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con id: " + id));
    }

    private void validarTipoDocumento(TipoDocumento tipoDocumento) {
        if (tipoDocumento.getNombre() == null || tipoDocumento.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del tipo de documento es obligatorio");
        }
        if (tipoDocumento.getNombre().trim().length() > 50) {
            throw new RuntimeException("El nombre no puede superar 50 caracteres");
        }
    }
}
