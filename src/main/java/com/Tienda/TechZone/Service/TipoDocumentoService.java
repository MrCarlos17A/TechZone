package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.TipoDocumento;
import com.Tienda.TechZone.Repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public List<TipoDocumento> ListarTiposDocumento() {
        return tipoDocumentoRepository.findAll();
    }

    public TipoDocumento CrearTipoDocumento(TipoDocumento tipoDocumento) {
        validarTipoDocumento(tipoDocumento);
        if (tipoDocumento.getVigente() == null) {
            tipoDocumento.setVigente(true);
        }
        if (tipoDocumento.getIdTipoDocumento() == null) {
            tipoDocumento.setIdTipoDocumento(tipoDocumentoRepository.findAll().stream()
                    .map(TipoDocumento::getIdTipoDocumento)
                    .max(Long::compareTo)
                    .orElse(0L) + 1);
        }
        return tipoDocumentoRepository.save(tipoDocumento);
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
        tipoDocumentoRepository.deleteById(id);
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
