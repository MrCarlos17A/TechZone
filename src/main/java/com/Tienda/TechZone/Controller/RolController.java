package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Rol;
import com.Tienda.TechZone.Service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public String ListarRoles(Model model) {
        model.addAttribute("roles", rolService.ListarRoles());
        return "admin/roles/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("rol", new Rol());
        return "admin/roles/formulario";
    }

    @PostMapping("/guardar")
    public String CrearRol(@ModelAttribute Rol rol, RedirectAttributes flash) {
        try {
            rolService.CrearRol(rol);
            flash.addFlashAttribute("exito", "Rol creado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear el rol: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Integer id, Model model) {
        model.addAttribute("rol", rolService.ObtenerRolPorId(id));
        return "admin/roles/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarRol(@PathVariable Integer id, @ModelAttribute Rol rol, RedirectAttributes flash) {
        try {
            rolService.ActualizarRol(id, rol);
            flash.addFlashAttribute("exito", "Rol actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar el rol: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarRol(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            rolService.EliminarRol(id);
            flash.addFlashAttribute("exito", "Rol eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el rol porque tiene usuarios asociados.");
        }
        return "redirect:/admin/roles";
    }
}
