package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Empleado;
import com.tienda.backend_techzone.Service.EmpleadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
        BigDecimal salario
    ) {}

    @GetMapping
    public ResponseEntity<List<Empleado>> listar() {
        return ResponseEntity.ok(empleadoService.listarEmpleados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(empleadoService.obtenerEmpleadoPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("mensaje", e.getMessage() != null ? e.getMessage() : "Empleado no encontrado"));
        }
    }

    @PostMapping("/completo")
    public ResponseEntity<?> crearCompleto(@RequestBody CrearEmpleadoReq req) {
        try {
            LocalDate fNac = null;
            if (req.fechaNacimiento() != null && !req.fechaNacimiento().isBlank()) {
                fNac = LocalDate.parse(req.fechaNacimiento().trim());
            }

            LocalDate fContr = LocalDate.now();
            if (req.fechaContrato() != null && !req.fechaContrato().isBlank()) {
                fContr = LocalDate.parse(req.fechaContrato().trim());
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
                req.salario() != null ? req.salario() : BigDecimal.valueOf(1800)
            );
            if (req.cargo() != null && !req.cargo().isBlank()) {
                creado.setCargo(req.cargo().trim().toUpperCase());
            }
            return ResponseEntity.status(201).body(creado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage() != null ? e.getMessage() : "Error al procesar el registro del colaborador"));
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
        BigDecimal salario,
        Boolean vigente
    ) {}

    @PutMapping("/{id}/completo")
    public ResponseEntity<?> actualizarCompleto(@PathVariable Long id, @RequestBody ActualizarEmpleadoReq req) {
        try {
            LocalDate fNac = null;
            if (req.fechaNacimiento() != null && !req.fechaNacimiento().isBlank()) {
                fNac = LocalDate.parse(req.fechaNacimiento().trim());
            }

            LocalDate fContr = null;
            if (req.fechaContrato() != null && !req.fechaContrato().isBlank()) {
                fContr = LocalDate.parse(req.fechaContrato().trim());
            }

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
                req.vigente()
            );
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage() != null ? e.getMessage() : "Error al actualizar datos del colaborador"));
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
