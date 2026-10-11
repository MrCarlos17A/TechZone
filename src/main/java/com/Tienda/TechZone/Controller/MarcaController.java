package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Marca;
import com.Tienda.TechZone.Service.MarcaService;
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
@RequestMapping("/admin/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    @GetMapping
    public String ListarMarcas(Model model) {
        model.addAttribute("marcas", marcaService.ListarMarcas());
        return "admin/marcas/listar";
    }

    @GetMapping("/nueva")
    public String FormularioNueva(Model model) {
        model.addAttribute("marca", new Marca());
        return "admin/marcas/formulario";
    }

    @PostMapping("/guardar")
    public String CrearMarca(@ModelAttribute Marca marca,
            @RequestParam(value = "archivoImagen", required = false) MultipartFile archivoImagen,
            RedirectAttributes flash) {
        try {
            if (archivoImagen != null && !archivoImagen.isEmpty()) {
                marca.setImagen(guardarArchivo(archivoImagen, "marcas"));
            }
            marcaService.CrearMarca(marca);
            flash.addFlashAttribute("exito", "Marca creada correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo crear la marca: " + e.getMessage());
        }
        return "redirect:/admin/marcas";
    }

    @GetMapping("/editar/{id}")
    public String FormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("marca", marcaService.ObtenerMarcaPorId(id));
        return "admin/marcas/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarMarca(@PathVariable Long id, @ModelAttribute Marca marca,
            @RequestParam(value = "archivoImagen", required = false) MultipartFile archivoImagen,
            RedirectAttributes flash) {
        try {
            if (archivoImagen != null && !archivoImagen.isEmpty()) {
                String anterior = marcaService.ObtenerMarcaPorId(id).getImagen();
                marca.setImagen(guardarArchivo(archivoImagen, "marcas"));
                eliminarArchivo(anterior);
            }
            marcaService.ActualizarMarca(id, marca);
            flash.addFlashAttribute("exito", "Marca actualizada correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo actualizar la marca: " + e.getMessage());
        }
        return "redirect:/admin/marcas";
    }

    @GetMapping("/eliminar/{id}")
    public String EliminarMarca(@PathVariable Long id, RedirectAttributes flash) {
        try {
            String imagen = marcaService.ObtenerMarcaPorId(id).getImagen();
            marcaService.EliminarMarca(id);
            eliminarArchivo(imagen);
            flash.addFlashAttribute("exito", "Marca eliminada correctamente");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", "No se pudo eliminar la marca porque tiene productos asociados.");
        }
        return "redirect:/admin/marcas";
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
