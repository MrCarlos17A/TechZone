package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Carrito;
import com.Tienda.TechZone.Service.CarritoService;
import com.Tienda.TechZone.Service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/carritos")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;
    private final ClienteService clienteService;

    @GetMapping
    public String ListarCarritos(Model model) {
        model.addAttribute("carritos", carritoService.ListarCarritos());
        return "admin/carritos/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("carrito", new Carrito());
        model.addAttribute("clientes", clienteService.ListarClientes());
        return "admin/carritos/formulario";
    }

    @PostMapping("/guardar")
    public String CrearCarrito(@ModelAttribute Carrito carrito, RedirectAttributes flash) {
        try {
            carritoService.CrearCarrito(carrito);
            flash.addFlashAttribute("exito", "Carrito creado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear el carrito: " + e.getMessage());
        }
        return "redirect:/admin/carritos";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarCarrito(@PathVariable Long id, RedirectAttributes flash) {
        try {
            carritoService.EliminarCarrito(id);
            flash.addFlashAttribute("exito", "Carrito eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el carrito: " + e.getMessage());
        }
        return "redirect:/admin/carritos";
    }
}
