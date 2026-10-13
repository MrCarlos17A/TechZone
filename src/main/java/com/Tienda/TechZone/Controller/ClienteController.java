package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Service.ClienteService;
import com.Tienda.TechZone.Service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final PersonaService personaService;

    @GetMapping
    public String ListarClientes(@RequestParam(value = "q", required = false) String q, Model model) {
        java.util.List<Cliente> clientes = clienteService.ListarClientes();
        if (q != null && !q.isBlank()) {
            String t = q.trim().toLowerCase();
            clientes = clientes.stream().filter(c -> {
                if (c.getPersona() == null) return false;
                return (c.getPersona().getNombres() != null && c.getPersona().getNombres().toLowerCase().contains(t))
                        || (c.getPersona().getApellidoPaterno() != null && c.getPersona().getApellidoPaterno().toLowerCase().contains(t))
                        || (c.getPersona().getApellidoMaterno() != null && c.getPersona().getApellidoMaterno().toLowerCase().contains(t))
                        || (c.getPersona().getNumeroDocumento() != null && c.getPersona().getNumeroDocumento().contains(t));
            }).collect(java.util.stream.Collectors.toList());
        }
        model.addAttribute("clientes", clientes);
        model.addAttribute("q", q);
        return "admin/clientes/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("personas", personaService.ListarPersonas());
        return "admin/clientes/formulario";
    }

    @PostMapping("/guardar")
    public String CrearCliente(@ModelAttribute Cliente cliente, RedirectAttributes flash) {
        try {
            clienteService.CrearCliente(cliente);
            flash.addFlashAttribute("exito", "Cliente creado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear el cliente: " + e.getMessage());
        }
        return "redirect:/admin/clientes";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.ObtenerClientePorId(id));
        model.addAttribute("personas", personaService.ListarPersonas());
        return "admin/clientes/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarCliente(@PathVariable Long id, @ModelAttribute Cliente cliente, RedirectAttributes flash) {
        try {
            clienteService.ActualizarCliente(id, cliente);
            flash.addFlashAttribute("exito", "Cliente actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar el cliente: " + e.getMessage());
        }
        return "redirect:/admin/clientes";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarCliente(@PathVariable Long id, RedirectAttributes flash) {
        try {
            clienteService.EliminarCliente(id);
            flash.addFlashAttribute("exito", "Cliente eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el cliente: " + e.getMessage());
        }
        return "redirect:/admin/clientes";
    }
}
