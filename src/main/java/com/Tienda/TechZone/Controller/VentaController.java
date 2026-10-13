package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Model.Empleado;
import com.Tienda.TechZone.Repository.VentaRepository;
import com.Tienda.TechZone.Repository.ClienteRepository;
import com.Tienda.TechZone.Repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/ventas")
public class VentaController {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;

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
        return "admin/ventas/crear";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Venta venta,
            @RequestParam("idCliente") Long idCliente,
            @RequestParam("idEmpleado") Long idEmpleado,
            Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        try {
            Cliente cliente = clienteRepository.findById(idCliente)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Empleado empleado = empleadoRepository.findById(idEmpleado)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
            venta.setCliente(cliente);
            venta.setEmpleado(empleado);
            if (venta.getSubtotal() != null && venta.getSubtotal().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new RuntimeException("El subtotal no puede ser negativo");
            }
            if (venta.getIgv() != null && venta.getIgv().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new RuntimeException("El IGV no puede ser negativo");
            }
            if (venta.getTotal() != null && venta.getTotal().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("El total debe ser mayor a 0");
            }
            if (venta.getEstado() != null && !venta.getEstado().isBlank()
                    && !java.util.Set.of("PENDIENTE", "COMPLETADA", "CANCELADA").contains(venta.getEstado())) {
                throw new RuntimeException("Estado no valido (PENDIENTE, COMPLETADA, CANCELADA)");
            }
            if (venta.getFechaVenta() == null) {
                venta.setFechaVenta(java.time.LocalDateTime.now());
            }
            ventaRepository.save(venta);
            return "redirect:/admin/ventas/" + venta.getIdVenta();
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo registrar la venta: " + e.getMessage());
            return "redirect:/admin/ventas/crear";
        }
    }
}
