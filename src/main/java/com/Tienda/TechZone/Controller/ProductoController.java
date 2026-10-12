package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Service.ProductoService;
import com.Tienda.TechZone.Service.MarcaService;
import com.Tienda.TechZone.Service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;

    @GetMapping
    public String ListarProductos(Model model) {
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/productos/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("marcas", marcaService.ListarMarcas());
        model.addAttribute("categorias", categoriaService.ListarCategorias());
        return "admin/productos/formulario";
    }

    @PostMapping("/guardar")
    public String CrearProducto(@ModelAttribute Producto producto, RedirectAttributes flash) {
        try {
            productoService.CrearProducto(producto);
            flash.addFlashAttribute("exito", "Producto creado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo crear el producto: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.ObtenerProductoPorId(id));
        model.addAttribute("marcas", marcaService.ListarMarcas());
        model.addAttribute("categorias", categoriaService.ListarCategorias());
        return "admin/productos/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarProducto(@PathVariable Long id, @ModelAttribute Producto producto, RedirectAttributes flash) {
        try {
            productoService.ActualizarProducto(id, producto);
            flash.addFlashAttribute("exito", "Producto actualizado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo actualizar el producto: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarProducto(@PathVariable Long id, RedirectAttributes flash) {
        try {
            productoService.EliminarProducto(id);
            flash.addFlashAttribute("exito", "Producto eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error",
                    "No se pudo eliminar el producto porque tiene registros asociados (ventas).");
        }
        return "redirect:/admin/productos";
    }
}
