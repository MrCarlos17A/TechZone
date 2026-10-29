package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findByVentaIdVenta(Long idVenta);

    @org.springframework.data.jpa.repository.Query("SELECT dv FROM DetalleVenta dv LEFT JOIN FETCH dv.venta LEFT JOIN FETCH dv.producto ORDER BY dv.idDetalleVenta DESC")
    List<DetalleVenta> findAllConRelaciones();
}
