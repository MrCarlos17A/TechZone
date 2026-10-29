package com.tienda.backend_techzone.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarViolacionIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violacion de integridad en la base de datos: {}", ex.getMessage());
        String mensaje = "Ya existe un registro con esos datos en el sistema.";
        String detalle = ex.getMessage() != null ? ex.getMessage() : "";

        if (detalle.contains("uk_persona_documento")) {
            mensaje = "El número de documento (DNI) ya está registrado en el sistema.";
        } else if (detalle.contains("uk_persona_telefono")) {
            mensaje = "El número de teléfono ya está registrado en el sistema.";
        } else if (detalle.contains("uk_usuario_correo")) {
            mensaje = "El correo electrónico ya se encuentra registrado con otra cuenta.";
        } else if (detalle.contains("uk_usuario_persona")) {
            mensaje = "La persona ya tiene una cuenta de usuario vinculada.";
        } else if (detalle.contains("uk_proveedor_documento")) {
            mensaje = "El documento del proveedor ya está registrado.";
        }

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("ok", false);
        error.put("status", HttpStatus.CONFLICT.value());
        error.put("mensaje", mensaje);
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> manejarCredencialesIncorrectas(BadCredentialsException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("ok", false);
        error.put("status", HttpStatus.UNAUTHORIZED.value());
        error.put("mensaje", "Correo o contraseña incorrectos.");
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> manejarCuentaBloqueada(LockedException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("ok", false);
        error.put("status", HttpStatus.LOCKED.value());
        error.put("mensaje", ex.getMessage() != null ? ex.getMessage() : "Tu cuenta ha sido bloqueada temporalmente por demasiados intentos fallidos.");
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.LOCKED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> manejarAccesoDenegado(AccessDeniedException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("ok", false);
        error.put("status", HttpStatus.FORBIDDEN.value());
        error.put("mensaje", "No tienes permisos suficientes para realizar esta acción.");
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacionCampos(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(f -> campos.put(f.getField(), f.getDefaultMessage()));

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("ok", false);
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("mensaje", "Datos de solicitud inválidos");
        error.put("errores", campos);
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("ok", false);
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("mensaje", ex.getMessage());
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> manejarRuntime(RuntimeException ex) {
        log.error("Error en tiempo de ejecución: ", ex);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("ok", false);
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("mensaje", ex.getMessage() != null ? ex.getMessage() : "Error al procesar la solicitud.");
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
