package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Persona;
import com.Tienda.TechZone.Service.PersonaService;
import com.Tienda.TechZone.Service.TipoDocumentoService;
import com.Tienda.TechZone.Service.UbigeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/personas")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;
    private final TipoDocumentoService tipoDocumentoService;
    private final UbigeoService ubigeoService;

    @GetMapping
    public String ListarPersonas(Model model) {
        model.addAttribute("personas", personaService.ListarPersonas());
        return "admin/personas/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("persona", new Persona());
        model.addAttribute("tiposDocumento", tipoDocumentoService.ListarTiposDocumento());
        model.addAttribute("ubigeos", ubigeoService.ListarUbigeos());
        return "admin/personas/formulario";
    }

    @PostMapping("/guardar")
    public String CrearPersona(@ModelAttribute Persona persona, RedirectAttributes flash) {
        try {
            personaService.CrearPersona(persona);
            flash.addFlashAttribute("exito", "Persona creada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear la persona: " + e.getMessage());
        }
        return "redirect:/admin/personas";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("persona", personaService.ObtenerPersonaPorId(id));
        model.addAttribute("tiposDocumento", tipoDocumentoService.ListarTiposDocumento());
        model.addAttribute("ubigeos", ubigeoService.ListarUbigeos());
        return "admin/personas/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarPersona(@PathVariable Long id, @ModelAttribute Persona persona, RedirectAttributes flash) {
        try {
            personaService.ActualizarPersona(id, persona);
            flash.addFlashAttribute("exito", "Persona actualizada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar la persona: " + e.getMessage());
        }
        return "redirect:/admin/personas";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarPersona(@PathVariable Long id, RedirectAttributes flash) {
        try {
            personaService.EliminarPersona(id);
            flash.addFlashAttribute("exito", "Persona eliminada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar la persona: " + e.getMessage());
        }
        return "redirect:/admin/personas";
    }
}
