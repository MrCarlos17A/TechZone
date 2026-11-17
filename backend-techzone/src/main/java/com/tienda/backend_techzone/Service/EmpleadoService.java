package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.*;
import com.tienda.backend_techzone.Repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UbigeoRepository ubigeoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(
            EmpleadoRepository empleadoRepository,
            PersonaRepository personaRepository,
            UsuarioRepository usuarioRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            UbigeoRepository ubigeoRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.ubigeoRepository = ubigeoRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public List<Empleado> ListarEmpleados() {
        List<Empleado> lista = empleadoRepository.findAllConRelaciones();
        for (Empleado emp : lista) {
            if (emp.getPersona() != null) {
                usuarioRepository.findFirstByPersona(emp.getPersona()).ifPresent(u -> {
                    if (u.getRol() != null && u.getRol().getNombre() != null) {
                        String r = u.getRol().getNombre().toUpperCase();
                        if (r.contains("ADMIN")) {
                            emp.setCargo("ADMINISTRADOR GENERAL");
                        } else {
                            emp.setCargo(r);
                        }
                    }
                });
            }
            if (emp.getCargo() == null || emp.getCargo().isBlank()) {
                emp.setCargo("COLABORADOR");
            }
        }
        return lista;
    }

    public List<Empleado> listarEmpleados() {
        return ListarEmpleados();
    }

    public Empleado ObtenerEmpleadoPorId(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
    }

    public Empleado obtenerEmpleadoPorId(Long id) {
        return ObtenerEmpleadoPorId(id);
    }

    public Empleado CrearEmpleado(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }

    public Empleado crearEmpleado(Empleado empleado) {
        return CrearEmpleado(empleado);
    }

    public Empleado actualizarEmpleado(Long id, Empleado empleado) {
        return ActualizarEmpleado(id, empleado);
    }

    public void eliminarEmpleado(Long id) {
        EliminarEmpleado(id);
    }

    @Transactional
    public Empleado CrearEmpleadoCompleto(String nombres, String apellidoPaterno, String apellidoMaterno,
            Long idTipoDocumento, String numeroDocumento, String telefono, LocalDate fechaNacimiento,
            String direccion, String referencia, Long idUbigeo,
            String correo, String password, Long idRol,
            LocalDate fechaContrato, BigDecimal salario) {

        if (nombres == null || nombres.isBlank()) {
            throw new RuntimeException("Los nombres son obligatorios");
        }
        if (correo == null || !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new RuntimeException("Ingresa un correo valido que contenga @");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria para la cuenta del colaborador");
        }
        UsuarioService.validarContrasenaSegura(password);
        if (idRol == null || !rolRepository.existsById(idRol)) {
            throw new RuntimeException("Selecciona un rol válido registrado en el sistema");
        }
        validarSalario(salario);

        String correoLimpio = correo.trim().toLowerCase();
        String doc = (numeroDocumento != null && !numeroDocumento.isBlank()) ? numeroDocumento.trim() : null;

        // Local-first: si el documento ya esta en el sistema se reusa esa persona
        Persona persona = null;
        if (doc != null) {
            if (idTipoDocumento != null) {
                persona = personaRepository
                        .findFirstByTipoDocumentoAndNumeroDocumento(idTipoDocumento, doc).orElse(null);
            }
            if (persona == null) {
                persona = personaRepository.findFirstByNumeroDocumento(doc).orElse(null);
            }
        }

        // El correo solo se rechaza si pertenece a OTRA persona
        Usuario usuarioExistente = usuarioRepository.findByCorreo(correoLimpio).orElse(null);
        if (usuarioExistente != null) {
            boolean mismaPersona = persona != null && usuarioExistente.getPersona() != null
                    && usuarioExistente.getPersona().getIdPersona().equals(persona.getIdPersona());
            if (!mismaPersona) {
                throw new RuntimeException("El correo ya esta registrado por otra cuenta");
            }
        }

        TipoDocumento tipoDoc = null;
        if (idTipoDocumento != null) {
            tipoDoc = tipoDocumentoRepository.findById(idTipoDocumento).orElse(null);
        }
        Ubigeo ubigeo = null;
        if (idUbigeo != null) {
            ubigeo = ubigeoRepository.findById(idUbigeo).orElse(null);
        }
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        if (persona == null) {
            persona = personaRepository.save(Persona.builder()
                    .nombres(nombres.trim())
                    .apellidoPaterno(apellidoPaterno != null ? apellidoPaterno.trim() : null)
                    .apellidoMaterno(apellidoMaterno != null ? apellidoMaterno.trim() : null)
                    .tipoDocumento(tipoDoc)
                    .numeroDocumento(doc)
                    .telefono(telefono)
                    .direccion(direccion)
                    .referencia(referencia)
                    .ubigeo(ubigeo)
                    .fechaNacimiento(fechaNacimiento)
                    .fechaRegistro(LocalDateTime.now())
                    .vigente(true)
                    .build());
        } else {
            persona.setNombres(nombres.trim());
            if (apellidoPaterno != null && !apellidoPaterno.isBlank()) {
                persona.setApellidoPaterno(apellidoPaterno.trim());
            }
            if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
                persona.setApellidoMaterno(apellidoMaterno.trim());
            }
            if (tipoDoc != null) {
                persona.setTipoDocumento(tipoDoc);
            }
            if (doc != null) {
                persona.setNumeroDocumento(doc);
            }
            if (telefono != null) {
                persona.setTelefono(telefono);
            }
            if (direccion != null) {
                persona.setDireccion(direccion);
            }
            if (referencia != null) {
                persona.setReferencia(referencia);
            }
            if (ubigeo != null) {
                persona.setUbigeo(ubigeo);
            }
            if (fechaNacimiento != null) {
                persona.setFechaNacimiento(fechaNacimiento);
            }
            persona.setVigente(true);
            persona = personaRepository.save(persona);
        }

        // Se reusa la cuenta existente de esa misma persona si la tiene
        Usuario usuario;
        if (usuarioExistente != null) {
            usuario = usuarioExistente;
            usuario.setPersona(persona);
            usuario.setRol(rol);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setEstado("activo");
        } else {
            usuario = Usuario.builder()
                    .persona(persona)
                    .rol(rol)
                    .correo(correoLimpio)
                    .password(passwordEncoder.encode(password))
                    .estado("activo")
                    .build();
        }
        usuarioRepository.save(usuario);

        Empleado empleado = empleadoRepository.findFirstByPersona(persona).orElse(null);
        if (empleado == null) {
            empleado = Empleado.builder().persona(persona).build();
        }
        empleado.setFechaContrato(fechaContrato);
        empleado.setSalario(salario);
        empleado.setCargo(rol.getNombre().toUpperCase());
        empleado.setVigente(true);
        return empleadoRepository.save(empleado);
    }

    @Transactional
    public Empleado ActualizarEmpleadoCompleto(Long id, String nombres, String apellidoPaterno, String apellidoMaterno,
            Long idTipoDocumento, String numeroDocumento, String telefono, LocalDate fechaNacimiento,
            String direccion, String referencia, Long idUbigeo,
            String correo, String password, Long idRol,
            LocalDate fechaContrato, BigDecimal salario, Boolean vigente) {

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
        Persona persona = empleado.getPersona();
        if (persona == null) {
            throw new RuntimeException("El empleado no tiene persona asociada");
        }

        persona.setNombres(nombres != null && !nombres.isBlank() ? nombres.trim() : persona.getNombres());
        if (apellidoPaterno != null && !apellidoPaterno.isBlank()) {
            persona.setApellidoPaterno(apellidoPaterno.trim());
        }
        if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
            persona.setApellidoMaterno(apellidoMaterno.trim());
        }
        if (idTipoDocumento != null) {
            persona.setTipoDocumento(
                    tipoDocumentoRepository.findById(idTipoDocumento).orElse(persona.getTipoDocumento()));
        }
        if (numeroDocumento != null && !numeroDocumento.isBlank()) {
            String docLimpio = numeroDocumento.trim();
            Persona personaConMismoDoc = personaRepository.findFirstByNumeroDocumento(docLimpio).orElse(null);
            if (personaConMismoDoc != null && !personaConMismoDoc.getIdPersona().equals(persona.getIdPersona())) {
                throw new RuntimeException("El número de documento ya está registrado por otra persona");
            }
            persona.setNumeroDocumento(docLimpio);
        }
        if (telefono != null) {
            persona.setTelefono(telefono.trim());
        }
        if (direccion != null) {
            persona.setDireccion(direccion.trim());
        }
        if (referencia != null) {
            persona.setReferencia(referencia.trim());
        }
        if (idUbigeo != null) {
            ubigeoRepository.findById(idUbigeo).ifPresent(persona::setUbigeo);
        }
        if (fechaNacimiento != null) {
            persona.setFechaNacimiento(fechaNacimiento);
        }
        personaRepository.save(persona);

        Usuario usuarioPropio = usuarioRepository.findFirstByPersona(persona).orElse(null);

        if (correo != null && !correo.isBlank()) {
            String correoLimpio = correo.trim().toLowerCase();
            Usuario usuarioCorreo = usuarioRepository.findByCorreo(correoLimpio).orElse(null);

            // El correo no puede pertenecer a otra persona
            if (usuarioCorreo != null && (usuarioPropio == null
                    || !usuarioCorreo.getIdUsuario().equals(usuarioPropio.getIdUsuario()))) {
                throw new RuntimeException("El correo ya esta registrado por otra cuenta");
            }

            Usuario usuario = usuarioCorreo != null ? usuarioCorreo : usuarioPropio;
            if (usuario == null) {
                String passFinal = (password != null && !password.isBlank()) ? password : "TechZone2026*";
                if (password != null && !password.isBlank()) {
                    UsuarioService.validarContrasenaSegura(password);
                }
                Rol rolInicial = idRol != null ? rolRepository.findById(idRol).orElse(null) : null;
                if (rolInicial == null) {
                    rolInicial = rolRepository.findById(2L).orElse(null);
                }
                usuario = new Usuario();
                usuario.setPersona(persona);
                usuario.setRol(rolInicial);
                usuario.setCorreo(correoLimpio);
                usuario.setPassword(passwordEncoder.encode(passFinal));
                usuario.setContrasena(passwordEncoder.encode(passFinal));
                usuario.setEstado(vigente != null && !vigente ? "inactivo" : "activo");
                usuario.setFechaCreacion(LocalDateTime.now());
                usuario.setIntentosFallidos(0);
            } else {
                usuario.setPersona(persona);
                usuario.setCorreo(correoLimpio);
                if (password != null && !password.isBlank()) {
                    UsuarioService.validarContrasenaSegura(password);
                    usuario.setPassword(passwordEncoder.encode(password));
                }
                if (vigente != null) {
                    usuario.setEstado(vigente ? "activo" : "inactivo");
                }
            }
            if (idRol != null) {
                Rol rolNuevo = rolRepository.findById(idRol)
                        .orElseThrow(() -> new RuntimeException("El rol seleccionado no existe"));
                usuario.setRol(rolNuevo);
                empleado.setCargo(rolNuevo.getNombre().toUpperCase());
            }
            usuarioRepository.save(usuario);
        } else {
            if (idRol != null) {
                Rol rolNuevo = rolRepository.findById(idRol)
                        .orElseThrow(() -> new RuntimeException("El rol seleccionado no existe"));
                if (usuarioPropio != null) {
                    usuarioPropio.setRol(rolNuevo);
                    if (vigente != null) {
                        usuarioPropio.setEstado(vigente ? "activo" : "inactivo");
                    }
                    usuarioRepository.save(usuarioPropio);
                }
                empleado.setCargo(rolNuevo.getNombre().toUpperCase());
            } else if (vigente != null && usuarioPropio != null) {
                usuarioPropio.setEstado(vigente ? "activo" : "inactivo");
                usuarioRepository.save(usuarioPropio);
            }
        }

        if (fechaContrato != null) {
            empleado.setFechaContrato(fechaContrato);
        }
        if (salario != null) {
            validarSalario(salario);
            empleado.setSalario(salario);
        }
        if (vigente != null) {
            empleado.setVigente(vigente);
        }
        return empleadoRepository.save(empleado);
    }

    @Transactional
    public Empleado ActualizarEmpleado(Long id, Empleado empleado) {
        Empleado existente = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
        if (empleado.getSalario() != null) {
            validarSalario(empleado.getSalario());
            existente.setSalario(empleado.getSalario());
        }
        if (empleado.getFechaContrato() != null) {
            existente.setFechaContrato(empleado.getFechaContrato());
        }
        if (empleado.getVigente() != null) {
            existente.setVigente(empleado.getVigente());
            if (existente.getPersona() != null) {
                usuarioRepository.findFirstByPersona(existente.getPersona()).ifPresent(u -> {
                    u.setEstado(empleado.getVigente() ? "activo" : "inactivo");
                    usuarioRepository.save(u);
                });
            }
        }
        if (empleado.getPersona() != null && existente.getPersona() != null) {
            Persona pActual = existente.getPersona();
            Persona pNueva = empleado.getPersona();
            if (pNueva.getTelefono() != null) pActual.setTelefono(pNueva.getTelefono());
            if (pNueva.getDireccion() != null) pActual.setDireccion(pNueva.getDireccion());
            if (pNueva.getReferencia() != null) pActual.setReferencia(pNueva.getReferencia());
            if (pNueva.getUbigeo() != null) pActual.setUbigeo(pNueva.getUbigeo());
            if (pActual != null) {
                pActual = personaRepository.save(pActual);
                existente.setPersona(pActual);
            }
        }
        return empleadoRepository.save(existente);
    }

    private void validarSalario(BigDecimal salario) {
        if (salario == null) {
            throw new RuntimeException("El salario es obligatorio");
        }
        if (salario.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El salario no puede ser negativo");
        }
    }

    @Transactional
    public void EliminarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
        empleado.setVigente(false);
        empleadoRepository.save(empleado);
        if (empleado.getPersona() != null) {
            usuarioRepository.findFirstByPersona(empleado.getPersona()).ifPresent(u -> {
                u.setEstado("inactivo");
                usuarioRepository.save(u);
            });
        }
    }
}
