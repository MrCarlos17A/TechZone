package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Service.DetalleVentaService;
import com.Tienda.TechZone.Service.ProductoService;
import com.Tienda.TechZone.Service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/detalles-venta")
@RequiredArgsConstructor
public class DetalleVentaController {

    private final DetalleVentaService detalleVentaService;
    private final VentaService ventaService;
    private final ProductoService productoService;

    @GetMapping
    public String ListarPorVenta(@RequestParam(required = false) Integer idVenta, Model model) {
        if (idVenta != null) {
            model.addAttribute("detalles", detalleVentaService.ListarPorVenta(idVenta));
            model.addAttribute("idVenta", idVenta);
        } else {
            model.addAttribute("detalles", detalleVentaService.ListarPorVenta(null));
        }
        model.addAttribute("ventas", ventaService.ListarVentas());
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/detalles-venta/listar";
    }

    @GetMapping("/ver/{id}")
    public String VerDetalle(@PathVariable Integer id, Model model) {
        model.addAttribute("detalle", detalleVentaService.ObtenerDetallePorId(id));
        return "admin/detalles-venta/detalle";
    }
}
