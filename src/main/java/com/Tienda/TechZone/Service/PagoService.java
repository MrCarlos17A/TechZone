package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.MedioPago;
import com.Tienda.TechZone.Model.Pago;
import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Repository.MedioPagoRepository;
import com.Tienda.TechZone.Repository.PagoRepository;
import com.Tienda.TechZone.Repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final VentaRepository ventaRepository;
    private final MedioPagoRepository medioPagoRepository;

    public List<Pago> ListarPagos() {
        return pagoRepository.findAll();
    }

    public Pago ObtenerPagoPorId(Integer id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con id: " + id));
    }

    public Pago RegistrarPago(Pago pago) {
        if (pago.getVenta() == null || pago.getVenta().getIdVenta() == null) {
            throw new RuntimeException("La venta es obligatoria");
        }
        if (pago.getMedioPago() == null || pago.getMedioPago().getIdMedioPago() == null) {
            throw new RuntimeException("El medio de pago es obligatorio");
        }
        if (pago.getMontoPago() == null || pago.getMontoPago().signum() <= 0) {
            throw new RuntimeException("El monto debe ser mayor a 0");
        }

        Venta venta = ventaRepository.findById(pago.getVenta().getIdVenta())
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        MedioPago medioPago = medioPagoRepository.findById(pago.getMedioPago().getIdMedioPago())
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado"));

        pago.setVenta(venta);
        pago.setMedioPago(medioPago);
        pago.setFechaPago(LocalDateTime.now());
        if (pago.getEstadoPago() == null) {
            pago.setEstadoPago("CONFIRMADO");
        }
        return pagoRepository.save(pago);
    }

    public Pago CambiarEstado(Integer idPago, String estado) {
        Pago pago = ObtenerPagoPorId(idPago);
        pago.setEstadoPago(estado);
        return pagoRepository.save(pago);
    }
}
