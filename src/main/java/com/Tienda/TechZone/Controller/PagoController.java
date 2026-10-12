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
    public String ver(@PathVariable Integer id, Model model) {
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
                          @RequestParam("idVenta") Integer idVenta,
                          @RequestParam("idMedioPago") Integer idMedioPago) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        MedioPago medioPago = medioPagoRepository.findById(idMedioPago)
                .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado"));
        pago.setVenta(venta);
        pago.setMedioPago(medioPago);
        pagoRepository.save(pago);
        return "redirect:/admin/pagos";
    }
}