package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Proveedor;
import com.Tienda.TechZone.Model.TipoDocumento;
import com.Tienda.TechZone.Repository.ProveedorRepository;
import com.Tienda.TechZone.Repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/proveedores")
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    @GetMapping
    public String listar(@RequestParam(value = "q", required = false) String q, Model model) {
        java.util.List<Proveedor> proveedores = proveedorRepository.findAll();
        if (q != null && !q.isBlank()) {
            String t = q.trim().toLowerCase();
            proveedores = proveedores.stream().filter(p -> {
                return (p.getRazonSocial() != null && p.getRazonSocial().toLowerCase().contains(t))
                        || (p.getNombreEncargado() != null && p.getNombreEncargado().toLowerCase().contains(t))
                        || (p.getCorreo() != null && p.getCorreo().toLowerCase().contains(t))
                        || (p.getNumeroDocumento() != null && p.getNumeroDocumento().contains(t))
                        || (p.getTelefonoContacto() != null && p.getTelefonoContacto().contains(t));
            }).collect(java.util.stream.Collectors.toList());
        }
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("q", q);
        model.addAttribute("tiposDocumento", tipoDocumentoRepository.findAllByVigenteTrue());
        return "admin/proveedores/listar";
    }

    @GetMapping("/nuevo")
    public String formulario(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("tiposDocumento", tipoDocumentoRepository.findAll());
        return "admin/proveedores/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Proveedor proveedor,
            @RequestParam(value = "idTipoDocumento", required = false) Long idTipoDocumento,
            RedirectAttributes flash) {
        try {
            validar(proveedor);
            if (idTipoDocumento != null) {
                TipoDocumento td = tipoDocumentoRepository.findById(idTipoDocumento)
                        .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
                proveedor.setTipoDocumento(td);
            }
            proveedorRepository.save(proveedor);
            flash.addFlashAttribute("exito", "Proveedor guardado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo guardar: " + e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("proveedor", proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado")));
        model.addAttribute("tiposDocumento", tipoDocumentoRepository.findAll());
        return "admin/proveedores/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Proveedor proveedor,
            @RequestParam(value = "idTipoDocumento", required = false) Long idTipoDocumento,
            RedirectAttributes flash) {
        try {
            Proveedor existente = proveedorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
            validar(proveedor);
            existente.setRazonSocial(proveedor.getRazonSocial().trim());
            existente.setNumeroDocumento(proveedor.getNumeroDocumento());
            existente.setTelefonoContacto(proveedor.getTelefonoContacto());
            existente.setCorreo(proveedor.getCorreo() != null ? proveedor.getCorreo().trim() : null);
            existente.setNombreEncargado(proveedor.getNombreEncargado() != null ? proveedor.getNombreEncargado().trim() : null);
            if (proveedor.getVigente() != null) {
                existente.setVigente(proveedor.getVigente());
            }
            if (idTipoDocumento != null) {
                tipoDocumentoRepository.findById(idTipoDocumento).ifPresent(existente::setTipoDocumento);
            }
            proveedorRepository.save(existente);
            flash.addFlashAttribute("exito", "Proveedor actualizado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar: " + e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            proveedorRepository.deleteById(id);
            flash.addFlashAttribute("exito", "Proveedor eliminado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se puede eliminar: el proveedor tiene registros asociados");
        }
        return "redirect:/admin/proveedores";
    }

    private void validar(Proveedor proveedor) {
        if (proveedor.getRazonSocial() == null || proveedor.getRazonSocial().isBlank()) {
            throw new RuntimeException("La razon social es obligatoria");
        }
        if (proveedor.getCorreo() != null && !proveedor.getCorreo().isBlank()
                && !proveedor.getCorreo().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new RuntimeException("El correo no es valido");
        }
        if (proveedor.getNombreEncargado() != null && proveedor.getNombreEncargado().length() > 150) {
            throw new RuntimeException("El nombre del encargado no puede superar 150 caracteres");
        }
    }
}
