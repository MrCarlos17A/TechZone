package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.ProductoProveedor;
import com.tienda.backend_techzone.Model.ProductoProveedor.Id;
import com.tienda.backend_techzone.Repository.ProductoProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoProveedorService {

    private final ProductoProveedorRepository productoProveedorRepository;

    public List<ProductoProveedor> ListarPorProducto(Long idProducto) {
        if (idProducto == null) {
            return productoProveedorRepository.findAll();
        }
        return productoProveedorRepository.findById_IdProducto(idProducto);
    }

    public List<ProductoProveedor> ListarPorProveedor(Long idProveedor) {
        if (idProveedor == null) {
            return productoProveedorRepository.findAll();
        }
        return productoProveedorRepository.findById_IdProveedor(idProveedor);
    }

    public ProductoProveedor CrearRelacion(ProductoProveedor productoProveedor) {
        if (productoProveedor.getProducto() == null || productoProveedor.getProducto().getIdProducto() == null) {
            throw new RuntimeException("El producto es obligatorio");
        }
        if (productoProveedor.getProveedor() == null || productoProveedor.getProveedor().getIdProveedor() == null) {
            throw new RuntimeException("El proveedor es obligatorio");
        }
        if (productoProveedor.getPrecioCompra() == null) {
            throw new RuntimeException("El precio de compra es obligatorio");
        }
        if (productoProveedor.getPrecioCompra().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio de compra debe ser mayor a 0");
        }
        if (productoProveedor.getTiempoPedidoDias() != null && productoProveedor.getTiempoPedidoDias() < 0) {
            throw new RuntimeException("El tiempo de pedido no puede ser negativo");
        }
        Id id = new Id();
        id.setIdProducto(productoProveedor.getProducto().getIdProducto());
        id.setIdProveedor(productoProveedor.getProveedor().getIdProveedor());
        productoProveedor.setId(id);
        return productoProveedorRepository.save(productoProveedor);
    }

    public void EliminarRelacion(Long idProducto, Long idProveedor) {
        Id id = new Id();
        id.setIdProducto(idProducto);
        id.setIdProveedor(idProveedor);
        productoProveedorRepository.deleteById(id);
    }
}
