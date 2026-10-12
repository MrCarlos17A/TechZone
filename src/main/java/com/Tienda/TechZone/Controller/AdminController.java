package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Service.*;
import com.Tienda.TechZone.Repository.VentaRepository;
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
    private final VentaRepository ventaRepository;

    @GetMapping
    public String Dashboard(Model model) {
        List<Producto> productos = productoService.ListarProductos();
        List<Venta> ventas = ventaRepository.findAll();

        long totalProductos = productos.size();
        long totalMarcas = marcaService.ListarMarcas().size();
        long totalCategorias = categoriaService.ListarCategorias().size();
        long totalUsuarios = usuarioService.ListarUsuarios().size();
        long totalRoles = rolService.ListarRoles().size();
        long totalVentas = ventas.size();
        long ventasPendientes = ventas.stream()
                .filter(v -> "PENDIENTE".equalsIgnoreCase(v.getEstado()))
                .count();

        List<Venta> ultimasVentas = ventas.stream()
                .sorted(Comparator.comparing(Venta::getIdVenta).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<Producto> stockBajo = productos.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 5)
                .sorted(Comparator.comparing(Producto::getStock))
                .limit(5)
                .collect(Collectors.toList());

        Map<LocalDate, Long> conteoPorDia = ventas.stream()
                .filter(v -> v.getFechaVenta() != null)
                .collect(Collectors.groupingBy(v -> v.getFechaVenta().toLocalDate(), Collectors.counting()));

        LocalDate hoy = LocalDate.now();
        List<String> etiquetasVentas = new ArrayList<>();
        List<Long> valoresVentas = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoy.minusDays(i);
            etiquetasVentas.add(dia.getDayOfMonth() + "/" + dia.getMonthValue());
            valoresVentas.add(conteoPorDia.getOrDefault(dia, 0L));
        }

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalMarcas", totalMarcas);
        model.addAttribute("totalCategorias", totalCategorias);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalRoles", totalRoles);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("ventasPendientes", ventasPendientes);
        model.addAttribute("ultimasVentas", ultimasVentas);
        model.addAttribute("stockBajo", stockBajo);
        model.addAttribute("etiquetasVentas", etiquetasVentas);
        model.addAttribute("valoresVentas", valoresVentas);
        return "admin/index";
    }

    @GetMapping("/acceso-denegado")
    public String AccesoDenegado() {
        return "admin/acceso-denegado";
    }
}
