package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.ProductoProveedor;
import com.Tienda.TechZone.Model.ProductoProveedor.Id;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Id> {

    List<ProductoProveedor> findById_IdProducto(Long idProducto);

    List<ProductoProveedor> findById_IdProveedor(Long idProveedor);
}
