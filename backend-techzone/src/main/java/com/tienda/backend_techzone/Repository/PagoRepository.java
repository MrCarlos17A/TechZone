package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Pago p LEFT JOIN FETCH p.venta LEFT JOIN FETCH p.medioPago")
    List<Pago> findAllConRelaciones();
    List<Pago> findByVentaIdVenta(Long idVenta);
    List<Pago> findByMedioPagoIdMedioPago(Long idMedioPago);
}
