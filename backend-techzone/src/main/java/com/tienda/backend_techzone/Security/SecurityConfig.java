package com.tienda.backend_techzone.Security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Seguridad de la API: sin sesiones, sin CSRF (se usa Bearer) y con
 * los mismos roles que el proyecto original.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CacheControlFilter cacheControlFilter;
    private final UsuarioDetalleService usuarioDetalleService;
    private final List<String> origenesPermitidos;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CacheControlFilter cacheControlFilter,
                          UsuarioDetalleService usuarioDetalleService,
                          @Value("${app.cors.allowed-origins:*}") List<String> origenesPermitidos) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.cacheControlFilter = cacheControlFilter;
        this.usuarioDetalleService = usuarioDetalleService;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetalleService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- Rutas publicas de la API ---
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/productos/**",
                                "/api/marcas/**",
                                "/api/categorias/**",
                                "/api/configuracion",
                                "/api/medios-pago",
                                "/api/tipos-documento",
                                "/api/ubigeos/**",
                                "/api/consulta-documento",
                                "/api/dni/**",
                                "/api/ayuda/**",
                                "/api/carrito/**"
                        ).permitAll()

                        // --- Administracion estricta: Solo Administrador ---
                        .requestMatchers(
                                "/api/admin/usuarios/**",
                                "/api/admin/roles/**",
                                "/api/admin/configuracion/**",
                                "/api/admin/auditoria/**",
                                "/api/admin/empleados/**",
                                "/api/admin/colaboradores/**"
                        ).hasAnyRole("ADMINISTRADOR", "Administrador", "GERENTE")

                        // Acciones de eliminacion destructiva: Exclusivas del Administrador
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/**")
                        .hasAnyRole("ADMINISTRADOR", "Administrador")

                        // --- Administracion compartida: Administrador o Colaborador / Empleado ---
                        .requestMatchers("/api/admin/**")
                        .hasAnyRole("ADMINISTRADOR", "Administrador", "EMPLEADO", "Empleado", "COLABORADOR", "Colaborador", "TRABAJADOR", "Trabajador")

                        // --- Usuario / Cliente autenticado ---
                        .requestMatchers(
                                "/api/checkout",
                                "/api/mis-datos",
                                "/api/mis-pedidos/**",
                                "/api/perfil",
                                "/api/perfil/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            escribir(res, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesion para continuar");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            escribir(res, HttpServletResponse.SC_FORBIDDEN, "No tienes permisos para esta accion");
                        })
                )
                .addFilterBefore(cacheControlFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void escribir(HttpServletResponse res, int estado, String mensaje) throws java.io.IOException {
        res.setStatus(estado);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenesPermitidos);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }
}
