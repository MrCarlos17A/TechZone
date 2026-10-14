package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Empleado;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final TipoDocumentoService tipoDocumentoService;
    private final UbigeoService ubigeoService;
    private final RolService rolService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String ListarEmpleados(@RequestParam(value = "q", required = false) String q, Model model) {
        java.util.List<Empleado> empleados = empleadoService.ListarEmpleados();
        if (q != null && !q.isBlank()) {
            String t = q.trim().toLowerCase();
            empleados = empleados.stream().filter(e -> {
                if (e.getPersona() == null) return false;
                return (e.getPersona().getNombres() != null && e.getPersona().getNombres().toLowerCase().contains(t))
                        || (e.getPersona().getApellidoPaterno() != null && e.getPersona().getApellidoPaterno().toLowerCase().contains(t))
                        || (e.getPersona().getApellidoMaterno() != null && e.getPersona().getApellidoMaterno().toLowerCase().contains(t))
                        || (e.getPersona().getNumeroDocumento() != null && e.getPersona().getNumeroDocumento().contains(t));
            }).collect(java.util.stream.Collectors.toList());
        }
        model.addAttribute("empleados", empleados);
        model.addAttribute("q", q);
        model.addAttribute("cuentasPorPersona", usuarioService.ListarUsuarios().stream()
                .filter(u -> u.getPersona() != null && u.getPersona().getIdPersona() != null)
                .collect(java.util.stream.Collectors.toMap(
                        u -> u.getPersona().getIdPersona(), u -> u, (a, b) -> a)));
        return "admin/empleados/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("empleado", new Empleado());
        model.addAttribute("persona", new com.Tienda.TechZone.Model.Persona());
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tiposDocumento", tipoDocumentoService.ListarTiposDocumento());
        model.addAttribute("ubigeos", ubigeoService.ListarUbigeos());
        model.addAttribute("roles", rolService.ListarRoles());
        return "admin/empleados/formulario";
    }

    @PostMapping("/guardar")
    public String CrearEmpleado(@RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidoPaterno,
            @RequestParam(required = false) String apellidoMaterno,
            @RequestParam(required = false) Long idTipoDocumento,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) Long ubigeo,
            @RequestParam String correo,
            @RequestParam String password,
            @RequestParam Long rol,
            @RequestParam(required = false) String fechaContrato,
            @RequestParam(required = false) BigDecimal salario,
            RedirectAttributes flash) {
        try {
            LocalDate fNac = (fechaNacimiento != null && !fechaNacimiento.isBlank())
                    ? LocalDate.parse(fechaNacimiento) : null;
            LocalDate fCont = (fechaContrato != null && !fechaContrato.isBlank())
                    ? LocalDate.parse(fechaContrato) : null;
            empleadoService.CrearEmpleadoCompleto(nombres, apellidoPaterno, apellidoMaterno,
                    idTipoDocumento, numeroDocumento, telefono, fNac,
                    direccion, referencia, ubigeo,
                    correo, password, rol, fCont, salario);
            flash.addFlashAttribute("exito", "Empleado creado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo crear el empleado: " + e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        Empleado empleado = empleadoService.ObtenerEmpleadoPorId(id);
        model.addAttribute("empleado", empleado);
        model.addAttribute("persona", empleado.getPersona());
        model.addAttribute("usuario", empleado.getPersona() != null
                ? usuarioService.ListarUsuarios().stream()
                        .filter(u -> u.getPersona() != null
                                && u.getPersona().getIdPersona().equals(empleado.getPersona().getIdPersona()))
                        .findFirst().orElse(new Usuario())
                : new Usuario());
        model.addAttribute("tiposDocumento", tipoDocumentoService.ListarTiposDocumento());
        model.addAttribute("ubigeos", ubigeoService.ListarUbigeos());
        model.addAttribute("roles", rolService.ListarRoles());
        return "admin/empleados/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarEmpleado(@PathVariable Long id,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidoPaterno,
            @RequestParam(required = false) String apellidoMaterno,
            @RequestParam(required = false) Long idTipoDocumento,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String referencia,
            @RequestParam(required = false) Long ubigeo,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) Long rol,
            @RequestParam(required = false) String fechaContrato,
            @RequestParam(required = false) BigDecimal salario,
            RedirectAttributes flash) {
        try {
            LocalDate fNac = (fechaNacimiento != null && !fechaNacimiento.isBlank())
                    ? LocalDate.parse(fechaNacimiento) : null;
            LocalDate fCont = (fechaContrato != null && !fechaContrato.isBlank())
                    ? LocalDate.parse(fechaContrato) : null;
            empleadoService.ActualizarEmpleadoCompleto(id, nombres, apellidoPaterno, apellidoMaterno,
                    idTipoDocumento, numeroDocumento, telefono, fNac,
                    direccion, referencia, ubigeo,
                    correo, password, rol, fCont, salario);
            flash.addFlashAttribute("exito", "Empleado actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar el empleado: " + e.getMessage());
        }
        return "redirect:/admin/empleados";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarEmpleado(@PathVariable Long id, RedirectAttributes flash) {
        try {
            empleadoService.EliminarEmpleado(id);
            flash.addFlashAttribute("exito", "Empleado eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el empleado: " + e.getMessage());
        }
        return "redirect:/admin/empleados";
    }
}
