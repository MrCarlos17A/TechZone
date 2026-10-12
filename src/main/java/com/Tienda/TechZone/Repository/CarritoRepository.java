package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
}