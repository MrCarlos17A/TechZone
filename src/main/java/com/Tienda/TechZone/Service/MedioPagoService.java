package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.MedioPago;
import com.Tienda.TechZone.Repository.MedioPagoRepository;
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

    public MedioPago CrearMedioPago(MedioPago medioPago) {
        if (medioPago.getNombre() == null || medioPago.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del medio de pago es obligatorio");
        }
        if (medioPago.getVigente() == null) {
            medioPago.setVigente(true);
        }
        return medioPagoRepository.save(medioPago);
    }

    public MedioPago ActualizarMedioPago(Integer id, MedioPago medioPago) {
        MedioPago existente = medioPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado con id: " + id));
        existente.setNombre(medioPago.getNombre().trim());
        if (medioPago.getVigente() != null) {
            existente.setVigente(medioPago.getVigente());
        }
        return medioPagoRepository.save(existente);
    }

    public void EliminarMedioPago(Integer id) {
        medioPagoRepository.deleteById(id);
    }

    public MedioPago ObtenerMedioPagoPorId(Integer id) {
        return medioPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado con id: " + id));
    }
}
