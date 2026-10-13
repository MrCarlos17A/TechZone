package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.ProductoProveedor;
import com.Tienda.TechZone.Service.ProductoProveedorService;
import com.Tienda.TechZone.Service.ProductoService;
import com.Tienda.TechZone.Service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos-proveedor")
@RequiredArgsConstructor
public class ProductoProveedorController {

    private final ProductoProveedorService productoProveedorService;
    private final ProductoService productoService;
    private final ProveedorService proveedorService;

    @GetMapping
    public String ListarPorProducto(@RequestParam(required = false) Long idProducto, Model model) {
        if (idProducto != null) {
            model.addAttribute("relaciones", productoProveedorService.ListarPorProducto(idProducto));
            model.addAttribute("idProducto", idProducto);
        } else {
            model.addAttribute("relaciones", productoProveedorService.ListarPorProducto(null));
        }
        model.addAttribute("productos", productoService.ListarProductos());
        model.addAttribute("proveedores", proveedorService.ListarProveedores());
        return "admin/productos-proveedor/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("relacion", new ProductoProveedor());
        model.addAttribute("productos", productoService.ListarProductos());
        model.addAttribute("proveedores", proveedorService.ListarProveedores());
        return "admin/productos-proveedor/formulario";
    }

    @PostMapping("/guardar")
    public String CrearRelacion(@ModelAttribute ProductoProveedor relacion, RedirectAttributes flash) {
        try {
            productoProveedorService.CrearRelacion(relacion);
            flash.addFlashAttribute("exito", "Relacion creada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear la relacion: " + e.getMessage());
        }
        return "redirect:/admin/productos-proveedor";
    }

    @GetMapping("/eliminar/{idProducto}/{idProveedor}")
    public String EliminarRelacion(@PathVariable Long idProducto, @PathVariable Long idProveedor,
            RedirectAttributes flash) {
        try {
            productoProveedorService.EliminarRelacion(idProducto, idProveedor);
            flash.addFlashAttribute("exito", "Relacion eliminada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar la relacion: " + e.getMessage());
        }
        return "redirect:/admin/productos-proveedor";
    }
}
