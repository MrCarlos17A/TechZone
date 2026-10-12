package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    List<Venta> findByClienteIdCliente(Long idCliente);
    List<Venta> findByEstado(String estado);
}
