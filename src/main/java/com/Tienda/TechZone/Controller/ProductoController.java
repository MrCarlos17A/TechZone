package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Service.ProductoService;
import com.Tienda.TechZone.Service.MarcaService;
import com.Tienda.TechZone.Service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;

    @GetMapping
    public String ListarProductos(Model model) {
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/productos/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("marcas", marcaService.ListarMarcas());
        model.addAttribute("categorias", categoriaService.ListarCategorias());
        return "admin/productos/formulario";
    }

    @PostMapping("/guardar")
    public String CrearProducto(@ModelAttribute Producto producto,
            @RequestParam(value = "archivoImagen", required = false) MultipartFile archivoImagen,
            RedirectAttributes flash) {
        try {
            if (archivoImagen != null && !archivoImagen.isEmpty()) {
                producto.setImagen(guardarArchivo(archivoImagen, "productos"));
            }
            productoService.CrearProducto(producto);
            flash.addFlashAttribute("exito", "Producto creado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo crear el producto: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.ObtenerProductoPorId(id));
        model.addAttribute("marcas", marcaService.ListarMarcas());
        model.addAttribute("categorias", categoriaService.ListarCategorias());
        return "admin/productos/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarProducto(@PathVariable Long id, @ModelAttribute Producto producto,
            @RequestParam(value = "archivoImagen", required = false) MultipartFile archivoImagen,
            RedirectAttributes flash) {
        try {
            if (archivoImagen != null && !archivoImagen.isEmpty()) {
                String anterior = productoService.ObtenerProductoPorId(id).getImagen();
                producto.setImagen(guardarArchivo(archivoImagen, "productos"));
                eliminarArchivo(anterior);
            }
            productoService.ActualizarProducto(id, producto);
            flash.addFlashAttribute("exito", "Producto actualizado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo actualizar el producto: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarProducto(@PathVariable Long id, RedirectAttributes flash) {
        try {
            String imagen = productoService.ObtenerProductoPorId(id).getImagen();
            productoService.EliminarProducto(id);
            eliminarArchivo(imagen);
            flash.addFlashAttribute("exito", "Producto eliminado correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar el producto porque tiene registros asociados (pedidos).");
        }
        return "redirect:/admin/productos";
    }

    private String guardarArchivo(MultipartFile archivo, String carpeta) throws Exception {
        String original = StringUtils.cleanPath(archivo.getOriginalFilename());
        String extension = "";
        int punto = original.lastIndexOf('.');
        if (punto >= 0) {
            extension = original.substring(punto).toLowerCase();
        }
        if (!List.of(".png", ".jpg", ".jpeg", ".webp", ".gif").contains(extension)) {
            throw new RuntimeException("Formato no permitido. Usa PNG, JPG o WEBP.");
        }
        String nombreArchivo = System.currentTimeMillis() + "_" + original.replaceAll("\\s+", "_");
        Path destino = Paths.get("uploads", carpeta).toAbsolutePath();
        Files.createDirectories(destino);
        archivo.transferTo(destino.resolve(nombreArchivo));
        return "/uploads/" + carpeta + "/" + nombreArchivo;
    }

    private void eliminarArchivo(String ruta) {
        if (ruta == null || !ruta.startsWith("/uploads/")) {
            return;
        }
        try {
            Path base = Paths.get("uploads").toAbsolutePath().normalize();
            Path archivo = base.resolve(ruta.substring("/uploads/".length())).normalize();
            if (archivo.startsWith(base)) {
                Files.deleteIfExists(archivo);
            }
        } catch (Exception e) {
            // Limpieza opcional: si falla no se interrumpe la operacion
        }
    }
}
