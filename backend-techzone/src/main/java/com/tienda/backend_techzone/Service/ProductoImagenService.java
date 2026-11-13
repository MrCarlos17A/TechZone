package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.ProductoImagen;
import com.tienda.backend_techzone.Model.Producto;
import com.tienda.backend_techzone.Repository.ProductoImagenRepository;
import com.tienda.backend_techzone.Repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoImagenService {

    private static final Logger log = LoggerFactory.getLogger(ProductoImagenService.class);

    private final ProductoImagenRepository productoImagenRepository;
    private final ProductoRepository productoRepository;
    private final SupabaseStorageService supabaseStorageService;

    public ProductoImagenService(ProductoImagenRepository productoImagenRepository,
                                 ProductoRepository productoRepository,
                                 SupabaseStorageService supabaseStorageService) {
        this.productoImagenRepository = productoImagenRepository;
        this.productoRepository = productoRepository;
        this.supabaseStorageService = supabaseStorageService;
    }

    public List<ProductoImagen> ListarImagenesPorProducto(Long idProducto) {
        if (idProducto == null) {
            return productoImagenRepository.findAllConProducto();
        }
        return productoImagenRepository.findByProductoIdProductoOrderByOrdenAsc(idProducto);
    }

    public List<ProductoImagen> listarPorProducto(Long idProducto) {
        return ListarImagenesPorProducto(idProducto);
    }

    public ProductoImagen crearImagen(ProductoImagen imagen) {
        return AgregarImagen(imagen);
    }

    public void eliminarImagen(Long id) {
        EliminarImagen(id);
    }

    @Transactional
    public ProductoImagen AgregarImagen(ProductoImagen imagen) {
        validarImagen(imagen);
        Producto producto = productoRepository.findById(imagen.getProducto().getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        imagen.setProducto(producto);

        if (Boolean.TRUE.equals(imagen.getEsPrincipal())) {
            desmarcarOtrasPrincipales(producto.getIdProducto());
        }

        return productoImagenRepository.save(imagen);
    }

    @Transactional
    public ProductoImagen ActualizarImagen(Long id, ProductoImagen imagen) {
        ProductoImagen existente = ObtenerImagenPorId(id);
        if (imagen.getUrlImagen() == null || imagen.getUrlImagen().isBlank()) {
            throw new RuntimeException("La URL de la imagen es obligatoria");
        }
        existente.setUrlImagen(imagen.getUrlImagen());
        if (Boolean.TRUE.equals(imagen.getEsPrincipal()) && !Boolean.TRUE.equals(existente.getEsPrincipal())) {
            desmarcarOtrasPrincipales(existente.getProducto().getIdProducto());
        }
        existente.setEsPrincipal(imagen.getEsPrincipal());
        return productoImagenRepository.save(existente);
    }

    @Transactional
    public ProductoImagen actualizarCompleto(Long id, Long idProducto, String url, Boolean esPrincipal, Integer orden, String etiqueta) {
        ProductoImagen existente = ObtenerImagenPorId(id);
        if (idProducto != null) {
            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + idProducto));
            existente.setProducto(producto);
        }
        if (url != null && !url.isBlank()) {
            existente.setUrlImagen(url.trim());
        }
        if (Boolean.TRUE.equals(esPrincipal)) {
            if (existente.getProducto() != null) {
                desmarcarOtrasPrincipales(existente.getProducto().getIdProducto());
            }
            existente.setEsPrincipal(true);
        } else if (esPrincipal != null) {
            existente.setEsPrincipal(esPrincipal);
        }
        if (orden != null) {
            existente.setOrden(orden);
        }
        if (etiqueta != null && !etiqueta.isBlank()) {
            existente.setEtiqueta(etiqueta.trim());
        }
        return productoImagenRepository.save(existente);
    }

    public ProductoImagen actualizarCompleto(Long id, Long idProducto, String url, Boolean esPrincipal) {
        return actualizarCompleto(id, idProducto, url, esPrincipal, null, null);
    }

    @Transactional
    public ProductoImagen establecerComoPrincipal(Long idImagen) {
        ProductoImagen existente = ObtenerImagenPorId(idImagen);
        if (existente.getProducto() != null) {
            desmarcarOtrasPrincipales(existente.getProducto().getIdProducto());
        }
        existente.setEsPrincipal(true);
        return productoImagenRepository.save(existente);
    }

    private void desmarcarOtrasPrincipales(Long idProducto) {
        List<ProductoImagen> actuales = productoImagenRepository.findByProductoIdProducto(idProducto);
        for (ProductoImagen p : actuales) {
            if (Boolean.TRUE.equals(p.getEsPrincipal())) {
                p.setEsPrincipal(false);
                productoImagenRepository.save(p);
            }
        }
    }

    @Transactional
    public void EliminarImagen(Long id) {
        ProductoImagen existente = productoImagenRepository.findById(id).orElse(null);
        if (existente != null) {
            try {
                supabaseStorageService.EliminarArchivo(existente.getUrlImagen());
            } catch (Exception e) {
                log.warn("No se pudo eliminar archivo fisico de Supabase para imagen {}: {}", id, e.getMessage());
            }
            productoImagenRepository.deleteById(id);
        }
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
