package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Empleado;
import com.tienda.backend_techzone.Service.EmpleadoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
// ==============================================================
// CRUD DE GESTOR EMPLEADOS
// ==============================================================
@RestController
@RequestMapping("/api/admin/empleados")
public class EmpleadoAdminApiController {

    private final EmpleadoService empleadoService;

    public EmpleadoAdminApiController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    public record CrearEmpleadoReq(
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            Long idTipoDocumento,
            String numeroDocumento,
            String telefono,
            String fechaNacimiento,
            String direccion,
            String referencia,
            Long idUbigeo,
            String correo,
            String password,
            Long idRol,
            String cargo,
            String fechaContrato,
            String fechaContratacion,
            BigDecimal salario) {
        public String obtenerFechaContrato() {
            if (fechaContrato != null && !fechaContrato.isBlank())
                return fechaContrato.trim();
            if (fechaContratacion != null && !fechaContratacion.isBlank())
                return fechaContratacion.trim();
            return null;
        }
    }

    public static LocalDate parsearFechaSegura(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank())
            return null;
        String limpia = fechaStr.trim();
        if (limpia.length() >= 10 && limpia.charAt(4) == '-' && limpia.charAt(7) == '-') {
            try {
                return LocalDate.parse(limpia.substring(0, 10));
            } catch (Exception ignored) {
            }
        }
        if (limpia.length() >= 10 && (limpia.charAt(2) == '/' || limpia.charAt(2) == '-')) {
            String[] p = limpia.substring(0, 10).split("[/-]");
            if (p.length == 3 && p[2].length() == 4) {
                try {
                    return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                } catch (Exception ignored) {
                }
            }
        }
        try {
            return LocalDate.parse(limpia);
        } catch (Exception e) {
            log.warn("No se pudo parsear fecha '{}': {}", fechaStr, e.getMessage());
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<List<Empleado>> listar() {
        return ResponseEntity.ok(empleadoService.listarEmpleados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(empleadoService.obtenerEmpleadoPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(Map.of("mensaje", e.getMessage() != null ? e.getMessage() : "Empleado no encontrado"));
        }
    }

    @PostMapping("/completo")
    public ResponseEntity<?> crearCompleto(@RequestBody CrearEmpleadoReq req) {
        try {
            LocalDate fNac = parsearFechaSegura(req.fechaNacimiento());
            LocalDate fContr = parsearFechaSegura(req.obtenerFechaContrato());
            if (fContr == null) {
                fContr = LocalDate.now();
            }

            Empleado creado = empleadoService.CrearEmpleadoCompleto(
                    req.nombres(),
                    req.apellidoPaterno(),
                    req.apellidoMaterno(),
                    req.idTipoDocumento() != null ? req.idTipoDocumento() : 1L,
                    req.numeroDocumento(),
                    req.telefono(),
                    fNac,
                    req.direccion(),
                    req.referencia(),
                    req.idUbigeo(),
                    req.correo(),
                    req.password(),
                    req.idRol(),
                    fContr,
                    req.salario() != null ? req.salario() : BigDecimal.valueOf(1800));
            if (req.cargo() != null && !req.cargo().isBlank()) {
                creado.setCargo(req.cargo().trim().toUpperCase());
            }
            return ResponseEntity.status(201).body(creado);
        } catch (Exception e) {
            log.error("Error al crear colaborador completo: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("mensaje",
                    e.getMessage() != null ? e.getMessage() : "Error al procesar el registro del colaborador"));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Empleado empleado) {
        try {
            return ResponseEntity.status(201).body(empleadoService.crearEmpleado(empleado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Empleado empleado) {
        try {
            return ResponseEntity.ok(empleadoService.actualizarEmpleado(id, empleado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    public record ActualizarEmpleadoReq(
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            Long idTipoDocumento,
            String numeroDocumento,
            String telefono,
            String fechaNacimiento,
            String direccion,
            String referencia,
            Long idUbigeo,
            String correo,
            String password,
            Long idRol,
            String fechaContrato,
            String fechaContratacion,
            BigDecimal salario,
            Boolean vigente) {
        public String obtenerFechaContrato() {
            if (fechaContrato != null && !fechaContrato.isBlank())
                return fechaContrato.trim();
            if (fechaContratacion != null && !fechaContratacion.isBlank())
                return fechaContratacion.trim();
            return null;
        }
    }

    @PutMapping("/{id}/completo")
    public ResponseEntity<?> actualizarCompleto(@PathVariable Long id, @RequestBody ActualizarEmpleadoReq req) {
        try {
            LocalDate fNac = parsearFechaSegura(req.fechaNacimiento());
            LocalDate fContr = parsearFechaSegura(req.obtenerFechaContrato());

            Empleado actualizado = empleadoService.ActualizarEmpleadoCompleto(
                    id,
                    req.nombres(),
                    req.apellidoPaterno(),
                    req.apellidoMaterno(),
                    req.idTipoDocumento() != null ? req.idTipoDocumento() : 1L,
                    req.numeroDocumento(),
                    req.telefono(),
                    fNac,
                    req.direccion(),
                    req.referencia(),
                    req.idUbigeo(),
                    req.correo(),
                    req.password(),
                    req.idRol(),
                    fContr,
                    req.salario(),
                    req.vigente());
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            log.error("Error al actualizar colaborador #{}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("mensaje",
                    e.getMessage() != null ? e.getMessage() : "Error al actualizar datos del colaborador"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            empleadoService.eliminarEmpleado(id);
            return ResponseEntity.ok(Map.of("mensaje", "Empleado desactivado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
