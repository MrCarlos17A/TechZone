package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Pedido;
import com.Tienda.TechZone.Repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public List<Pedido> ListarPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido CrearPedido(Pedido pedido) {
        if (pedido.getTotal() == null) {
            throw new RuntimeException("El total es obligatorio");
        }
        if (pedido.getTotal().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El total no puede ser negativo");
        }
        pedido.setTotal(pedido.getTotal().setScale(2, java.math.RoundingMode.HALF_UP));
        return pedidoRepository.save(pedido);
    }

    public Pedido ActualizarPedido(Long id, Pedido pedido) {
        Pedido existente = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
        existente.setEstado(pedido.getEstado());
        return pedidoRepository.save(existente);
    }

    public void EliminarPedido(Long id) {
        pedidoRepository.deleteById(id);
    }

    public Pedido ObtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
    }

    public List<Pedido> ListarPedidosPorUsuario(Long idUsuario) {
        return pedidoRepository.findByUsuarioIdUsuario(idUsuario);
    }
}
