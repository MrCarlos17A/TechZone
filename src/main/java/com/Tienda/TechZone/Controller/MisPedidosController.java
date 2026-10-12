package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Model.DetalleVenta;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Repository.ClienteRepository;
import com.Tienda.TechZone.Repository.DetalleVentaRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import com.Tienda.TechZone.Repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MisPedidosController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @GetMapping("/mis-pedidos")
    public String MisPedidos(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Map<String, Object>> pedidos = new ArrayList<>();

        Cliente cliente = ObtenerCliente(userDetails);
        if (cliente != null) {
            List<Venta> ventas = ventaRepository.findByClienteIdCliente(cliente.getIdCliente());
            ventas.sort((a, b) -> Integer.compare(b.getIdVenta(), a.getIdVenta()));
            for (Venta venta : ventas) {
                pedidos.add(ConstruirPedido(venta));
            }
        }

        model.addAttribute("pedidos", pedidos);
        return "mis-pedidos";
    }

    @GetMapping("/mis-pedidos/{id}")
    public String DetallePedido(@PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Cliente cliente = ObtenerCliente(userDetails);
        if (cliente == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        }

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        if (venta.getCliente() == null || !cliente.getIdCliente().equals(venta.getCliente().getIdCliente())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }

        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaIdVenta(id);

        model.addAttribute("venta", venta);
        model.addAttribute("detalles", detalles);
        model.addAttribute("fechaVenta",
                venta.getFechaVenta() != null ? venta.getFechaVenta().format(FORMATO_FECHA) : "");
        return "mis-pedido-detalle";
    }

    private Cliente ObtenerCliente(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername()).orElse(null);
        if (usuario == null || usuario.getPersona() == null) {
            return null;
        }
        return clienteRepository.findByPersona(usuario.getPersona()).orElse(null);
    }

    private Map<String, Object> ConstruirPedido(Venta venta) {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", venta.getIdVenta());
        pedido.put("fecha",
                venta.getFechaVenta() != null ? venta.getFechaVenta().format(FORMATO_FECHA) : "");
        pedido.put("estado", venta.getEstado());
        pedido.put("total", venta.getTotal());

        List<String> items = new ArrayList<>();
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaIdVenta(venta.getIdVenta());
        for (DetalleVenta dv : detalles) {
            String nombre = dv.getProducto() != null ? dv.getProducto().getNombre() : "Producto";
            BigDecimal subtotal = dv.getSubTotalVenta() != null ? dv.getSubTotalVenta() : BigDecimal.ZERO;
            items.add(dv.getCantidad() + "x " + nombre + " — S/ " + subtotal);
        }
        pedido.put("items", items);
        return pedido;
    }
}
