package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.TipoDocumento;
import com.Tienda.TechZone.Service.TipoDocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tipos-documento")
@RequiredArgsConstructor
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    @GetMapping
    public String ListarTiposDocumento(Model model) {
        model.addAttribute("tiposDocumento", tipoDocumentoService.ListarTiposDocumento());
        return "admin/tipos-documento/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("tipoDocumento", new TipoDocumento());
        return "admin/tipos-documento/formulario";
    }

    @PostMapping("/guardar")
    public String CrearTipoDocumento(@ModelAttribute TipoDocumento tipoDocumento, RedirectAttributes flash) {
        try {
            tipoDocumentoService.CrearTipoDocumento(tipoDocumento);
            flash.addFlashAttribute("exito", "Tipo de documento creado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear el tipo de documento: " + e.getMessage());
        }
        return "redirect:/admin/tipos-documento";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Integer id, Model model) {
        model.addAttribute("tipoDocumento", tipoDocumentoService.ObtenerTipoDocumentoPorId(id));
        return "admin/tipos-documento/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarTipoDocumento(@PathVariable Integer id, @ModelAttribute TipoDocumento tipoDocumento,
            RedirectAttributes flash) {
        try {
            tipoDocumentoService.ActualizarTipoDocumento(id, tipoDocumento);
            flash.addFlashAttribute("exito", "Tipo de documento actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar el tipo de documento: " + e.getMessage());
        }
        return "redirect:/admin/tipos-documento";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarTipoDocumento(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            tipoDocumentoService.EliminarTipoDocumento(id);
            flash.addFlashAttribute("exito", "Tipo de documento eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el tipo de documento: " + e.getMessage());
        }
        return "redirect:/admin/tipos-documento";
    }
}
