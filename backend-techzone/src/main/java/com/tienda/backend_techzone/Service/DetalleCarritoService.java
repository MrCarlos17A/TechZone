package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.DetalleCarrito;
import com.tienda.backend_techzone.Repository.DetalleCarritoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DetalleCarritoService {

    private final DetalleCarritoRepository detalleCarritoRepository;

    public DetalleCarritoService(DetalleCarritoRepository detalleCarritoRepository) {
        this.detalleCarritoRepository = detalleCarritoRepository;
    }

    public List<DetalleCarrito> ListarPorCarrito(Long idCarrito) {
        if (idCarrito == null) {
            return detalleCarritoRepository.findAll();
        }
        return detalleCarritoRepository.findByCarritoIdCarrito(idCarrito);
    }

    public List<DetalleCarrito> listarPorCarrito(Long idCarrito) {
        return ListarPorCarrito(idCarrito);
    }

    public BigDecimal CalcularTotalCarrito(Long idCarrito) {
        List<DetalleCarrito> lista = ListarPorCarrito(idCarrito);
        if (lista == null || lista.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return lista.stream()
                .filter(dc -> dc != null && dc.getProducto() != null && dc.getProducto().getPrecioVenta() != null && dc.getCantidad() != null)
                .map(dc -> dc.getProducto().getPrecioVenta().multiply(BigDecimal.valueOf(dc.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularTotalCarrito(Long idCarrito) {
        return CalcularTotalCarrito(idCarrito);
    }

    public DetalleCarrito AgregarAlCarrito(DetalleCarrito detalle) {
        validarDetalle(detalle);
        return detalleCarritoRepository.save(detalle);
    }

    public DetalleCarrito agregarAlCarrito(DetalleCarrito detalle) {
        return AgregarAlCarrito(detalle);
    }

    public DetalleCarrito ActualizarDetalle(Long id, DetalleCarrito detalle) {
        if (id == null) {
            throw new IllegalArgumentException("El id del detalle es obligatorio");
        }
        DetalleCarrito existente = detalleCarritoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado con id: " + id));
        if (detalle == null || detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        existente.setCantidad(detalle.getCantidad());
        return detalleCarritoRepository.save(existente);
    }

    public DetalleCarrito actualizarDetalle(Long id, DetalleCarrito detalle) {
        return ActualizarDetalle(id, detalle);
    }

    public void EliminarDetalle(Long id) {
        if (id != null) {
            detalleCarritoRepository.deleteById(id);
        }
    }

    public void eliminarDetalle(Long id) {
        EliminarDetalle(id);
    }

    public DetalleCarrito actualizarCantidad(Long id, Integer cantidad) {
        if (id == null) {
            throw new IllegalArgumentException("El id del detalle es obligatorio");
        }
        DetalleCarrito existente = detalleCarritoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado con id: " + id));
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        existente.setCantidad(cantidad);
        return detalleCarritoRepository.save(existente);
    }

    public DetalleCarrito ActualizarCantidad(Long id, Integer cantidad) {
        return actualizarCantidad(id, cantidad);
    }

    public void VaciarCarrito(Long idCarrito) {
        if (idCarrito != null) {
            detalleCarritoRepository.deleteByCarritoIdCarrito(idCarrito);
        }
    }

    public void vaciarCarrito(Long idCarrito) {
        VaciarCarrito(idCarrito);
    }

    private void validarDetalle(DetalleCarrito detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }
        if (detalle.getCarrito() == null || detalle.getCarrito().getIdCarrito() == null) {
            throw new IllegalArgumentException("El carrito es obligatorio");
        }
        if (detalle.getProducto() == null || detalle.getProducto().getIdProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }
}
