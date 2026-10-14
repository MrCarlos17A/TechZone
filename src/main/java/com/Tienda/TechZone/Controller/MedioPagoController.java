package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.MedioPago;
import com.Tienda.TechZone.Repository.MedioPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/medios-pago")
public class MedioPagoController {

    private final MedioPagoRepository medioPagoRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("mediosPago", medioPagoRepository.findAll());
        return "admin/medios-pago/listar";
    }

    @GetMapping("/nuevo")
    public String formulario(Model model) {
        model.addAttribute("medioPago", new MedioPago());
        return "admin/medios-pago/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute MedioPago medioPago, RedirectAttributes flash) {
        try {
            if (medioPago.getNombre() == null || medioPago.getNombre().isBlank()) {
                throw new RuntimeException("El nombre del medio de pago es obligatorio");
            }
            medioPago.setNombre(medioPago.getNombre().trim());
            if (medioPago.getNombre().length() > 50) {
                throw new RuntimeException("El nombre no puede superar 50 caracteres");
            }
            if (medioPago.getIdMedioPago() == null) {
                Long siguiente = medioPagoRepository.findAll().stream()
                        .map(MedioPago::getIdMedioPago)
                        .max(Long::compareTo)
                        .orElse(0L) + 1;
                medioPago.setIdMedioPago(siguiente);
            }
            medioPagoRepository.save(medioPago);
            flash.addFlashAttribute("exito", "Medio de pago guardado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo guardar: " + e.getMessage());
        }
        return "redirect:/admin/medios-pago";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("medioPago", medioPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado")));
        return "admin/medios-pago/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            MedioPago medio = medioPagoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado"));
            medio.setVigente(false);
            medioPagoRepository.save(medio);
            flash.addFlashAttribute("exito", "Medio de pago desactivado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo desactivar: " + e.getMessage());
        }
        return "redirect:/admin/medios-pago";
    }
}
