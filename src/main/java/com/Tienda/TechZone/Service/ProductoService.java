package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    public List<Producto> ListarProductos() {
        return productoRepository.findAll();
    }

    public Producto CrearProducto(Producto producto) {
        validarPrecioStock(producto);
        return productoRepository.save(producto);
    }

    public Producto ActualizarProducto(Long id, Producto producto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        existente.setNombre(producto.getNombre());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPrecio(producto.getPrecio());
        existente.setStock(producto.getStock());
        existente.setImagen(producto.getImagen());
        existente.setEstadoProducto(producto.getEstadoProducto());
        existente.setMarca(producto.getMarca());
        existente.setCategoria(producto.getCategoria());
        validarPrecioStock(existente);
        return productoRepository.save(existente);
    }

    public void EliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    public Producto ObtenerProductoPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    public List<Producto> BuscarProductos(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return productoRepository.findAll();
        }
        return productoRepository.buscarPorTexto(texto.trim());
    }

    public List<Producto> ListarProductosPorCategoria(Long idCategoria) {
        return productoRepository.findByCategoriaIdCategoria(idCategoria);
    }

    public List<Producto> ListarProductosPorMarca(Long idMarca) {
        return productoRepository.findByMarcaIdMarca(idMarca);
    }

    private void validarPrecioStock(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del producto es obligatorio");
        }
        if (producto.getMarca() == null) {
            throw new RuntimeException("Selecciona una marca para el producto");
        }
        if (producto.getCategoria() == null) {
            throw new RuntimeException("Selecciona una categoria para el producto");
        }
        if (producto.getPrecio() == null) {
            throw new RuntimeException("El precio es obligatorio");
        }
        if (producto.getPrecio().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El precio no puede ser negativo");
        }
        producto.setPrecio(producto.getPrecio().setScale(2, java.math.RoundingMode.HALF_UP));
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }
    }
}
