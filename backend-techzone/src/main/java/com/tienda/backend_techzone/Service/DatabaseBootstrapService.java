package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.*;
import com.tienda.backend_techzone.Repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBootstrapService implements ApplicationRunner {

    private final RolRepository rolRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        sincronizarTodo();
    }

    @Transactional
    public synchronized void sincronizarTodo() {
        try {
            asegurarRolesCatalogo();
            asegurarAdministradorGeneral();
            asegurarUsuariosParaEmpleados();
        } catch (Exception e) {
            log.error("Aviso en DatabaseBootstrapService: {}", e.getMessage(), e);
        }
    }

    private void asegurarRolesCatalogo() {
        try {
            entityManager.createNativeQuery(
                "ALTER TABLE rol ADD COLUMN IF NOT EXISTS es_administrativo BOOLEAN DEFAULT false"
            ).executeUpdate();
        } catch (Exception e) {
            log.warn("Aviso al verificar columna es_administrativo en rol: {}", e.getMessage());
        }

        insertarOActualizarRol(1L, "Administrador", true,
            "{\"dashboard\":[\"ver\"],\"productos\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"categorias\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"marcas\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"imagenes\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"ventas\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"pagos\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"usuarios\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"empleados\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"clientes\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"roles\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"configuracion\":[\"ver\",\"crear\",\"editar\",\"eliminar\"],\"auditoria\":[\"ver\"],\"ayuda\":[\"ver\",\"crear\",\"editar\",\"eliminar\"]}");

        insertarOActualizarRol(2L, "Trabajador", true,
            "{\"dashboard\":[\"ver\"],\"productos\":[\"ver\",\"crear\",\"editar\"],\"categorias\":[\"ver\"],\"marcas\":[\"ver\"],\"imagenes\":[\"ver\"],\"ventas\":[\"ver\",\"crear\",\"editar\"],\"pagos\":[\"ver\"],\"clientes\":[\"ver\",\"crear\",\"editar\"]}");

        insertarOActualizarRol(3L, "Usuario", false,
            "{\"productos\":[\"ver\"]}");

        insertarOActualizarRol(4L, "Cliente", false,
            "{\"productos\":[\"ver\"]}");

        insertarOActualizarRol(5L, "Supervisor de Ventas", true,
            "{\"dashboard\":[\"ver\"],\"ventas\":[\"ver\",\"crear\",\"editar\"],\"pagos\":[\"ver\",\"crear\",\"editar\"],\"clientes\":[\"ver\",\"crear\",\"editar\"],\"productos\":[\"ver\"]}");

        insertarOActualizarRol(6L, "Logística y Almacén", true,
            "{\"dashboard\":[\"ver\"],\"productos\":[\"ver\",\"crear\",\"editar\"],\"categorias\":[\"ver\",\"crear\",\"editar\"],\"marcas\":[\"ver\",\"crear\",\"editar\"],\"imagenes\":[\"ver\",\"crear\",\"editar\"],\"ventas\":[\"ver\"]}");

        insertarOActualizarRol(7L, "Soporte Técnico", true,
            "{\"dashboard\":[\"ver\"],\"usuarios\":[\"ver\"],\"clientes\":[\"ver\"],\"productos\":[\"ver\"],\"ventas\":[\"ver\"],\"auditoria\":[\"ver\"]}");

        try {
            entityManager.createNativeQuery(
                "SELECT setval(pg_get_serial_sequence('rol', 'id_rol'), GREATEST((SELECT MAX(id_rol) FROM rol), 7))"
            ).getSingleResult();
        } catch (Exception ignored) {}
    }

    private void insertarOActualizarRol(Long id, String nombre, boolean esAdministrativo, String permisos) {
        try {
            entityManager.createNativeQuery(
                "INSERT INTO rol (id_rol, nombre, vigente, es_administrativo, permisos) " +
                "VALUES (:id, :nombre, true, :esAdmin, :permisos) " +
                "ON CONFLICT (id_rol) DO UPDATE SET " +
                "nombre = EXCLUDED.nombre, " +
                "permisos = EXCLUDED.permisos, " +
                "es_administrativo = EXCLUDED.es_administrativo, " +
                "vigente = true"
            )
            .setParameter("id", id)
            .setParameter("nombre", nombre)
            .setParameter("esAdmin", esAdministrativo)
            .setParameter("permisos", permisos)
            .executeUpdate();
        } catch (Exception e) {
            log.warn("No se pudo insertar/actualizar rol {}: {}", nombre, e.getMessage());
        }
    }

    private void asegurarAdministradorGeneral() {
        try {
            // 1. Asegurar Persona #1 (Carlos Felipe Arroyo Vega)
            Persona adminPersona = personaRepository.findById(1L).orElse(null);
            if (adminPersona == null) {
                entityManager.createNativeQuery(
                    "INSERT INTO persona (id_persona, nombres, apellido_paterno, apellido_materno, id_tipo_documento, numero_documento, telefono, direccion, id_ubigeo, fecha_nacimiento, fecha_registro, vigente) " +
                    "VALUES (1, 'CARLOS FELIPE', 'ARROYO', 'VEGA', 1, '73234768', '967662103', 'El Vivero S/N', 140120, '2005-04-12', now(), true) " +
                    "ON CONFLICT (id_persona) DO NOTHING"
                ).executeUpdate();
                adminPersona = personaRepository.findById(1L).orElse(null);
            }

            // 2. Asegurar Usuario #1 (Administrador)
            Usuario adminUsuario = usuarioRepository.findById(1L).orElse(null);
            Rol rolAdmin = rolRepository.findById(1L).orElse(null);
            if (adminUsuario == null) {
                entityManager.createNativeQuery(
                    "INSERT INTO usuario (id_usuario, id_persona, id_rol, correo, contrasena, estado, fecha_creacion, intentos_fallidos) " +
                    "VALUES (1, 1, 1, 'admin@techzone.com', :pass, 'activo', now(), 0) " +
                    "ON CONFLICT (id_usuario) DO NOTHING"
                )
                .setParameter("pass", passwordEncoder.encode("Admin123*"))
                .executeUpdate();
            } else {
                if (adminUsuario.getPersona() == null && adminPersona != null) {
                    adminUsuario.setPersona(adminPersona);
                }
                if (adminUsuario.getRol() == null && rolAdmin != null) {
                    adminUsuario.setRol(rolAdmin);
                }
                usuarioRepository.save(adminUsuario);
            }

            // 3. Asegurar Empleado #1 (Administrador General)
            try {
                Empleado empExistente = adminPersona != null ? empleadoRepository.findFirstByPersona(adminPersona).orElse(null) : null;
                if (empExistente == null) {
                    entityManager.createNativeQuery(
                        "INSERT INTO empleado (id_empleado, id_persona, fecha_contrato, salario, vigente) " +
                        "VALUES (1, 1, '2026-01-01', 3500.00, true) " +
                        "ON CONFLICT (id_empleado) DO NOTHING"
                    ).executeUpdate();
                }
            } catch (Exception ex) {
                log.warn("Aviso al insertar empleado #1 con ID explicito: {}", ex.getMessage());
            }

            try {
                entityManager.createNativeQuery(
                    "SELECT setval(pg_get_serial_sequence('empleado', 'id_empleado'), GREATEST((SELECT COALESCE(MAX(id_empleado), 1) FROM empleado), 1))"
                ).getSingleResult();
            } catch (Exception ignored) {}

        } catch (Exception e) {
            log.error("Error al asegurar administrador general: {}", e.getMessage(), e);
        }
    }

    private void asegurarUsuariosParaEmpleados() {
        try {
            List<Empleado> empleados = empleadoRepository.findAll();
            Rol rolTrabajador = rolRepository.findById(2L).orElse(null);

            for (Empleado emp : empleados) {
                Persona p = emp.getPersona();
                if (p == null) continue;

                Usuario usuarioExistente = usuarioRepository.findFirstByPersona(p).orElse(null);
                if (usuarioExistente == null) {
                    String baseCorreo = (p.getNombres() != null ? p.getNombres().trim().split("\\s+")[0].toLowerCase() : "colaborador")
                            + "." + (p.getApellidoPaterno() != null ? p.getApellidoPaterno().trim().toLowerCase() : "techzone")
                            + "@techzone.pe";

                    baseCorreo = baseCorreo.replaceAll("[^a-z0-9.@-]", "");
                    String correoFinal = baseCorreo;
                    int sufijo = 1;
                    while (usuarioRepository.existsByCorreo(correoFinal)) {
                        correoFinal = baseCorreo.replace("@", sufijo + "@");
                        sufijo++;
                    }

                    Usuario nuevoU = Usuario.builder()
                            .persona(p)
                            .rol(rolTrabajador)
                            .correo(correoFinal)
                            .password(passwordEncoder.encode("TechZone2026*"))
                            .estado(emp.getVigente() != null && emp.getVigente() ? "activo" : "inactivo")
                            .fechaCreacion(LocalDateTime.now())
                            .intentosFallidos(0)
                            .build();
                    usuarioRepository.save(nuevoU);
                    log.info("Cuenta generada automaticamente para colaborador #{}: {}", emp.getIdEmpleado(), correoFinal);
                } else {
                    boolean modificado = false;
                    if (usuarioExistente.getRol() == null && rolTrabajador != null) {
                        usuarioExistente.setRol(rolTrabajador);
                        modificado = true;
                    }
                    if (usuarioExistente.getPersona() == null) {
                        usuarioExistente.setPersona(p);
                        modificado = true;
                    }
                    if (modificado) {
                        usuarioRepository.save(usuarioExistente);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al asegurar usuarios para empleados: {}", e.getMessage(), e);
        }
    }
}
