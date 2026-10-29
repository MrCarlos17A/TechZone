package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {
    List<ProductoImagen> findByProductoIdProducto(Long idProducto);
    List<ProductoImagen> findByProductoIdProductoIn(java.util.Collection<Long> idProductos);

    @Query("SELECT pi FROM ProductoImagen pi LEFT JOIN FETCH pi.producto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.marca ORDER BY pi.idProductoImagen ASC")
    List<ProductoImagen> findAllConProducto();
}
