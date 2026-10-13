package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Marca;
import com.Tienda.TechZone.Service.MarcaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    @GetMapping
    public String ListarMarcas(@RequestParam(value = "q", required = false) String q, Model model) {
        java.util.List<Marca> marcas = marcaService.ListarMarcas();
        if (q != null && !q.isBlank()) {
            String t = q.trim().toLowerCase();
            marcas = marcas.stream()
                    .filter(m -> m.getNombre() != null && m.getNombre().toLowerCase().contains(t))
                    .collect(java.util.stream.Collectors.toList());
        }
        model.addAttribute("marcas", marcas);
        model.addAttribute("q", q);
        return "admin/marcas/listar";
    }

    @GetMapping("/nueva")
    public String FormularioNueva(Model model) {
        model.addAttribute("marca", new Marca());
        return "admin/marcas/formulario";
    }

    @PostMapping("/guardar")
    public String CrearMarca(@ModelAttribute Marca marca, RedirectAttributes flash) {
        try {
            marcaService.CrearMarca(marca);
            flash.addFlashAttribute("exito", "Marca creada correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo crear la marca: " + e.getMessage());
        }
        return "redirect:/admin/marcas";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("marca", marcaService.ObtenerMarcaPorId(id));
        return "admin/marcas/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarMarca(@PathVariable Long id, @ModelAttribute Marca marca, RedirectAttributes flash) {
        try {
            marcaService.ActualizarMarca(id, marca);
            flash.addFlashAttribute("exito", "Marca actualizada correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo actualizar la marca: " + e.getMessage());
        }
        return "redirect:/admin/marcas";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarMarca(@PathVariable Long id, RedirectAttributes flash) {
        try {
            marcaService.EliminarMarca(id);
            flash.addFlashAttribute("exito", "Marca eliminada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar la marca porque tiene productos asociados.");
        }
        return "redirect:/admin/marcas";
    }
}
