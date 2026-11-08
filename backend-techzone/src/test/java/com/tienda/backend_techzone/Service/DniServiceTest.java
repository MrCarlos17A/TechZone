package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Persona;
import com.tienda.backend_techzone.Repository.PersonaRepository;
import com.tienda.backend_techzone.Repository.TipoDocumentoRepository;
import com.tienda.backend_techzone.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DniServiceTest {

    private PersonaRepository personaRepository;
    private UsuarioRepository usuarioRepository;
    private TipoDocumentoRepository tipoDocumentoRepository;
    private DniService dniService;

    private Persona personaEncontrada;

    @BeforeEach
    void setUp() {
        personaEncontrada = null;

        personaRepository = (PersonaRepository) Proxy.newProxyInstance(
                PersonaRepository.class.getClassLoader(),
                new Class<?>[]{PersonaRepository.class},
                (proxy, method, args) -> {
                    if ("findFirstByTipoDocumentoAndNumeroDocumento".equals(method.getName())) {
                        return Optional.ofNullable(personaEncontrada);
                    }
                    return null;
                }
        );

        usuarioRepository = (UsuarioRepository) Proxy.newProxyInstance(
                UsuarioRepository.class.getClassLoader(),
                new Class<?>[]{UsuarioRepository.class},
                (proxy, method, args) -> {
                    if (method.getReturnType().equals(Optional.class)) {
                        return Optional.empty();
                    }
                    return null;
                }
        );

        com.tienda.backend_techzone.Model.TipoDocumento tdDni = new com.tienda.backend_techzone.Model.TipoDocumento();
        tdDni.setIdTipoDocumento(1L);
        tdDni.setNombre("DNI");

        tipoDocumentoRepository = (TipoDocumentoRepository) Proxy.newProxyInstance(
                TipoDocumentoRepository.class.getClassLoader(),
                new Class<?>[]{TipoDocumentoRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.of(tdDni);
                    }
                    return Optional.empty();
                }
        );

        dniService = new DniService(personaRepository, usuarioRepository, tipoDocumentoRepository);
    }

    @Test
    @DisplayName("Debe rechazar consulta si el numero de documento es nulo o vacio")
    void debeRechazarDocumentoVacio() {
        DniService.DocumentoResponse resNulo = dniService.consultarDocumento(1L, null);
        assertFalse(resNulo.ok());
        assertEquals("El numero de documento es obligatorio", resNulo.mensaje());

        DniService.DocumentoResponse resEspacios = dniService.consultarDocumento(1L, "   ");
        assertFalse(resEspacios.ok());
        assertEquals("El numero de documento es obligatorio", resEspacios.mensaje());
    }

    @Test
    @DisplayName("Debe retornar datos no encontrados si el usuario no existe")
    void debeRetornarNoEncontradoParaUsuarioInexistente() {
        DniService.MisDatos datos = dniService.obtenerMisDatos("desconocido@techzone.pe");
        assertNotNull(datos);
        assertFalse(datos.ok(), "Debe indicar ok=false para usuario que no existe");
        assertEquals("desconocido@techzone.pe", datos.correo());
    }

    @Test
    @DisplayName("Debe retornar fecha de nacimiento cuando existe en BD local")
    void debeRetornarFechaNacimientoLocal() {
        Persona persona = new Persona();
        persona.setIdPersona(5L);
        persona.setNumeroDocumento("45678901");
        persona.setNombres("Carlos");
        persona.setApellidoPaterno("Arroyo");
        persona.setApellidoMaterno("Vega");
        persona.setFechaNacimiento(LocalDate.of(1998, 8, 15));
        this.personaEncontrada = persona;

        DniService.DocumentoResponse resp = dniService.consultarDocumento(1L, "45678901");

        assertTrue(resp.ok());
        assertNotNull(resp.datos());
        assertEquals("1998-08-15", resp.datos().fechaNacimiento());
        assertEquals("Carlos", resp.datos().nombres());
    }
}
