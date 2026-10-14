package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Repository.VentaRepository;
import com.Tienda.TechZone.Service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Modulo de SOLO CONSULTA. El alta de clientes no existe a mano:
 * la ficha la crea el sistema al confirmar la primera compra del usuario.
 */
@Controller
@RequestMapping("/admin/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final VentaRepository ventaRepository;

    @GetMapping
    public String ListarClientes(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Cliente> clientes = clienteService.ListarClientes();
        if (q != null && !q.isBlank()) {
            String t = q.trim().toLowerCase();
            clientes = clientes.stream().filter(c -> {
                if (c.getPersona() == null) return false;
                return (c.getPersona().getNombres() != null && c.getPersona().getNombres().toLowerCase().contains(t))
                        || (c.getPersona().getApellidoPaterno() != null && c.getPersona().getApellidoPaterno().toLowerCase().contains(t))
                        || (c.getPersona().getApellidoMaterno() != null && c.getPersona().getApellidoMaterno().toLowerCase().contains(t))
                        || (c.getPersona().getNumeroDocumento() != null && c.getPersona().getNumeroDocumento().contains(t));
            }).toList();
        }
        model.addAttribute("clientes", clientes);
        model.addAttribute("q", q);
        return "admin/clientes/listar";
    }

    @GetMapping("/{id}")
    public String VerCliente(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.ObtenerClientePorId(id);
        List<Venta> compras = ventaRepository.findByClienteIdCliente(id);

        BigDecimal totalCompletadas = BigDecimal.ZERO;
        long completadas = 0;
        for (Venta v : compras) {
            if ("COMPLETADA".equals(v.getEstado())) {
                completadas++;
                if (v.getTotal() != null) totalCompletadas = totalCompletadas.add(v.getTotal());
            }
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("compras", compras);
        model.addAttribute("nCompras", compras.size());
        model.addAttribute("nCompletadas", completadas);
        model.addAttribute("totalCompletadas", totalCompletadas);
        return "admin/clientes/detalle";
    }
}
