package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.*;
import com.Tienda.TechZone.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UbigeoRepository ubigeoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Empleado> ListarEmpleados() {
        return empleadoRepository.findAll();
    }

    public Empleado ObtenerEmpleadoPorId(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
    }

    public Empleado CrearEmpleado(Empleado empleado) {
        return empleadoRepository.save(empleado);
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
        if (password == null || password.length() < 6) {
            throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
        }
        if (idRol == null || (idRol != 1 && idRol != 2)) {
            throw new RuntimeException("Selecciona un rol valido (Administrador o Trabajador)");
        }
        validarSalario(salario);

        String correoLimpio = correo.trim();
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
        empleado.setVigente(true);
        return empleadoRepository.save(empleado);
    }

    @Transactional
    public Empleado ActualizarEmpleadoCompleto(Long id, String nombres, String apellidoPaterno, String apellidoMaterno,
            Long idTipoDocumento, String numeroDocumento, String telefono, LocalDate fechaNacimiento,
            String direccion, String referencia, Long idUbigeo,
            String correo, String password, Long idRol,
            LocalDate fechaContrato, BigDecimal salario) {

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
            persona.setTipoDocumento(tipoDocumentoRepository.findById(idTipoDocumento).orElse(persona.getTipoDocumento()));
        }
        persona.setNumeroDocumento(numeroDocumento != null ? numeroDocumento : persona.getNumeroDocumento());
        persona.setTelefono(telefono != null ? telefono : persona.getTelefono());
        persona.setDireccion(direccion != null ? direccion : persona.getDireccion());
        persona.setReferencia(referencia != null ? referencia : persona.getReferencia());
        if (idUbigeo != null) {
            ubigeoRepository.findById(idUbigeo).ifPresent(persona::setUbigeo);
        }
        if (fechaNacimiento != null) {
            persona.setFechaNacimiento(fechaNacimiento);
        }
        personaRepository.save(persona);

        if (correo != null && !correo.isBlank()) {
            String correoLimpio = correo.trim();
            Usuario usuarioPropio = usuarioRepository.findFirstByPersona(persona).orElse(null);
            Usuario usuarioCorreo = usuarioRepository.findByCorreo(correoLimpio).orElse(null);

            // El correo no puede pertenecer a otra persona
            if (usuarioCorreo != null && (usuarioPropio == null
                    || !usuarioCorreo.getIdUsuario().equals(usuarioPropio.getIdUsuario()))) {
                throw new RuntimeException("El correo ya esta registrado por otra cuenta");
            }

            Usuario usuario = usuarioCorreo != null ? usuarioCorreo : usuarioPropio;
            if (usuario == null) {
                if (password == null || password.isBlank()) {
                    throw new RuntimeException("La contrasena es obligatoria para crear la cuenta");
                }
                if (password.length() < 6) {
                    throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
                }
                usuario = Usuario.builder()
                        .persona(persona)
                        .correo(correoLimpio)
                        .password(passwordEncoder.encode(password))
                        .estado("activo")
                        .build();
            } else {
                usuario.setPersona(persona);
                usuario.setCorreo(correoLimpio);
                if (password != null && !password.isBlank()) {
                    if (password.length() < 6) {
                        throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
                    }
                    usuario.setPassword(passwordEncoder.encode(password));
                }
                usuario.setEstado("activo");
            }
            if (idRol != null && (idRol == 1 || idRol == 2)) {
                rolRepository.findById(idRol).ifPresent(usuario::setRol);
            } else if (usuario.getRol() == null) {
                throw new RuntimeException("Selecciona un rol valido (Administrador o Trabajador)");
            }
            usuarioRepository.save(usuario);
        }

        if (fechaContrato != null && fechaContrato.isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de contrato no puede ser futura");
        }
        empleado.setFechaContrato(fechaContrato != null ? fechaContrato : empleado.getFechaContrato());
        if (salario != null) {
            validarSalario(salario);
            empleado.setSalario(salario);
        }
        return empleadoRepository.save(empleado);
    }

    public Empleado ActualizarEmpleado(Long id, Empleado empleado) {
        Empleado existente = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
        validarSalario(empleado.getSalario());
        existente.setPersona(empleado.getPersona());
        existente.setFechaContrato(empleado.getFechaContrato());
        existente.setSalario(empleado.getSalario());
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

    public void EliminarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));
        empleado.setVigente(false);
        empleadoRepository.save(empleado);
    }
}
