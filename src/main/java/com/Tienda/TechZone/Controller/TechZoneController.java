package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Model.Marca;
import com.Tienda.TechZone.Model.Categoria;
import com.Tienda.TechZone.Service.ProductoService;
import com.Tienda.TechZone.Service.MarcaService;
import com.Tienda.TechZone.Service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TechZoneController {

    private final ProductoService productoService;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;

    @GetMapping("/")
    public String Inicio(Model model) {
        List<Marca> marcas = marcaService.ListarMarcasActivas();
        List<Categoria> categorias = categoriaService.ListarCategoriasActivas();
        List<Producto> todosProductos = productoService.ListarProductosActivos();

        Map<Categoria, List<Producto>> productosPorCategoria = new LinkedHashMap<>();
        for (Categoria cat : categorias) {
            List<Producto> productosCat = todosProductos.stream()
                    .filter(p -> p.getCategoria().getIdCategoria().equals(cat.getIdCategoria()))
                    .collect(Collectors.toList());
            if (!productosCat.isEmpty()) {
                productosPorCategoria.put(cat, productosCat);
            }
        }

        model.addAttribute("marcas", marcas);
        model.addAttribute("productosPorCategoria", productosPorCategoria);
        return "index";
    }

    @GetMapping("/productos")
    public String Productos(@RequestParam(value = "categoria", required = false) Long idCategoria, Model model) {
        List<Categoria> categorias = categoriaService.ListarCategoriasActivas();
        List<Producto> productos;
        Categoria categoriaActiva = null;

        if (idCategoria != null) {
            productos = productoService.ListarProductosActivosPorCategoria(idCategoria);
            for (Categoria c : categorias) {
                if (c.getIdCategoria().equals(idCategoria)) {
                    categoriaActiva = c;
                    break;
                }
            }
        } else {
            productos = productoService.ListarProductosActivos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoriaSeleccionada", idCategoria);
        model.addAttribute("categoriaActiva", categoriaActiva);
        model.addAttribute("tituloPagina", categoriaActiva != null ? categoriaActiva.getNombre() : "Todos los Productos");
        return "productos";
    }

    @GetMapping("/novedades")
    public String Novedades(Model model) {
        List<Categoria> categorias = categoriaService.ListarCategoriasActivas();
        Long idCategoriaTarjetas = null;
        for (Categoria c : categorias) {
            if (c.getNombre().toLowerCase().contains("tarjeta")) {
                idCategoriaTarjetas = c.getIdCategoria();
                break;
            }
        }
        List<Producto> productos;
        if (idCategoriaTarjetas != null) {
            productos = productoService.ListarProductosActivosPorCategoria(idCategoriaTarjetas);
        } else {
            productos = productoService.ListarProductosActivos();
        }
        model.addAttribute("productos", productos);
        return "novedades";
    }

    @GetMapping("/marcas")
    public String Marcas(Model model) {
        List<Marca> marcas = marcaService.ListarMarcasActivas();
        model.addAttribute("marcas", marcas);
        return "marcas";
    }

    @GetMapping("/buscar")
    public String Buscar(@RequestParam(value = "q", required = false, defaultValue = "") String query, Model model) {
        List<Producto> resultados = productoService.BuscarProductos(query);
        model.addAttribute("productos", resultados);
        model.addAttribute("query", query);
        return "busqueda";
    }
}
