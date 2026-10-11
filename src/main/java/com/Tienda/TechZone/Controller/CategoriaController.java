package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Categoria;
import com.Tienda.TechZone.Service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public String ListarCategorias(Model model) {
        model.addAttribute("categorias", categoriaService.ListarCategorias());
        return "admin/categorias/listar";
    }

    @GetMapping("/nueva")
    public String FormularioNueva(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "admin/categorias/formulario";
    }

    @PostMapping("/guardar")
    public String CrearCategoria(@ModelAttribute Categoria categoria, RedirectAttributes flash) {
        try {
            categoriaService.CrearCategoria(categoria);
            flash.addFlashAttribute("exito", "Categoria creada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear la categoria: " + e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", categoriaService.ObtenerCategoriaPorId(id));
        return "admin/categorias/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarCategoria(@PathVariable Long id, @ModelAttribute Categoria categoria, RedirectAttributes flash) {
        try {
            categoriaService.ActualizarCategoria(id, categoria);
            flash.addFlashAttribute("exito", "Categoria actualizada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar la categoria: " + e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarCategoria(@PathVariable Long id, RedirectAttributes flash) {
        try {
            categoriaService.EliminarCategoria(id);
            flash.addFlashAttribute("exito", "Categoria eliminada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar la categoria porque tiene productos asociados.");
        }
        return "redirect:/admin/categorias";
    }
}
