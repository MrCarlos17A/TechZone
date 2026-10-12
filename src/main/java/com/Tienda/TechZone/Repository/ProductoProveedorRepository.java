package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.ProductoProveedor;
import com.Tienda.TechZone.Model.ProductoProveedor.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Id> {
}
