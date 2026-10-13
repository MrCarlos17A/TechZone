package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByVentaIdVenta(Long idVenta);
    List<Pago> findByMedioPagoIdMedioPago(Long idMedioPago);
}
