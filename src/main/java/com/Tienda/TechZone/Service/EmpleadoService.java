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
        if (usuarioRepository.findByCorreo(correo.trim()).isPresent()) {
            throw new RuntimeException("El correo ya esta registrado");
        }
        validarSalario(salario);

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

        Persona persona = personaRepository.save(Persona.builder()
                .nombres(nombres.trim())
                .apellidoPaterno(apellidoPaterno != null ? apellidoPaterno.trim() : null)
                .apellidoMaterno(apellidoMaterno != null ? apellidoMaterno.trim() : null)
                .tipoDocumento(tipoDoc)
                .numeroDocumento(numeroDocumento)
                .telefono(telefono)
                .direccion(direccion)
                .referencia(referencia)
                .ubigeo(ubigeo)
                .fechaNacimiento(fechaNacimiento)
                .fechaRegistro(LocalDateTime.now())
                .vigente(true)
                .build());

        usuarioRepository.save(Usuario.builder()
                .persona(persona)
                .rol(rol)
                .correo(correo.trim())
                .password(passwordEncoder.encode(password))
                .estado("activo")
                .build());

        return empleadoRepository.save(Empleado.builder()
                .persona(persona)
                .fechaContrato(fechaContrato)
                .salario(salario)
                .build());
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
        persona.setApellidoPaterno(apellidoPaterno != null ? apellidoPaterno.trim() : persona.getApellidoPaterno());
        persona.setApellidoMaterno(apellidoMaterno != null ? apellidoMaterno.trim() : persona.getApellidoMaterno());
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
            var usuarioOpt = usuarioRepository.findByCorreo(correo.trim());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                if (idRol != null && (idRol == 1 || idRol == 2)) {
                    rolRepository.findById(idRol).ifPresent(usuario::setRol);
                }
                if (password != null && !password.isBlank()) {
                    if (password.length() < 6) {
                        throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
                    }
                    usuario.setPassword(passwordEncoder.encode(password));
                }
                usuarioRepository.save(usuario);
            }
        }

        if (fechaContrato != null && fechaContrato.isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de contrato no puede ser futura");
        }
        empleado.setFechaContrato(fechaContrato != null ? fechaContrato : empleado.getFechaContrato());
        if (salario != null) {
        validarSalario(salario);
        if (fechaContrato != null && fechaContrato.isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de contrato no puede ser futura");
        }
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
        empleadoRepository.deleteById(id);
    }
}
