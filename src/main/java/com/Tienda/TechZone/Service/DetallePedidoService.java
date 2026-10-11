package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.DetallePedido;
import com.Tienda.TechZone.Repository.DetallePedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;

    public List<DetallePedido> ListarDetallesPedido() {
        return detallePedidoRepository.findAll();
    }

    public DetallePedido CrearDetallePedido(DetallePedido detalle) {
        validarDetalle(detalle);
        return detallePedidoRepository.save(detalle);
    }

    public DetallePedido ActualizarDetallePedido(Long id, DetallePedido detalle) {
        DetallePedido existente = detallePedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de pedido no encontrado con id: " + id));
        existente.setPedido(detalle.getPedido());
        existente.setProducto(detalle.getProducto());
        existente.setCantidad(detalle.getCantidad());
        existente.setPrecioUnitario(detalle.getPrecioUnitario());
        existente.setSubtotal(detalle.getSubtotal());
        validarDetalle(existente);
        return detallePedidoRepository.save(existente);
    }

    public void EliminarDetallePedido(Long id) {
        detallePedidoRepository.deleteById(id);
    }

    public DetallePedido ObtenerDetallePedidoPorId(Long id) {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle de pedido no encontrado con id: " + id));
    }

    public List<DetallePedido> ListarDetallesPorPedido(Long idPedido) {
        return detallePedidoRepository.findByPedidoIdPedido(idPedido);
    }

    private void validarDetalle(DetallePedido detalle) {
        if (detalle.getCantidad() == null || detalle.getCantidad() < 1) {
            throw new RuntimeException("La cantidad debe ser al menos 1");
        }
        if (detalle.getPrecioUnitario() == null
                || detalle.getPrecioUnitario().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El precio unitario no puede ser negativo");
        }
        detalle.setPrecioUnitario(detalle.getPrecioUnitario().setScale(2, java.math.RoundingMode.HALF_UP));
        if (detalle.getSubtotal() == null
                || detalle.getSubtotal().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El subtotal no puede ser negativo");
        }
        detalle.setSubtotal(detalle.getSubtotal().setScale(2, java.math.RoundingMode.HALF_UP));
    }
}
