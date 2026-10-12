package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.ProductoImagen;
import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Repository.ProductoImagenRepository;
import com.Tienda.TechZone.Repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoImagenService {

    private final ProductoImagenRepository productoImagenRepository;
    private final ProductoRepository productoRepository;

    public List<ProductoImagen> ListarImagenesPorProducto(Long idProducto) {
        if (idProducto == null) {
            return productoImagenRepository.findAll();
        }
        return productoImagenRepository.findByProductoIdProducto(idProducto);
    }

    public ProductoImagen AgregarImagen(ProductoImagen imagen) {
        validarImagen(imagen);
        Producto producto = productoRepository.findById(imagen.getProducto().getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        imagen.setProducto(producto);
        return productoImagenRepository.save(imagen);
    }

    public ProductoImagen ActualizarImagen(Long id, ProductoImagen imagen) {
        ProductoImagen existente = productoImagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con id: " + id));
        existente.setUrlImagen(imagen.getUrlImagen());
        existente.setEsPrincipal(imagen.getEsPrincipal());
        return productoImagenRepository.save(existente);
    }

    public void EliminarImagen(Long id) {
        productoImagenRepository.deleteById(id);
    }

    public ProductoImagen ObtenerImagenPorId(Long id) {
        return productoImagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con id: " + id));
    }

    private void validarImagen(ProductoImagen imagen) {
        if (imagen.getUrlImagen() == null || imagen.getUrlImagen().isBlank()) {
            throw new RuntimeException("La URL de la imagen es obligatoria");
        }
        if (imagen.getProducto() == null || imagen.getProducto().getIdProducto() == null) {
            throw new RuntimeException("El producto es obligatorio");
        }
    }
}
