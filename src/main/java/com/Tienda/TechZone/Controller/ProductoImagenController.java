package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.ProductoImagen;
import com.Tienda.TechZone.Service.ProductoImagenService;
import com.Tienda.TechZone.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/admin/imagenes-producto")
@RequiredArgsConstructor
public class ProductoImagenController {

    private final ProductoImagenService productoImagenService;
    private final ProductoService productoService;

    private static final Path UPLOADS_DIR = Paths.get("uploads");

    @GetMapping
    public String ListarImagenes(@RequestParam(required = false) Long idProducto, Model model) {
        if (idProducto != null) {
            model.addAttribute("imagenes", productoImagenService.ListarImagenesPorProducto(idProducto));
            model.addAttribute("idProducto", idProducto);
        } else {
            model.addAttribute("imagenes", productoImagenService.ListarImagenesPorProducto(null));
        }
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/imagenes-producto/listar";
    }

    @GetMapping("/nuevo")
    public String FormularioNuevo(@RequestParam(required = false) Long idProducto, Model model) {
        model.addAttribute("imagen", new ProductoImagen());
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/imagenes-producto/formulario";
    }

    @PostMapping("/guardar")
    public String CrearImagen(@ModelAttribute ProductoImagen imagen,
            @RequestParam(value = "archivoImagen", required = false) MultipartFile archivoImagen,
            RedirectAttributes flash) {
        try {
            String url = guardarArchivo(archivoImagen);
            if (url != null) {
                imagen.setUrlImagen(url);
            }
            productoImagenService.AgregarImagen(imagen);
            flash.addFlashAttribute("exito", "Imagen agregada correctamente");
        } catch (RuntimeException | IOException e) {
            flash.addFlashAttribute("error", "No se pudo agregar la imagen: " + e.getMessage());
        }
        return "redirect:/admin/imagenes-producto";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("imagen", productoImagenService.ObtenerImagenPorId(id));
        model.addAttribute("productos", productoService.ListarProductos());
        return "admin/imagenes-producto/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarImagen(@PathVariable Long id, @ModelAttribute ProductoImagen imagen,
            @RequestParam(value = "archivoImagen", required = false) MultipartFile archivoImagen,
            RedirectAttributes flash) {
        try {
            String url = guardarArchivo(archivoImagen);
            if (url != null) {
                imagen.setUrlImagen(url);
            }
            productoImagenService.ActualizarImagen(id, imagen);
            flash.addFlashAttribute("exito", "Imagen actualizada correctamente");
        } catch (RuntimeException | IOException e) {
            flash.addFlashAttribute("error", "No se pudo actualizar la imagen: " + e.getMessage());
        }
        return "redirect:/admin/imagenes-producto";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarImagen(@PathVariable Long id, RedirectAttributes flash) {
        try {
            productoImagenService.EliminarImagen(id);
            flash.addFlashAttribute("exito", "Imagen eliminada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar la imagen: " + e.getMessage());
        }
        return "redirect:/admin/imagenes-producto";
    }

    private String guardarArchivo(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new RuntimeException("El archivo no tiene nombre valido");
        }
        String nombreLimpio = Paths.get(nombreOriginal).getFileName().toString();
        String extension = nombreLimpio.contains(".")
                ? nombreLimpio.substring(nombreLimpio.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!java.util.Set.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
            throw new RuntimeException("Formato no valido. Usa JPG, PNG, GIF o WEBP");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }
        String nombreUnico = System.currentTimeMillis() + "_" + nombreLimpio.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!Files.exists(UPLOADS_DIR)) {
            Files.createDirectories(UPLOADS_DIR);
        }
        Path destino = UPLOADS_DIR.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + nombreUnico;
    }
}
