package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Persona;
import com.tienda.backend_techzone.Model.TipoDocumento;
import com.tienda.backend_techzone.Repository.PersonaRepository;
import com.tienda.backend_techzone.Repository.TipoDocumentoRepository;
import com.tienda.backend_techzone.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DniServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    private DniService dniService;

    @BeforeEach
    void setUp() {
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
        when(usuarioRepository.findByCorreo("desconocido@techzone.pe")).thenReturn(Optional.empty());

        DniService.MisDatos datos = dniService.obtenerMisDatos("desconocido@techzone.pe");
        assertNotNull(datos);
        assertFalse(datos.ok(), "Debe indicar ok=false para usuario que no existe");
        assertEquals("desconocido@techzone.pe", datos.correo());
    }

    @Test
    @DisplayName("Debe retornar fecha de nacimiento cuando existe en BD local")
    void debeRetornarFechaNacimientoLocal() {
        TipoDocumento td = new TipoDocumento();
        td.setIdTipoDocumento(1L);
        td.setNombre("DNI");

        Persona persona = new Persona();
        persona.setIdPersona(5L);
        persona.setNumeroDocumento("45678901");
        persona.setNombres("Carlos");
        persona.setApellidoPaterno("Arroyo");
        persona.setApellidoMaterno("Vega");
        persona.setFechaNacimiento(LocalDate.of(1998, 8, 15));

        when(tipoDocumentoRepository.findById(1L)).thenReturn(Optional.of(td));
        when(personaRepository.findFirstByTipoDocumentoAndNumeroDocumento(1L, "45678901"))
                .thenReturn(Optional.of(persona));
        when(usuarioRepository.findFirstByPersona(persona)).thenReturn(Optional.empty());

        DniService.DocumentoResponse resp = dniService.consultarDocumento(1L, "45678901");

        assertTrue(resp.ok());
        assertNotNull(resp.datos());
        assertEquals("1998-08-15", resp.datos().fechaNacimiento());
        assertEquals("Carlos", resp.datos().nombres());
    }
}
