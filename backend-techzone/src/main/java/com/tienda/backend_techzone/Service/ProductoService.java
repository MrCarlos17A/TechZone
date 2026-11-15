package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Producto;
import com.tienda.backend_techzone.Repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@lombok.extern.slf4j.Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> ListarProductos() {
        return new java.util.ArrayList<>(new java.util.LinkedHashSet<>(productoRepository.findAllConRelaciones()));
    }

    public List<Producto> listarProductos() {
        return ListarProductos();
    }

    // Solo productos vigentes (para la tienda publica)
    @Transactional(readOnly = true)
    public List<Producto> ListarProductosActivos() {
        return new java.util.ArrayList<>(new java.util.LinkedHashSet<>(productoRepository.findByVigenteTrue()));
    }

    public List<Producto> listarProductosActivos() {
        return ListarProductosActivos();
    }

    public List<Producto> listarProductosPorCategoria(Long idCategoria) {
        return ListarProductosActivosPorCategoria(idCategoria);
    }

    public List<Producto> listarProductosActivosPorCategoria(Long idCategoria) {
        return ListarProductosActivosPorCategoria(idCategoria);
    }

    public List<Producto> buscarProductos(String texto) {
        return BuscarProductos(texto);
    }

    public List<Producto> listarProductosPorMarca(Long idMarca) {
        return ListarProductosPorMarca(idMarca);
    }

    public Producto obtenerProductoPorId(Long id) {
        return ObtenerProductoPorId(id);
    }

    public Producto crearProducto(Producto producto) {
        return CrearProducto(producto);
    }

    public Producto actualizarProducto(Long id, Producto producto) {
        return ActualizarProducto(id, producto);
    }

    public void eliminarProducto(Long id) {
        EliminarProducto(id);
    }

    public List<Producto> ListarProductosActivosPorCategoria(Long idCategoria) {
        return productoRepository.findByCategoriaIdCategoria(idCategoria).stream()
                .filter(p -> p.getVigente() == null || p.getVigente())
                .collect(java.util.stream.Collectors.toList());
    }

    public Producto CrearProducto(Producto producto) {
        validarPrecioStock(producto);
        if (producto.getVigente() == null) {
            producto.setVigente(true);
        }
        if (producto.getNovedad() == null) {
            producto.setNovedad(false);
        }
        if (producto.getFechaIngreso() == null) {
            producto.setFechaIngreso(java.time.LocalDateTime.now());
        }
        producto.setCodigoSku(generarSku(producto));
        return productoRepository.save(producto);
    }

    public Producto ActualizarProducto(Long id, Producto producto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        if (producto.getNombre() != null)
            existente.setNombre(producto.getNombre());
        if (producto.getDescripcion() != null)
            existente.setDescripcion(producto.getDescripcion());
        if (producto.getPrecioVenta() != null)
            existente.setPrecioVenta(producto.getPrecioVenta());
        if (producto.getStock() != null)
            existente.setStock(producto.getStock());
        if (producto.getMarca() != null)
            existente.setMarca(producto.getMarca());
        if (producto.getCategoria() != null)
            existente.setCategoria(producto.getCategoria());
        if (producto.getCaracteristicas() != null)
            existente.setCaracteristicas(producto.getCaracteristicas());
        if (producto.getVigente() != null) {
            existente.setVigente(producto.getVigente());
        }
        if (producto.getDestacado() != null) {
            existente.setDestacado(producto.getDestacado());
        }
        if (producto.getNovedad() != null) {
            existente.setNovedad(producto.getNovedad());
        }
        validarPrecioStock(existente);
        return productoRepository.save(existente);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductosDestacados() {
        try {
            List<Producto> raw = productoRepository.findDestacados();
            if (raw != null && !raw.isEmpty()) {
                List<Producto> destacados = new java.util.ArrayList<>(new java.util.LinkedHashSet<>(raw));
                destacados.sort(java.util.Comparator.comparing(Producto::getIdProducto));
                return destacados.stream().limit(9).collect(java.util.stream.Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error al obtener destacados: {}", e.getMessage(), e);
        }
        return listarProductosActivos().stream().limit(9).collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public Producto toggleDestacado(Long idProducto) {
        Producto p = productoRepository.findByIdConRelaciones(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + idProducto));
        boolean nuevoEstado = p.getDestacado() == null || !p.getDestacado();
        if (nuevoEstado) {
            long total = productoRepository.countDestacados();
            if (total >= 9) {
                throw new IllegalStateException(
                        "Límite alcanzado: Ya existen 9 productos destacados en el Inicio. Debes desmarcar uno o usar la sustitución rápida.");
            }
        }
        p.setDestacado(nuevoEstado);
        return productoRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductosNovedades() {
        try {
            List<Producto> raw = productoRepository.findNovedades();
            if (raw != null && !raw.isEmpty()) {
                return new java.util.ArrayList<>(new java.util.LinkedHashSet<>(raw));
            }
        } catch (Exception e) {
            log.error("Error al obtener novedades: {}", e.getMessage(), e);
        }
        return listarProductosActivos();
    }

    @Transactional
    public Producto toggleNovedad(Long idProducto) {
        Producto p = productoRepository.findByIdConRelaciones(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + idProducto));
        boolean nuevoEstado = p.getNovedad() == null || !p.getNovedad();
        p.setNovedad(nuevoEstado);
        return productoRepository.save(p);
    }

    @Transactional
    public List<Producto> sustituirDestacado(Long idSaliente, Long idEntrante) {
        if (idSaliente == null || idEntrante == null) {
            throw new IllegalArgumentException("Los identificadores de productos no pueden ser nulos");
        }
        if (idSaliente.equals(idEntrante)) {
            throw new IllegalArgumentException("El producto saliente y el entrante no pueden ser el mismo");
        }
        Producto saliente = productoRepository.findByIdConRelaciones(idSaliente)
                .orElseThrow(() -> new RuntimeException("Producto saliente no encontrado con id: " + idSaliente));
        Producto entrante = productoRepository.findByIdConRelaciones(idEntrante)
                .orElseThrow(() -> new RuntimeException("Producto entrante no encontrado con id: " + idEntrante));

        saliente.setDestacado(false);
        entrante.setDestacado(true);

        Producto sGuardado = productoRepository.save(saliente);
        Producto eGuardado = productoRepository.save(entrante);

        return List.of(sGuardado, eGuardado);
    }

    public void EliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        producto.setVigente(false);
        productoRepository.save(producto);
    }

    public Producto ObtenerProductoPorId(Long id) {
        return productoRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    public List<Producto> BuscarProductos(String texto) {
        List<Producto> resultado;
        if (texto == null || texto.trim().isEmpty()) {
            resultado = productoRepository.findByVigenteTrue();
        } else {
            resultado = productoRepository.buscarPorTexto(texto.trim());
        }
        return new java.util.ArrayList<>(new java.util.LinkedHashSet<>(resultado));
    }

    public List<Producto> ListarProductosPorCategoria(Long idCategoria) {
        return new java.util.ArrayList<>(
                new java.util.LinkedHashSet<>(productoRepository.findByCategoriaIdCategoria(idCategoria)));
    }

    public List<Producto> ListarProductosPorMarca(Long idMarca) {
        return new java.util.ArrayList<>(new java.util.LinkedHashSet<>(productoRepository.findByMarcaIdMarca(idMarca)));
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
                ? producto.getMarca().getIdMarca()
                : 0;
        long idCategoria = producto.getCategoria() != null && producto.getCategoria().getIdCategoria() != null
                ? producto.getCategoria().getIdCategoria()
                : 0;
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
