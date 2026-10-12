package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
    List<Pago> findByVentaIdVenta(Integer idVenta);
    List<Pago> findByMedioPagoIdMedioPago(Integer idMedioPago);
}
