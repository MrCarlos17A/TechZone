package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {
    List<ProductoImagen> findByProductoIdProducto(Long idProducto);
}
