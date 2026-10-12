package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Proveedor;
import com.Tienda.TechZone.Model.TipoDocumento;
import com.Tienda.TechZone.Repository.ProveedorRepository;
import com.Tienda.TechZone.Repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/proveedores")
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proveedores", proveedorRepository.findAll());
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
            @RequestParam("idTipoDocumento") Integer idTipoDocumento,
            Model model) {
        TipoDocumento td = tipoDocumentoRepository.findById(idTipoDocumento)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
        proveedor.setTipoDocumento(td);
        proveedorRepository.save(proveedor);
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("proveedor", proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado")));
        model.addAttribute("tiposDocumento", tipoDocumentoRepository.findAll());
        return "admin/proveedores/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, Model model) {
        try {
            proveedorRepository.deleteById(id);
        } catch (Exception e) {
            model.addAttribute("error", "No se puede eliminar: el proveedor tiene productos asociados");
            return "redirect:/admin/proveedores";
        }
        return "redirect:/admin/proveedores";
    }
}