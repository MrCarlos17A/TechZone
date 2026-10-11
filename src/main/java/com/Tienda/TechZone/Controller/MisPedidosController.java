package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.DetallePedido;
import com.Tienda.TechZone.Model.Pedido;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.PedidoRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import com.Tienda.TechZone.Service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MisPedidosController {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DetallePedidoService detallePedidoService;

    public static class PedidoVista {
        private final Long id;
        private final String fecha;
        private final String estado;
        private final String total;
        private final List<String> items;

        public PedidoVista(Long id, String fecha, String estado, String total, List<String> items) {
            this.id = id;
            this.fecha = fecha;
            this.estado = estado;
            this.total = total;
            this.items = items;
        }

        public Long getId() { return id; }
        public String getFecha() { return fecha; }
        public String getEstado() { return estado; }
        public String getTotal() { return total; }
        public List<String> getItems() { return items; }
    }

    @GetMapping("/mis-pedidos")
    @Transactional(readOnly = true)
    public String MisPedidos(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername()).orElseThrow();
        List<Pedido> pedidos = pedidoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<PedidoVista> vista = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            List<String> items = new ArrayList<>();
            for (DetallePedido detalle : detallePedidoService.ListarDetallesPorPedido(pedido.getIdPedido())) {
                items.add(detalle.getProducto().getNombre()
                        + " x" + detalle.getCantidad()
                        + " - S/ " + detalle.getSubtotal());
            }
            vista.add(new PedidoVista(
                    pedido.getIdPedido(),
                    pedido.getFechaPedido() != null ? pedido.getFechaPedido().format(formato) : "-",
                    pedido.getEstado() != null ? pedido.getEstado().name() : "-",
                    "S/ " + pedido.getTotal(),
                    items));
        }
        model.addAttribute("pedidos", vista);
        return "mis-pedidos";
    }
}
