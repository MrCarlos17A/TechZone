package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.DetallePedido;
import com.Tienda.TechZone.Model.Pedido;
import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import com.Tienda.TechZone.Service.DetallePedidoService;
import com.Tienda.TechZone.Service.PedidoService;
import com.Tienda.TechZone.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CompraController {

    private final UsuarioRepository usuarioRepository;
    private final ProductoService productoService;
    private final PedidoService pedidoService;
    private final DetallePedidoService detallePedidoService;

    public record ItemCompra(Long idProducto, Integer cantidad) {
    }

    @PostMapping("/checkout")
    @Transactional
    public Map<String, Object> Checkout(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody List<ItemCompra> items) {
        Map<String, Object> respuesta = new LinkedHashMap<>();

        if (userDetails == null) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Debes iniciar sesion para comprar");
            return respuesta;
        }

        if (items == null || items.isEmpty()) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "El carrito esta vacio");
            return respuesta;
        }

        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername()).orElse(null);
        if (usuario == null) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Usuario no encontrado");
            return respuesta;
        }

        try {
            BigDecimal total = BigDecimal.ZERO;
            List<DetallePedido> detalles = new ArrayList<>();

            for (ItemCompra item : items) {
                if (item.cantidad() == null || item.cantidad() <= 0) {
                    continue;
                }
                Producto producto = productoService.ObtenerProductoPorId(item.idProducto());
                if (producto.getStock() == null || producto.getStock() < item.cantidad()) {
                    respuesta.put("ok", false);
                    respuesta.put("mensaje", "Stock insuficiente para: " + producto.getNombre());
                    return respuesta;
                }
                if (producto.getPrecio() == null
                        || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                    respuesta.put("ok", false);
                    respuesta.put("mensaje", "Precio invalido para: " + producto.getNombre());
                    return respuesta;
                }
                BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.cantidad()));
                total = total.add(subtotal);
                detalles.add(DetallePedido.builder()
                        .producto(producto)
                        .cantidad(item.cantidad())
                        .precioUnitario(producto.getPrecio())
                        .subtotal(subtotal)
                        .build());
            }

            if (detalles.isEmpty()) {
                respuesta.put("ok", false);
                respuesta.put("mensaje", "No hay items validos en el carrito");
                return respuesta;
            }

            if (total.compareTo(BigDecimal.ZERO) <= 0) {
                respuesta.put("ok", false);
                respuesta.put("mensaje", "El total de la compra debe ser mayor a cero");
                return respuesta;
            }

            Pedido pedido = pedidoService.CrearPedido(Pedido.builder()
                    .usuario(usuario)
                    .estado(Pedido.EstadoPedido.PENDIENTE)
                    .total(total)
                    .build());

            for (DetallePedido detalle : detalles) {
                detalle.setPedido(pedido);
                detallePedidoService.CrearDetallePedido(detalle);
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() - detalle.getCantidad());
                productoService.ActualizarProducto(producto.getIdProducto(), producto);
            }

            respuesta.put("ok", true);
            respuesta.put("idPedido", pedido.getIdPedido());
            respuesta.put("total", total);
            return respuesta;
        } catch (RuntimeException e) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", e.getMessage());
            return respuesta;
        }
    }
}
