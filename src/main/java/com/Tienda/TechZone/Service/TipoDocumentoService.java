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
        if (tipoDocumento.getNombre() == null || tipoDocumento.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del tipo de documento es obligatorio");
        }
        if (tipoDocumento.getVigente() == null) {
            tipoDocumento.setVigente(true);
        }
        if (tipoDocumento.getIdTipoDocumento() == null) {
            tipoDocumento.setIdTipoDocumento(tipoDocumentoRepository.findAll().stream()
                    .map(TipoDocumento::getIdTipoDocumento)
                    .max(Integer::compareTo)
                    .orElse(0) + 1);
        }
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    public TipoDocumento ActualizarTipoDocumento(Integer id, TipoDocumento tipoDocumento) {
        TipoDocumento existente = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con id: " + id));
        existente.setNombre(tipoDocumento.getNombre().trim());
        if (tipoDocumento.getVigente() != null) {
            existente.setVigente(tipoDocumento.getVigente());
        }
        return tipoDocumentoRepository.save(existente);
    }

    public void EliminarTipoDocumento(Integer id) {
        tipoDocumentoRepository.deleteById(id);
    }

    public TipoDocumento ObtenerTipoDocumentoPorId(Integer id) {
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con id: " + id));
    }
}
