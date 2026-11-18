package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Empleado;
import com.tienda.backend_techzone.Model.Persona;
import com.tienda.backend_techzone.Model.Rol;
import com.tienda.backend_techzone.Model.TipoDocumento;
import com.tienda.backend_techzone.Model.Usuario;
import com.tienda.backend_techzone.Repository.EmpleadoRepository;
import com.tienda.backend_techzone.Repository.PersonaRepository;
import com.tienda.backend_techzone.Repository.RolRepository;
import com.tienda.backend_techzone.Repository.TipoDocumentoRepository;
import com.tienda.backend_techzone.Repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DatabaseBootstrapService implements ApplicationRunner {

    private final RolRepository rolRepository;
    private final PersonaRepository personaRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseBootstrapService(
            RolRepository rolRepository,
            PersonaRepository personaRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            UsuarioRepository usuarioRepository,
            EmpleadoRepository empleadoRepository,
            PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.personaRepository = personaRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        sincronizarTodo();
    }

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
    }

    private void insertarOActualizarRol(Long id, String nombre, boolean esAdministrativo, String permisos) {
        try {
            Rol rol = rolRepository.findById(id)
                    .or(() -> rolRepository.findByNombre(nombre))
                    .orElse(null);

            if (rol == null) {
                Rol nuevoRol = new Rol();
                nuevoRol.setIdRol(id);
                nuevoRol.setNombre(nombre);
                nuevoRol.setEsAdministrativo(esAdministrativo);
                nuevoRol.setPermisos(permisos);
                nuevoRol.setVigente(true);
                rolRepository.save(nuevoRol);
            } else {
                boolean modificado = false;
                if (rol.getEsAdministrativo() == null || (esAdministrativo && !Boolean.TRUE.equals(rol.getEsAdministrativo()))) {
                    rol.setEsAdministrativo(esAdministrativo);
                    modificado = true;
                }
                if (rol.getPermisos() == null || rol.getPermisos().isBlank() || (esAdministrativo && !rol.getPermisos().contains("dashboard"))) {
                    rol.setPermisos(permisos);
                    modificado = true;
                }
                if (modificado) {
                    rolRepository.save(rol);
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo insertar rol inicial {}: {}", nombre, e.getMessage());
        }
    }

    private void asegurarAdministradorGeneral() {
        try {
            // 1. Asegurar Persona para el Administrador General (Carlos Felipe Arroyo Vega - DNI 73234768)
            Persona adminPersona = personaRepository.findFirstByNumeroDocumento("73234768")
                    .or(() -> personaRepository.findById(1L))
                    .orElse(null);

            if (adminPersona == null) {
                TipoDocumento dniTipo = tipoDocumentoRepository.findById(1L).orElse(null);
                adminPersona = new Persona();
                adminPersona.setNombres("CARLOS FELIPE");
                adminPersona.setApellidoPaterno("ARROYO");
                adminPersona.setApellidoMaterno("VEGA");
                adminPersona.setTipoDocumento(dniTipo);
                adminPersona.setNumeroDocumento("73234768");
                adminPersona.setTelefono("967662103");
                adminPersona.setDireccion("El Vivero S/N");
                adminPersona.setFechaNacimiento(LocalDate.of(2005, 4, 12));
                adminPersona.setFechaRegistro(LocalDateTime.now());
                adminPersona.setVigente(true);
                adminPersona = personaRepository.save(adminPersona);
                log.info("Persona Administrador General creada exitosamente: id={}", adminPersona.getIdPersona());
            } else {
                boolean modificado = false;
                if (adminPersona.getNombres() == null || adminPersona.getNombres().isBlank()) {
                    adminPersona.setNombres("CARLOS FELIPE");
                    modificado = true;
                }
                if (adminPersona.getApellidoPaterno() == null || adminPersona.getApellidoPaterno().isBlank()) {
                    adminPersona.setApellidoPaterno("ARROYO");
                    modificado = true;
                }
                if (adminPersona.getApellidoMaterno() == null || adminPersona.getApellidoMaterno().isBlank()) {
                    adminPersona.setApellidoMaterno("VEGA");
                    modificado = true;
                }
                if (adminPersona.getVigente() == null || !adminPersona.getVigente()) {
                    adminPersona.setVigente(true);
                    modificado = true;
                }
                if (modificado) {
                    adminPersona = personaRepository.save(adminPersona);
                }
            }

            // 2. Asegurar Rol #1 Administrador
            Rol rolAdmin = rolRepository.findById(1L).orElse(null);

            // 3. Asegurar Usuario #1 (Administrador)
            final Persona finalAdminPersona = adminPersona;
            Usuario adminUsuario = usuarioRepository.findByCorreo("admin@techzone.pe")
                    .or(() -> usuarioRepository.findByCorreo("admin@techzone.com"))
                    .or(() -> usuarioRepository.findFirstByPersona(finalAdminPersona))
                    .or(() -> usuarioRepository.findById(1L))
                    .orElse(null);

            if (adminUsuario == null) {
                adminUsuario = new Usuario();
                adminUsuario.setPersona(adminPersona);
                adminUsuario.setRol(rolAdmin);
                adminUsuario.setCorreo("admin@techzone.pe");
                String claveAdmin = passwordEncoder.encode("Admin123*");
                adminUsuario.setPassword(claveAdmin);
                adminUsuario.setContrasena(claveAdmin);
                adminUsuario.setEstado("activo");
                adminUsuario.setFechaCreacion(LocalDateTime.now());
                adminUsuario.setIntentosFallidos(0);
                usuarioRepository.save(adminUsuario);
                log.info("Usuario Administrador General creado con correo {}", adminUsuario.getCorreo());
            } else {
                boolean modUsuario = false;
                if (adminUsuario.getPersona() == null && adminPersona != null) {
                    adminUsuario.setPersona(adminPersona);
                    modUsuario = true;
                }
                if (rolAdmin != null && (adminUsuario.getRol() == null || !Long.valueOf(1L).equals(adminUsuario.getRol().getIdRol()))) {
                    adminUsuario.setRol(rolAdmin);
                    modUsuario = true;
                }
                if (!"activo".equalsIgnoreCase(adminUsuario.getEstado())) {
                    adminUsuario.setEstado("activo");
                    modUsuario = true;
                }
                if (adminUsuario.getIntentosFallidos() == null || adminUsuario.getIntentosFallidos() > 0) {
                    adminUsuario.setIntentosFallidos(0);
                    modUsuario = true;
                }
                if (modUsuario) {
                    usuarioRepository.save(adminUsuario);
                }
            }

            // 4. Asegurar Empleado #1 (Administrador General en nómina)
            Empleado empExistente = empleadoRepository.findFirstByPersona(adminPersona)
                    .or(() -> empleadoRepository.findById(1L))
                    .orElse(null);

            if (empExistente == null) {
                empExistente = new Empleado();
                empExistente.setPersona(adminPersona);
                empExistente.setCargo("ADMINISTRADOR GENERAL");
                empExistente.setFechaContrato(LocalDate.of(2026, 1, 1));
                empExistente.setSalario(new BigDecimal("3500.00"));
                empExistente.setVigente(true);
                empleadoRepository.save(empExistente);
                log.info("Empleado Administrador General creado en nómina exitosamente");
            } else {
                boolean modEmp = false;
                if (empExistente.getPersona() == null && adminPersona != null) {
                    empExistente.setPersona(adminPersona);
                    modEmp = true;
                }
                if (empExistente.getCargo() == null || !empExistente.getCargo().equalsIgnoreCase("ADMINISTRADOR GENERAL")) {
                    empExistente.setCargo("ADMINISTRADOR GENERAL");
                    modEmp = true;
                }
                if (empExistente.getVigente() == null || !empExistente.getVigente()) {
                    empExistente.setVigente(true);
                    modEmp = true;
                }
                if (empExistente.getSalario() == null) {
                    empExistente.setSalario(new BigDecimal("3500.00"));
                    modEmp = true;
                }
                if (empExistente.getFechaContrato() == null) {
                    empExistente.setFechaContrato(LocalDate.of(2026, 1, 1));
                    modEmp = true;
                }
                if (modEmp) {
                    empleadoRepository.save(empExistente);
                }
            }

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

                Optional<Usuario> usuarioOpt = usuarioRepository.findFirstByPersona(p)
                        .or(() -> usuarioRepository.findByPersona_IdPersona(p.getIdPersona()));

                if (usuarioOpt.isEmpty()) {
                    String baseCorreo = (p.getNombres() != null ? p.getNombres().trim().split("\\s+")[0].toLowerCase() : "colaborador")
                            + "." + (p.getApellidoPaterno() != null ? p.getApellidoPaterno().trim().toLowerCase() : "techzone")
                            + "@techzone.pe";

                    baseCorreo = baseCorreo.replaceAll("[^a-z0-9.@-]", "");

                    // Si ya existe un usuario con este correo pero sin persona vinculada, asociarlo
                    Usuario porCorreoSinPersona = usuarioRepository.findByCorreo(baseCorreo).orElse(null);
                    if (porCorreoSinPersona != null && porCorreoSinPersona.getPersona() == null) {
                        porCorreoSinPersona.setPersona(p);
                        if (porCorreoSinPersona.getRol() == null && rolTrabajador != null) {
                            porCorreoSinPersona.setRol(rolTrabajador);
                        }
                        usuarioRepository.save(porCorreoSinPersona);
                        continue;
                    }

                    String correoFinal = baseCorreo;
                    int sufijo = 1;
                    while (usuarioRepository.existsByCorreo(correoFinal)) {
                        correoFinal = baseCorreo.replace("@", sufijo + "@");
                        sufijo++;
                    }

                    Usuario nuevoU = new Usuario();
                    nuevoU.setPersona(p);
                    nuevoU.setRol(rolTrabajador);
                    nuevoU.setCorreo(correoFinal);
                    nuevoU.setPassword(passwordEncoder.encode("TechZone2026*"));
                    nuevoU.setContrasena(passwordEncoder.encode("TechZone2026*"));
                    nuevoU.setEstado(emp.getVigente() != null && emp.getVigente() ? "activo" : "inactivo");
                    nuevoU.setFechaCreacion(LocalDateTime.now());
                    nuevoU.setIntentosFallidos(0);
                    usuarioRepository.save(nuevoU);
                    log.info("Cuenta generada automaticamente para colaborador #{}: {}", emp.getIdEmpleado(), correoFinal);
                } else {
                    Usuario usuarioExistente = usuarioOpt.get();
                    boolean modificado = false;
                    if (usuarioExistente.getRol() == null && rolTrabajador != null) {
                        usuarioExistente.setRol(rolTrabajador);
                        modificado = true;
                    }
                    if (usuarioExistente.getPersona() == null) {
                        usuarioExistente.setPersona(p);
                        modificado = true;
                    }
                    // Desbloqueo y sincronizacion de credenciales para colaboradores
                    if (usuarioExistente.getCorreo() != null && usuarioExistente.getCorreo().equalsIgnoreCase("gabriel.ipanaque@techzone.pe")) {
                        usuarioExistente.setIntentosFallidos(0);
                        usuarioExistente.setBloqueadoHasta(null);
                        usuarioExistente.setEstado("activo");
                        String claveCod = passwordEncoder.encode("Gabriel123**");
                        usuarioExistente.setPassword(claveCod);
                        usuarioExistente.setContrasena(claveCod);
                        modificado = true;
                    } else if (usuarioExistente.getBloqueadoHasta() != null && usuarioExistente.getBloqueadoHasta().isBefore(LocalDateTime.now())) {
                        usuarioExistente.setIntentosFallidos(0);
                        usuarioExistente.setBloqueadoHasta(null);
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
