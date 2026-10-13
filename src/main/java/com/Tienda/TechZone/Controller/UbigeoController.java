package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Ubigeo;
import com.Tienda.TechZone.Service.UbigeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/ubigeos")
@RequiredArgsConstructor
public class UbigeoController {

    private final UbigeoService ubigeoService;

    @GetMapping
    public String ListarUbigeos(Model model) {
        model.addAttribute("ubigeos", ubigeoService.ListarUbigeos());
        return "admin/ubigeos/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("ubigeo", new Ubigeo());
        return "admin/ubigeos/formulario";
    }

    @PostMapping("/guardar")
    public String CrearUbigeo(@ModelAttribute Ubigeo ubigeo, RedirectAttributes flash) {
        try {
            ubigeoService.CrearUbigeo(ubigeo);
            flash.addFlashAttribute("exito", "Ubigeo creado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear el ubigeo: " + e.getMessage());
        }
        return "redirect:/admin/ubigeos";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("ubigeo", ubigeoService.ObtenerUbigeoPorId(id));
        return "admin/ubigeos/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarUbigeo(@PathVariable Long id, @ModelAttribute Ubigeo ubigeo, RedirectAttributes flash) {
        try {
            ubigeoService.ActualizarUbigeo(id, ubigeo);
            flash.addFlashAttribute("exito", "Ubigeo actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar el ubigeo: " + e.getMessage());
        }
        return "redirect:/admin/ubigeos";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarUbigeo(@PathVariable Long id, RedirectAttributes flash) {
        try {
            ubigeoService.EliminarUbigeo(id);
            flash.addFlashAttribute("exito", "Ubigeo eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el ubigeo: " + e.getMessage());
        }
        return "redirect:/admin/ubigeos";
    }
}
