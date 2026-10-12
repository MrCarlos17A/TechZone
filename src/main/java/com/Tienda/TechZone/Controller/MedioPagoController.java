package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.MedioPago;
import com.Tienda.TechZone.Repository.MedioPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String guardar(@ModelAttribute MedioPago medioPago) {
        if (medioPago.getIdMedioPago() == null) {
            Integer siguiente = medioPagoRepository.findAll().stream()
                    .map(MedioPago::getIdMedioPago)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
            medioPago.setIdMedioPago(siguiente);
        }
        medioPagoRepository.save(medioPago);
        return "redirect:/admin/medios-pago";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("medioPago", medioPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado")));
        return "admin/medios-pago/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        medioPagoRepository.deleteById(id);
        return "redirect:/admin/medios-pago";
    }
}
