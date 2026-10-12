package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.DetalleCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
    List<DetalleCarrito> findByCarritoIdCarrito(Long idCarrito);
    void deleteByCarritoIdCarrito(Long idCarrito);
}
