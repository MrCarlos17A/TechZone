package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Repository.PersonaRepository;
import com.tienda.backend_techzone.Repository.TipoDocumentoRepository;
import com.tienda.backend_techzone.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

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
        DniService.MisDatos datos = dniService.obtenerMisDatos("desconocido@techzone.pe");
        assertNotNull(datos);
        assertFalse(datos.ok(), "Debe indicar ok=false para usuario que no existe");
        assertEquals("desconocido@techzone.pe", datos.correo());
    }
}
