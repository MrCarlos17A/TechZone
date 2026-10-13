package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Producto> ListarProductos() {
        return productoRepository.findAll();
    }

    public Producto CrearProducto(Producto producto) {
        validarPrecioStock(producto);
        if (producto.getVigente() == null) {
            producto.setVigente(true);
        }
        producto.setCodigoSku(generarSku(producto));
        return productoRepository.save(producto);
    }

    public Producto ActualizarProducto(Long id, Producto producto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        existente.setNombre(producto.getNombre());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPrecioVenta(producto.getPrecioVenta());
        existente.setStock(producto.getStock());
        existente.setMarca(producto.getMarca());
        existente.setCategoria(producto.getCategoria());
        existente.setCaracteristicas(producto.getCaracteristicas());
        if (producto.getVigente() != null) {
            existente.setVigente(producto.getVigente());
        }
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
        if (producto.getNombre().trim().length() > 255) {
            throw new RuntimeException("El nombre no puede superar 255 caracteres");
        }
        producto.setNombre(producto.getNombre().trim());
        if (producto.getDescripcion() != null && producto.getDescripcion().length() > 65000) {
            throw new RuntimeException("La descripcion es demasiado larga");
        }
        if (producto.getCaracteristicas() != null && !producto.getCaracteristicas().isBlank()) {
            String car = producto.getCaracteristicas().trim();
            if (car.startsWith("{") || car.startsWith("[")) {
                try {
                    new tools.jackson.databind.ObjectMapper().readTree(car);
                } catch (Exception e) {
                    throw new RuntimeException("Las caracteristicas deben ser JSON valido");
                }
            }
        }
        if (producto.getMarca() == null) {
            throw new RuntimeException("Selecciona una marca para el producto");
        }
        if (producto.getCategoria() == null) {
            throw new RuntimeException("Selecciona una categoria para el producto");
        }
        if (producto.getPrecioVenta() == null) {
            throw new RuntimeException("El precio de venta es obligatorio");
        }
        if (producto.getPrecioVenta().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio de venta debe ser mayor a 0");
        }
        producto.setPrecioVenta(producto.getPrecioVenta().setScale(2, java.math.RoundingMode.HALF_UP));
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }
        if (producto.getStock() > 32767) {
            throw new RuntimeException("El stock no puede superar 32767");
        }
    }

    private String generarSku(Producto producto) {
        String marcaNombre = producto.getMarca() != null ? producto.getMarca().getNombre() : "";
        String categoriaNombre = producto.getCategoria() != null ? producto.getCategoria().getNombre() : "";
        String preMarca = prefijo(marcaNombre, 2);
        String preCategoria = prefijo(categoriaNombre, 2);
        long idMarca = producto.getMarca() != null && producto.getMarca().getIdMarca() != null
                ? producto.getMarca().getIdMarca() : 0;
        long idCategoria = producto.getCategoria() != null && producto.getCategoria().getIdCategoria() != null
                ? producto.getCategoria().getIdCategoria() : 0;
        long correlativo = productoRepository.countByMarcaIdMarcaAndCategoriaIdCategoria(
                (int) idMarca, (int) idCategoria) + 1;
        String sku;
        do {
            sku = preMarca + preCategoria + String.format("%02d", correlativo);
            correlativo++;
        } while (productoRepository.existsByCodigoSku(sku));
        return sku;
    }

    private String prefijo(String texto, int longitud) {
        if (texto == null || texto.isBlank()) {
            return "XX";
        }
        String limpio = texto.replaceAll("[^a-zA-Z]", "").toUpperCase();
        if (limpio.length() >= longitud) {
            return limpio.substring(0, longitud);
        }
        return String.format("%-2s", limpio).replace(' ', 'X');
    }
}
