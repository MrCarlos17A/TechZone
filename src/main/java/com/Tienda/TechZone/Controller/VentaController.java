package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Model.Empleado;
import com.Tienda.TechZone.Model.MedioPago;
import com.Tienda.TechZone.Model.Pago;
import com.Tienda.TechZone.Repository.VentaRepository;
import com.Tienda.TechZone.Repository.ClienteRepository;
import com.Tienda.TechZone.Repository.EmpleadoRepository;
import com.Tienda.TechZone.Repository.MedioPagoRepository;
import com.Tienda.TechZone.Repository.PagoRepository;
import com.Tienda.TechZone.Service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/ventas")
public class VentaController {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final VentaService ventaService;
    private final PagoRepository pagoRepository;
    private final MedioPagoRepository medioPagoRepository;

    @GetMapping
    public String listar(@RequestParam(required = false) String estado,
            @RequestParam(required = false) String q,
            Model model) {
        String est = (estado == null || estado.isBlank()) ? null : estado.trim();
        String busqueda = (q == null || q.isBlank()) ? null : q.trim();

        List<Venta> ventas = ventaRepository.buscar(est, busqueda);

        model.addAttribute("ventas", ventas);
        model.addAttribute("estadoActual", est);
        model.addAttribute("q", busqueda == null ? "" : busqueda);

        List<String> estados = java.util.List.of("PENDIENTE", "COMPLETADA", "CANCELADA");
        java.util.Map<String, Long> conteos = new java.util.LinkedHashMap<>();
        for (String e : estados) {
            conteos.put(e, ventaRepository.countByEstado(e));
        }
        model.addAttribute("estadosDisponibles", estados);
        model.addAttribute("conteosEstados", conteos);
        model.addAttribute("totalVentas", ventaRepository.count());
        model.addAttribute("totalFiltrado", ventas.size());
        return "admin/ventas/listar";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        model.addAttribute("venta", venta);
        return "admin/ventas/detalle";
    }

    @GetMapping("/crear")
    public String crearForm(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll());
        model.addAttribute("empleados", empleadoRepository.findAll());
        model.addAttribute("mediosPago", medioPagoRepository.findAllByVigenteTrue());
        return "admin/ventas/crear";
    }

    /**
     * Venta registrada por el admin = venta hecha en la tienda, por lo que
     * nace COMPLETADA con su pago CONFIRMADO y ya actualiza al cliente.
     */
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Venta venta,
            @RequestParam("idCliente") Long idCliente,
            @RequestParam("idEmpleado") Long idEmpleado,
            @RequestParam("idMedioPago") Long idMedioPago,
            org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        try {
            Cliente cliente = clienteRepository.findById(idCliente)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Empleado empleado = empleadoRepository.findById(idEmpleado)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
            MedioPago medioPago = medioPagoRepository.findById(idMedioPago)
                    .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado"));

            if (venta.getSubtotal() == null || venta.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("El subtotal debe ser mayor a 0");
            }
            BigDecimal subtotal = venta.getSubtotal().setScale(2, RoundingMode.HALF_UP);
            BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(igv);

            venta.setCliente(cliente);
            venta.setEmpleado(empleado);
            venta.setEstado("COMPLETADA");
            venta.setSubtotal(subtotal);
            venta.setIgv(igv);
            venta.setTotal(total);
            if (venta.getFechaVenta() == null) {
                venta.setFechaVenta(LocalDateTime.now());
            }
            venta = ventaRepository.save(venta);

            pagoRepository.save(Pago.builder()
                    .venta(venta)
                    .medioPago(medioPago)
                    .montoPago(total)
                    .fechaPago(LocalDateTime.now())
                    .codigoOperacion("VENTA-ADMIN-" + venta.getIdVenta())
                    .estadoPago("CONFIRMADO")
                    .build());

            BigDecimal gastadoPrevio = cliente.getTotalGastado() != null ? cliente.getTotalGastado() : BigDecimal.ZERO;
            cliente.setTotalGastado(gastadoPrevio.add(total));
            cliente.setFechaUltimaCompra(venta.getFechaVenta());
            clienteRepository.save(cliente);

            flash.addFlashAttribute("exito", "Venta #" + venta.getIdVenta() + " registrada y confirmada");
            return "redirect:/admin/ventas/" + venta.getIdVenta();
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo registrar la venta: " + e.getMessage());
            return "redirect:/admin/ventas/crear";
        }
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        try {
            ventaService.ConfirmarVenta(id);
            flash.addFlashAttribute("exito", "Venta #" + id + " confirmada. Pago CONFIRMADO y cliente actualizado.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo confirmar la venta: " + e.getMessage());
        }
        return "redirect:/admin/ventas";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        try {
            ventaService.CancelarVenta(id);
            flash.addFlashAttribute("exito", "Venta #" + id + " cancelada. Pago RECHAZADO y stock devuelto.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo cancelar la venta: " + e.getMessage());
        }
        return "redirect:/admin/ventas";
    }
}
