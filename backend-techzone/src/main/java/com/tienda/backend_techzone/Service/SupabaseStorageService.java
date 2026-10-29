package com.tienda.backend_techzone.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private static final Set<String> EXTENSIONES_VALIDAS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final HttpClient CLIENTE = HttpClient.newHttpClient();

    @Value("${supabase.storage.project-url:${supabase.url:}}")
    private String projectUrl;

    @Value("${supabase.storage.key:${supabase.service-key:}}")
    private String storageKey;

    @Value("${supabase.storage.bucket:${supabase.bucket:imagenes}}")
    private String bucket;

    /**
     * Sube una imagen al bucket de Supabase Storage y devuelve su URL publica.
     * Devuelve null si el archivo viene vacio (para que el campo quede sin cambiar).
     */
    public String SubirImagen(MultipartFile archivo, String carpeta) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new RuntimeException("El archivo no tiene nombre valido");
        }
        String nombreLimpio = Paths.get(nombreOriginal).getFileName().toString();

        String extension = nombreLimpio.contains(".")
                ? nombreLimpio.substring(nombreLimpio.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!EXTENSIONES_VALIDAS.contains(extension)) {
            throw new RuntimeException("Formato no valido. Usa JPG, PNG, GIF o WEBP");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }

        if (storageKey == null || storageKey.isBlank()) {
            throw new RuntimeException("Falta la clave de Supabase Storage (supabase.storage.key)");
        }

        String ruta = construirRuta(carpeta, nombreLimpio);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(projectUrl + "/storage/v1/object/" + bucket + "/" + ruta))
                .header("Authorization", "Bearer " + storageKey)
                .header("apikey", storageKey)
                .header("Content-Type", contentType)
                .header("x-upsert", "true")
                .POST(HttpRequest.BodyPublishers.ofByteArray(archivo.getBytes()))
                .build();

        HttpResponse<String> respuesta;
        try {
            respuesta = CLIENTE.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Se interrumpio la subida de la imagen");
        }

        if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
            log.error("Supabase rechazo la imagen. HTTP {} -> {}", respuesta.statusCode(), respuesta.body());
            throw new RuntimeException("Supabase rechazo la imagen (HTTP " + respuesta.statusCode() + ")");
        }

        String urlPublica = projectUrl + "/storage/v1/object/public/" + bucket + "/" + ruta;
        log.info("Imagen subida a Supabase: {}", urlPublica);
        return urlPublica;
    }

    /**
     * Carpeta de destino segun la categoria del producto.
     * "Monitores" -> productos/monitores | "Tarjetas de Video" -> productos/tarjetas-de-video
     */
    /**
     * Descarga una imagen desde una URL externa y la sube al bucket de Supabase Storage.
     * Si ya pertenece a Supabase, devuelve la misma URL.
     */
    public String SubirImagenDesdeUrl(String urlOrigen, String carpeta) {
        if (urlOrigen == null || urlOrigen.isBlank()) {
            return null;
        }
        String urlLimpia = urlOrigen.trim();
        if (urlLimpia.contains("/storage/v1/object/public/")) {
            return urlLimpia;
        }

        try {
            HttpRequest reqDescarga = HttpRequest.newBuilder()
                    .uri(URI.create(urlLimpia))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) TechZone/1.0")
                    .GET()
                    .build();

            HttpResponse<byte[]> resDescarga = CLIENTE.send(reqDescarga, HttpResponse.BodyHandlers.ofByteArray());
            if (resDescarga.statusCode() < 200 || resDescarga.statusCode() >= 300) {
                log.warn("No se pudo descargar la imagen desde URL: HTTP {}", resDescarga.statusCode());
                return urlLimpia;
            }

            byte[] bytes = resDescarga.body();
            if (bytes == null || bytes.length == 0) {
                return urlLimpia;
            }

            String contentType = resDescarga.headers().firstValue("Content-Type").orElse("image/jpeg");
            if (!contentType.startsWith("image/")) {
                contentType = "image/jpeg";
            }

            String extension = "jpg";
            if (contentType.contains("png")) extension = "png";
            else if (contentType.contains("webp")) extension = "webp";
            else if (contentType.contains("gif")) extension = "gif";
            else if (urlLimpia.contains(".png")) extension = "png";
            else if (urlLimpia.contains(".webp")) extension = "webp";

            String nombreSimulado = "url_import." + extension;
            String ruta = construirRuta(carpeta, nombreSimulado);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(projectUrl + "/storage/v1/object/" + bucket + "/" + ruta))
                    .header("Authorization", "Bearer " + storageKey)
                    .header("apikey", storageKey)
                    .header("Content-Type", contentType)
                    .header("x-upsert", "true")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .build();

            HttpResponse<String> respuesta = CLIENTE.send(request, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() >= 200 && respuesta.statusCode() < 300) {
                String urlPublica = projectUrl + "/storage/v1/object/public/" + bucket + "/" + ruta;
                log.info("Imagen desde URL subida con exito a Supabase: {}", urlPublica);
                return urlPublica;
            } else {
                log.warn("Supabase rechazo la imagen desde URL. HTTP {} -> {}", respuesta.statusCode(), respuesta.body());
                return urlLimpia;
            }
        } catch (Exception e) {
            log.warn("Excepcion al importar imagen desde URL a Supabase: {}", e.getMessage());
            return urlLimpia;
        }
    }

    public String CarpetaDeCategoria(String nombreCategoria) {
        if (nombreCategoria == null || nombreCategoria.isBlank()) {
            return "productos";
        }
        String slug = nombreCategoria.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return "productos/" + (slug.isBlank() ? "otros" : slug);
    }

    private String construirRuta(String carpeta, String nombreOriginal) {
        String prefijo = (carpeta == null || carpeta.isBlank())
                ? ""
                : carpeta.trim().replaceAll("^/+", "").replaceAll("/+$", "") + "/";
        String nombreUnico = System.currentTimeMillis() + "_"
                + nombreOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
        return prefijo + nombreUnico;
    }

    /**
     * Elimina un archivo de Supabase Storage dada su URL publica.
     */
    public boolean EliminarArchivo(String urlPublica) {
        if (urlPublica == null || urlPublica.isBlank() || storageKey == null || storageKey.isBlank()) {
            return false;
        }
        try {
            String patronPublico = "/storage/v1/object/public/" + bucket + "/";
            int idx = urlPublica.indexOf(patronPublico);
            if (idx == -1) {
                return false;
            }
            String ruta = urlPublica.substring(idx + patronPublico.length());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(projectUrl + "/storage/v1/object/" + bucket + "/" + ruta))
                    .header("Authorization", "Bearer " + storageKey)
                    .header("apikey", storageKey)
                    .DELETE()
                    .build();

            HttpResponse<String> respuesta = CLIENTE.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Archivo eliminado de Supabase {}: HTTP {}", ruta, respuesta.statusCode());
            return respuesta.statusCode() >= 200 && respuesta.statusCode() < 300;
        } catch (Exception e) {
            log.warn("No se pudo eliminar archivo de Supabase {}: {}", urlPublica, e.getMessage());
            return false;
        }
    }
}
