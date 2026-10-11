package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.DetallePedido;
import com.Tienda.TechZone.Service.DetallePedidoService;
import com.Tienda.TechZone.Service.PedidoService;
import com.Tienda.TechZone.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/detalles-pedido")
@RequiredArgsConstructor
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;
    private final PedidoService pedidoService;
    private final ProductoService productoService;

    @GetMapping
    public String ListarDetallesPedido(Model model) {
        model.addAttribute("detalles", detallePedidoService.ListarDetallesPedido());
        return "admin/detalles-pedido/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("detalle", new DetallePedido());
        model.addAttribute("pedidos", pedidoService.ListarPedidos());
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/detalles-pedido/formulario";
    }

    @PostMapping("/guardar")
    public String CrearDetallePedido(@ModelAttribute DetallePedido detalle, RedirectAttributes flash) {
        flash.addFlashAttribute("error", "Los detalles de pedido son de solo lectura y se generan al comprar");
        return "redirect:/admin/detalles-pedido";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("detalle", detallePedidoService.ObtenerDetallePedidoPorId(id));
        model.addAttribute("pedidos", pedidoService.ListarPedidos());
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/detalles-pedido/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarDetallePedido(@PathVariable Long id, @ModelAttribute DetallePedido detalle, RedirectAttributes flash) {
        flash.addFlashAttribute("error", "Los detalles de pedido son de solo lectura y no se pueden modificar");
        return "redirect:/admin/detalles-pedido";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarDetallePedido(@PathVariable Long id, RedirectAttributes flash) {
        flash.addFlashAttribute("error", "Los detalles de pedido son de solo lectura. Elimina el pedido completo si corresponde");
        return "redirect:/admin/detalles-pedido";
    }
}
