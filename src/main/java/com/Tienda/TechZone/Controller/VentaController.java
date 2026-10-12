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
            Model model) {
        List<Venta> ventas = estado != null && !estado.isBlank()
                ? ventaRepository.findByEstado(estado)
                : ventaRepository.findAll();
        model.addAttribute("ventas", ventas);
        model.addAttribute("estadosDisponibles",
                java.util.stream.Stream.of("PENDIENTE", "COMPLETADA", "CANCELADA")
                        .collect(java.util.stream.Collectors.toList()));
        return "admin/ventas/listar";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Integer id, Model model) {
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
            Model model) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        venta.setCliente(cliente);
        venta.setEmpleado(empleado);
        ventaRepository.save(venta);
        return "redirect:/admin/ventas/" + venta.getIdVenta();
    }
}
