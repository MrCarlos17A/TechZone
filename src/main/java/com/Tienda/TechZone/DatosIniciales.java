package com.Tienda.TechZone;

import com.Tienda.TechZone.Model.Persona;
import com.Tienda.TechZone.Model.Rol;
import com.Tienda.TechZone.Model.TipoDocumento;
import com.Tienda.TechZone.Model.Ubigeo;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.PersonaRepository;
import com.Tienda.TechZone.Repository.RolRepository;
import com.Tienda.TechZone.Repository.TipoDocumentoRepository;
import com.Tienda.TechZone.Repository.UbigeoRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final RolRepository rolRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UbigeoRepository ubigeoRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(UsuarioRepository usuarioRepository,
            PersonaRepository personaRepository,
            RolRepository rolRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            UbigeoRepository ubigeoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.rolRepository = rolRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.ubigeoRepository = ubigeoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByCorreo("admin@techzone.com").isPresent()) {
            log.info("Admin ya existe, no se crea.");
            return;
        }

        TipoDocumento dni = tipoDocumentoRepository.findById(1L).orElse(null);
        Ubigeo ubigeo = ubigeoRepository.findById(140122L).orElse(null);
        Rol rolAdmin = rolRepository.findById(1L).orElse(null);

        if (dni == null || ubigeo == null || rolAdmin == null) {
            log.warn("Faltan datos base (tipo documento, ubigeo o rol). No se crea admin.");
            return;
        }

        Persona persona = Persona.builder()
                .nombres("CARLOS FELIPE")
                .apellidoPaterno("ARROYO")
                .apellidoMaterno("VEGA")
                .tipoDocumento(dni)
                .numeroDocumento("73234768")
                .telefono("967662103")
                .direccion("Av. Santa Ana")
                .referencia("Plaza")
                .ubigeo(ubigeo)
                .fechaNacimiento(LocalDate.of(2005, 4, 13))
                .fechaRegistro(LocalDateTime.now())
                .vigente(true)
                .build();
        persona = personaRepository.save(persona);

        Usuario usuario = Usuario.builder()
                .persona(persona)
                .rol(rolAdmin)
                .correo("admin@techzone.com")
                .password(passwordEncoder.encode("Admin123**"))
                .estado("activo")
                .build();
        usuarioRepository.save(usuario);

        log.info("Admin creado: admin@techzone.com / Admin123**");
    }
}
