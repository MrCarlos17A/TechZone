package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.DetalleCarrito;
import com.Tienda.TechZone.Service.CarritoService;
import com.Tienda.TechZone.Service.DetalleCarritoService;
import com.Tienda.TechZone.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/detalles-carrito")
@RequiredArgsConstructor
public class DetalleCarritoController {

    private final DetalleCarritoService detalleCarritoService;
    private final CarritoService carritoService;
    private final ProductoService productoService;

    @GetMapping
    public String ListarPorCarrito(@RequestParam(required = false) Long idCarrito, Model model) {
        if (idCarrito != null) {
            model.addAttribute("detalles", detalleCarritoService.ListarPorCarrito(idCarrito));
            model.addAttribute("idCarrito", idCarrito);
        } else {
            model.addAttribute("detalles", detalleCarritoService.ListarPorCarrito(null));
        }
        model.addAttribute("carritos", carritoService.ListarCarritos());
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/detalles-carrito/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("detalle", new DetalleCarrito());
        model.addAttribute("carritos", carritoService.ListarCarritos());
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/detalles-carrito/formulario";
    }

    @PostMapping("/guardar")
    public String CrearDetalle(@ModelAttribute DetalleCarrito detalle, RedirectAttributes flash) {
        try {
            detalleCarritoService.AgregarAlCarrito(detalle);
            flash.addFlashAttribute("exito", "Detalle agregado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo agregar el detalle: " + e.getMessage());
        }
        return "redirect:/admin/detalles-carrito";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarDetalle(@PathVariable Long id, RedirectAttributes flash) {
        try {
            detalleCarritoService.EliminarDetalle(id);
            flash.addFlashAttribute("exito", "Detalle eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el detalle: " + e.getMessage());
        }
        return "redirect:/admin/detalles-carrito";
    }
}
