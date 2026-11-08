package com.tienda.backend_techzone.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET_TEST = "b1e55296f1efd85a92828162e126b7462039307a210da139a600e912c62d6cc54e7b4f85eea9c51ef28408d54beca088";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_TEST, 15L, 7L);
    }

    @Test
    @DisplayName("Debe generar un token JWT valido con roles, correo e ID de usuario")
    void debeGenerarTokenValido() {
        String correo = "admin@techzone.pe";
        List<String> roles = List.of("ROLE_ADMINISTRADOR");
        Long idUsuario = 1L;

        String token = jwtService.generarToken(correo, roles, idUsuario);

        assertNotNull(token, "El token generado no debe ser nulo");
        assertFalse(token.isBlank(), "El token generado no debe estar vacio");
        assertTrue(jwtService.esValido(token), "El token generado debe ser valido");

        assertEquals(correo, jwtService.obtenerCorreo(token), "El correo en el token debe coincidir");
        assertEquals(roles, jwtService.obtenerRoles(token), "Los roles en el token deben coincidir");
        assertEquals(idUsuario, jwtService.obtenerIdUsuario(token), "El idUsuario debe coincidir");
    }

    @Test
    @DisplayName("Debe validar un token recien generado y rechazar uno alterado")
    void debeValidarTokenCorrecto() {
        String correo = "cliente@techzone.pe";
        List<String> roles = List.of("ROLE_CLIENTE");

        String token = jwtService.generarToken(correo, roles, 2L);

        assertTrue(jwtService.esValido(token));
        assertFalse(jwtService.esValido(token + "_invalido"), "Un token alterado debe ser rechazado");
        assertFalse(jwtService.esValido("token.totalmente.invalido"));
    }

    @Test
    @DisplayName("Debe fallar al intentar instanciar JwtService con clave menor a 32 caracteres")
    void debeLanzarExcepcionConClaveInvalida() {
        assertThrows(IllegalStateException.class, () -> {
            new JwtService("clave_corta", 15L, 7L);
        }, "Debe arrojar excepcion si la clave secreta es menor a 32 caracteres");
    }
}
