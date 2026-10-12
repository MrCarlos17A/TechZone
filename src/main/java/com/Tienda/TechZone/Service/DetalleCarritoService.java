package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.DetalleCarrito;
import com.Tienda.TechZone.Repository.DetalleCarritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DetalleCarritoService {

    private final DetalleCarritoRepository detalleCarritoRepository;

    public List<DetalleCarrito> ListarPorCarrito(Long idCarrito) {
        return detalleCarritoRepository.findByCarritoIdCarrito(idCarrito);
    }

    public BigDecimal CalcularTotalCarrito(Long idCarrito) {
        return ListarPorCarrito(idCarrito).stream()
                .map(dc -> dc.getProducto().getPrecioVenta()
                        .multiply(BigDecimal.valueOf(dc.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public DetalleCarrito AgregarAlCarrito(DetalleCarrito detalle) {
        validarDetalle(detalle);
        return detalleCarritoRepository.save(detalle);
    }

    public DetalleCarrito ActualizarDetalle(Long id, DetalleCarrito detalle) {
        DetalleCarrito existente = detalleCarritoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado con id: " + id));
        existente.setCantidad(detalle.getCantidad());
        return detalleCarritoRepository.save(existente);
    }

    public void EliminarDetalle(Long id) {
        detalleCarritoRepository.deleteById(id);
    }

    public void VaciarCarrito(Long idCarrito) {
        detalleCarritoRepository.deleteByCarritoIdCarrito(idCarrito);
    }

    private void validarDetalle(DetalleCarrito detalle) {
        if (detalle.getCarrito() == null || detalle.getCarrito().getIdCarrito() == null) {
            throw new RuntimeException("El carrito es obligatorio");
        }
        if (detalle.getProducto() == null || detalle.getProducto().getIdProducto() == null) {
            throw new RuntimeException("El producto es obligatorio");
        }
        if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }
    }
}
