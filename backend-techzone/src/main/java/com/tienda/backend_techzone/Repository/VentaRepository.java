package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByClienteIdCliente(Long idCliente);
    List<Venta> findByEstado(String estado);

    long countByEstado(String estado);

    @Query("SELECT v FROM Venta v LEFT JOIN v.cliente c LEFT JOIN c.persona p " +
            "WHERE (:estado IS NULL OR :estado = '' OR v.estado = :estado) " +
            "AND (:q IS NULL OR :q = '' " +
            "OR LOWER(CONCAT(COALESCE(p.nombres, ''), ' ', COALESCE(p.apellidoPaterno, ''), ' ', COALESCE(p.apellidoMaterno, ''))) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(COALESCE(p.numeroDocumento, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(COALESCE(v.direccionEnvio, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR CAST(v.idVenta AS STRING) LIKE CONCAT('%', :q, '%'))")
    List<Venta> buscar(@Param("estado") String estado, @Param("q") String q);
}
