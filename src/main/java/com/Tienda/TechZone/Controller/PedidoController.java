package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Pedido;
import com.Tienda.TechZone.Model.DetallePedido;
import com.Tienda.TechZone.Service.PedidoService;
import com.Tienda.TechZone.Service.DetallePedidoService;
import com.Tienda.TechZone.Service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final DetallePedidoService detallePedidoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String ListarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.ListarPedidos());
        return "admin/pedidos/listar";
    }

    @GetMapping("/detalle/{id}")
    public String DetallePedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.ObtenerPedidoPorId(id);
        List<DetallePedido> detalles = detallePedidoService.ListarDetallesPorPedido(id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("detalles", detalles);
        return "admin/pedidos/detalle";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", pedidoService.ObtenerPedidoPorId(id));
        model.addAttribute("usuarios", usuarioService.ListarUsuarios());
        return "admin/pedidos/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarPedido(@PathVariable Long id, @ModelAttribute Pedido pedido, RedirectAttributes flash) {
        try {
            Pedido existente = pedidoService.ObtenerPedidoPorId(id);
            if (existente.getEstado() == Pedido.EstadoPedido.ENTREGADO
                    || existente.getEstado() == Pedido.EstadoPedido.CANCELADO) {
                flash.addFlashAttribute("error", "No se puede modificar un pedido ENTREGADO o CANCELADO");
                return "redirect:/admin/pedidos";
            }
            pedidoService.ActualizarPedido(id, pedido);
            flash.addFlashAttribute("exito", "Pedido actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar el pedido: " + e.getMessage());
        }
        return "redirect:/admin/pedidos";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarPedido(@PathVariable Long id, RedirectAttributes flash) {
        try {
            List<DetallePedido> detalles = detallePedidoService.ListarDetallesPorPedido(id);
            for (DetallePedido detalle : detalles) {
                detallePedidoService.EliminarDetallePedido(detalle.getIdDetalle());
            }
            pedidoService.EliminarPedido(id);
            flash.addFlashAttribute("exito", "Pedido eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el pedido: " + e.getMessage());
        }
        return "redirect:/admin/pedidos";
    }
}
