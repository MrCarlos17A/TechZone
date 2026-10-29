package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.MedioPago;
import com.tienda.backend_techzone.Repository.MedioPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedioPagoService {

    private final MedioPagoRepository medioPagoRepository;

    public List<MedioPago> ListarMediosPago() {
        return medioPagoRepository.findAll();
    }

    public List<MedioPago> listarMediosPago() {
        return ListarMediosPago();
    }

    public MedioPago obtenerMedioPagoPorId(Long id) {
        return ObtenerMedioPagoPorId(id);
    }

    public MedioPago crearMedioPago(MedioPago medioPago) {
        return CrearMedioPago(medioPago);
    }

    public MedioPago actualizarMedioPago(Long id, MedioPago medioPago) {
        return ActualizarMedioPago(id, medioPago);
    }

    public void eliminarMedioPago(Long id) {
        EliminarMedioPago(id);
    }

    public MedioPago CrearMedioPago(MedioPago medioPago) {
        if (medioPago.getNombre() == null || medioPago.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del medio de pago es obligatorio");
        }
        if (medioPago.getVigente() == null) {
            medioPago.setVigente(true);
        }
        return medioPagoRepository.save(medioPago);
    }

    public MedioPago ActualizarMedioPago(Long id, MedioPago medioPago) {
        MedioPago existente = medioPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado con id: " + id));
        if (medioPago.getNombre() == null || medioPago.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del medio de pago es obligatorio");
        }
        existente.setNombre(medioPago.getNombre().trim());
        existente.setInstrucciones(medioPago.getInstrucciones());
        if (medioPago.getVigente() != null) {
            existente.setVigente(medioPago.getVigente());
        }
        return medioPagoRepository.save(existente);
    }

    public void EliminarMedioPago(Long id) {
        MedioPago medio = medioPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado con id: " + id));
        medio.setVigente(false);
        medioPagoRepository.save(medio);
    }

    public MedioPago ObtenerMedioPagoPorId(Long id) {
        return medioPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado con id: " + id));
    }
}
