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
    public String ListarProductos(@RequestParam(value = "q", required = false) String q,
                                  @RequestParam(value = "marca", required = false) Long idMarca,
                                  @RequestParam(value = "categoria", required = false) Long idCategoria,
                                  Model model) {
        java.util.List<Producto> productos = productoService.ListarProductos();
        boolean hayFiltro = (q != null && !q.isBlank()) || idMarca != null || idCategoria != null;
        if (hayFiltro) {
            productos = productos.stream()
                    .filter(p -> {
                        if (q != null && !q.isBlank()) {
                            String t = q.trim().toLowerCase();
                            boolean coincide = (p.getNombre() != null && p.getNombre().toLowerCase().contains(t))
                                    || (p.getCodigoSku() != null && p.getCodigoSku().toLowerCase().contains(t))
                                    || (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(t))
                                    || (p.getMarca() != null && p.getMarca().getNombre() != null
                                            && p.getMarca().getNombre().toLowerCase().contains(t));
                            if (!coincide) return false;
                        }
                        if (idMarca != null && (p.getMarca() == null
                                || !idMarca.equals(p.getMarca().getIdMarca()))) return false;
                        if (idCategoria != null && (p.getCategoria() == null
                                || !idCategoria.equals(p.getCategoria().getIdCategoria()))) return false;
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
        model.addAttribute("productos", productos);
        model.addAttribute("marcas", marcaService.ListarMarcas());
        model.addAttribute("categorias", categoriaService.ListarCategorias());
        model.addAttribute("q", q);
        model.addAttribute("marcaSel", idMarca);
        model.addAttribute("categoriaSel", idCategoria);
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
