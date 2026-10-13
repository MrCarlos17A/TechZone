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
    public String ListarUsuarios(@RequestParam(value = "q", required = false) String q, Model model) {
        java.util.List<Usuario> usuarios = usuarioService.ListarUsuarios();
        if (q != null && !q.isBlank()) {
            String t = q.trim().toLowerCase();
            usuarios = usuarios.stream().filter(u -> {
                boolean ok = (u.getCorreo() != null && u.getCorreo().toLowerCase().contains(t))
                        || (u.getEstado() != null && u.getEstado().toLowerCase().contains(t));
                if (!ok && u.getPersona() != null) {
                    ok = (u.getPersona().getNombres() != null && u.getPersona().getNombres().toLowerCase().contains(t))
                            || (u.getPersona().getApellidoPaterno() != null && u.getPersona().getApellidoPaterno().toLowerCase().contains(t))
                            || (u.getPersona().getApellidoMaterno() != null && u.getPersona().getApellidoMaterno().toLowerCase().contains(t));
                }
                if (!ok && u.getRol() != null) {
                    ok = u.getRol().getNombre() != null && u.getRol().getNombre().toLowerCase().contains(t);
                }
                return ok;
            }).collect(java.util.stream.Collectors.toList());
        }
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("q", q);
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
