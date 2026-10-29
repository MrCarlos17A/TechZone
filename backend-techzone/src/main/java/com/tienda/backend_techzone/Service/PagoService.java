package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.MedioPago;
import com.tienda.backend_techzone.Model.Pago;
import com.tienda.backend_techzone.Model.Venta;
import com.tienda.backend_techzone.Repository.MedioPagoRepository;
import com.tienda.backend_techzone.Repository.PagoRepository;
import com.tienda.backend_techzone.Repository.VentaRepository;
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

    @org.springframework.transaction.annotation.Transactional
    public List<Pago> ListarPagos() {
        List<Pago> pagos = pagoRepository.findAllConRelaciones();
        List<Venta> ventas = ventaRepository.findAll();

        java.util.Set<Long> ventasConPago = new java.util.HashSet<>();
        for (Pago p : pagos) {
            if (p.getVenta() != null && p.getVenta().getIdVenta() != null) {
                ventasConPago.add(p.getVenta().getIdVenta());
            }
        }

        MedioPago medioDefault = medioPagoRepository.findAll().stream().findFirst().orElse(null);
        boolean nuevosCreados = false;

        for (Venta v : ventas) {
            if (!ventasConPago.contains(v.getIdVenta())) {
                Pago nuevoPago = Pago.builder()
                        .venta(v)
                        .medioPago(medioDefault)
                        .montoPago(v.getTotal() != null ? v.getTotal() : java.math.BigDecimal.ZERO)
                        .fechaPago(v.getFechaVenta() != null ? v.getFechaVenta() : java.time.LocalDateTime.now())
                        .codigoOperacion("OP-" + String.format("%06d", v.getIdVenta()))
                        .estadoPago("ENTREGADO".equals(v.getEstado()) || "COMPLETADA".equals(v.getEstado()) ? "CONFIRMADO" : "PENDIENTE")
                        .build();
                pagoRepository.save(nuevoPago);
                nuevosCreados = true;
            }
        }

        if (nuevosCreados) {
            return pagoRepository.findAllConRelaciones();
        }
        return pagos;
    }

    public Pago ObtenerPagoPorId(Long id) {
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
}
