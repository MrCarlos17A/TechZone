package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.DetalleCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
    List<DetalleCarrito> findByCarritoIdCarrito(Long idCarrito);
    void deleteByCarritoIdCarrito(Long idCarrito);
}
