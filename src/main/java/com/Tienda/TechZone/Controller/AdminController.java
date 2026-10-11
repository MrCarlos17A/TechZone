package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Pedido;
import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductoService productoService;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final PedidoService pedidoService;

    @GetMapping
    public String Dashboard(Model model) {
        List<Producto> productos = productoService.ListarProductos();
        List<Pedido> pedidos = pedidoService.ListarPedidos();

        long totalProductos = productos.size();
        long totalMarcas = marcaService.ListarMarcas().size();
        long totalCategorias = categoriaService.ListarCategorias().size();
        long totalUsuarios = usuarioService.ListarUsuarios().size();
        long totalRoles = rolService.ListarRoles().size();
        long totalPedidos = pedidos.size();
        long pedidosPendientes = pedidos.stream()
                .filter(p -> p.getEstado() == Pedido.EstadoPedido.PENDIENTE)
                .count();

        List<Pedido> ultimosPedidos = pedidos.stream()
                .sorted(Comparator.comparing(Pedido::getIdPedido).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<Producto> stockBajo = productos.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 5)
                .sorted(Comparator.comparing(Producto::getStock))
                .limit(5)
                .collect(Collectors.toList());

        Map<java.time.LocalDate, Long> conteoPorDia = pedidos.stream()
                .filter(p -> p.getFechaPedido() != null)
                .collect(Collectors.groupingBy(p -> p.getFechaPedido().toLocalDate(), Collectors.counting()));

        LocalDate hoy = LocalDate.now();
        List<String> etiquetasPedidos = new ArrayList<>();
        List<Long> valoresPedidos = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoy.minusDays(i);
            etiquetasPedidos.add(dia.getDayOfMonth() + "/" + dia.getMonthValue());
            valoresPedidos.add(conteoPorDia.getOrDefault(dia, 0L));
        }

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalMarcas", totalMarcas);
        model.addAttribute("totalCategorias", totalCategorias);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalRoles", totalRoles);
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("pedidosPendientes", pedidosPendientes);
        model.addAttribute("ultimosPedidos", ultimosPedidos);
        model.addAttribute("stockBajo", stockBajo);
        model.addAttribute("etiquetasPedidos", etiquetasPedidos);
        model.addAttribute("valoresPedidos", valoresPedidos);
        return "admin/index";
    }

    @GetMapping("/acceso-denegado")
    public String AccesoDenegado() {
        return "admin/acceso-denegado";
    }
}
