package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Pago;
import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Model.MedioPago;
import com.Tienda.TechZone.Repository.PagoRepository;
import com.Tienda.TechZone.Repository.VentaRepository;
import com.Tienda.TechZone.Repository.MedioPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/pagos")
public class PagoController {

    private final PagoRepository pagoRepository;
    private final VentaRepository ventaRepository;
    private final MedioPagoRepository medioPagoRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pagos", pagoRepository.findAll());
        model.addAttribute("mediosPago", medioPagoRepository.findAllByVigenteTrue());
        model.addAttribute("ventas", ventaRepository.findAll());
        return "admin/pagos/listar";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        model.addAttribute("pago", pago);
        return "admin/pagos/detalle";
    }

    @GetMapping("/nuevo")
    public String form(Model model) {
        model.addAttribute("venta", null);
        model.addAttribute("mediosPago", medioPagoRepository.findAllByVigenteTrue());
        return "admin/pagos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Pago pago,
                          @RequestParam("idVenta") Long idVenta,
                          @RequestParam("idMedioPago") Long idMedioPago,
                          org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        try {
            Venta venta = ventaRepository.findById(idVenta)
                    .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
            MedioPago medioPago = medioPagoRepository.findById(idMedioPago)
                    .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado"));
            pago.setVenta(venta);
            pago.setMedioPago(medioPago);
            if (pago.getMontoPago() == null) {
                throw new RuntimeException("El monto de pago es obligatorio");
            }
            if (pago.getMontoPago().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("El monto de pago debe ser mayor a 0");
            }
            if (pago.getEstadoPago() != null && !pago.getEstadoPago().isBlank()
                    && !java.util.Set.of("CONFIRMADO", "PENDIENTE", "RECHAZADO").contains(pago.getEstadoPago())) {
                throw new RuntimeException("Estado de pago no valido (CONFIRMADO, PENDIENTE, RECHAZADO)");
            }
            if (pago.getCodigoOperacion() != null && pago.getCodigoOperacion().length() > 50) {
                throw new RuntimeException("El codigo de operacion no puede superar 50 caracteres");
            }
            if (pago.getFechaPago() == null) {
                pago.setFechaPago(java.time.LocalDateTime.now());
            }
            pagoRepository.save(pago);
            flash.addFlashAttribute("exito", "Pago registrado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo registrar el pago: " + e.getMessage());
        }
        return "redirect:/admin/pagos";
    }
}