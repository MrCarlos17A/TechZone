package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Service.UsuarioService;
import com.Tienda.TechZone.Service.RolService;
import com.Tienda.TechZone.Service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final PersonaService personaService;

    @GetMapping
    public String ListarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.ListarUsuarios());
        return "admin/usuarios/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolService.ListarRoles());
        model.addAttribute("personas", personaService.ListarPersonas());
        return "admin/usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String CrearUsuario(@ModelAttribute Usuario usuario, RedirectAttributes flash) {
        try {
            usuarioService.CrearUsuario(usuario);
            flash.addFlashAttribute("exito", "Usuario creado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.ObtenerUsuarioPorId(id));
        model.addAttribute("roles", rolService.ListarRoles());
        model.addAttribute("personas", personaService.ListarPersonas());
        return "admin/usuarios/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarUsuario(@PathVariable Long id, @ModelAttribute Usuario usuario, RedirectAttributes flash) {
        try {
            usuarioService.ActualizarUsuario(id, usuario);
            flash.addFlashAttribute("exito", "Usuario actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarUsuario(@PathVariable Long id, RedirectAttributes flash) {
        try {
            Usuario usuario = usuarioService.ObtenerUsuarioPorId(id);
            usuario.setEstado("inactivo");
            usuarioService.ActualizarUsuario(id, usuario);
            flash.addFlashAttribute("exito", "Usuario deshabilitado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo deshabilitar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
