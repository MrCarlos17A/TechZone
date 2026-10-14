package com.Tienda.TechZone.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final UsuarioDetalleService usuarioDetalleService;

    public SecurityConfig(UsuarioDetalleService usuarioDetalleService) {
        this.usuarioDetalleService = usuarioDetalleService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetalleService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/productos", "/novedades", "/marcas", "/buscar").permitAll()
                .requestMatchers("/login", "/registro", "/acceso-denegado", "/api/login").permitAll()
                .requestMatchers("/css/**", "/js/**", "/assets/**", "/Assets/**", "/uploads/**", "/favicon.ico", "/images/**").permitAll()
                .requestMatchers("/api/carrito/**", "/api/ubigeos", "/api/consulta-documento").permitAll()
                .requestMatchers("/debug/**").hasRole("Administrador")
                .requestMatchers("/mi-perfil", "/mis-pedidos", "/actualizar-perfil", "/checkout").authenticated()
                .requestMatchers("/admin/usuarios/**", "/admin/roles/**", "/admin/configuracion/**").hasRole("Administrador")
                .requestMatchers("/admin/productos/**", "/admin/marcas/**", "/admin/categorias/**").hasAnyRole("Administrador", "Trabajador")
                .requestMatchers("/admin/pedidos/**", "/admin/detalles-pedido/**").hasAnyRole("Administrador", "Trabajador")
                .requestMatchers("/admin/**").hasAnyRole("Administrador", "Trabajador")
                .requestMatchers("/admin").hasAnyRole("Administrador", "Trabajador")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureHandler(failureHandler())
                .successHandler(successHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(denegadoHandler())
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/css/**", "/js/**", "/assets/**", "/api/**")
            );
        return http.build();
    }

    private AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            log.info("LOGIN EXITOSO: {} | Roles: {}", authentication.getName(), authentication.getAuthorities());
            String referencia = request.getHeader("Referer");
            String destino = "/";
            if (referencia != null && !referencia.isEmpty()) {
                try {
                    java.net.URI uri = java.net.URI.create(referencia);
                    destino = uri.getRawPath();
                    String query = uri.getQuery();
                    if (query != null && !query.isEmpty()) {
                        StringBuilder limpio = new StringBuilder();
                        for (String par : query.split("&")) {
                            if (!par.startsWith("error=") && !par.startsWith("abrirLogin=")
                                    && !par.startsWith("exitosa=")) {
                                if (limpio.length() > 0) limpio.append('&');
                                limpio.append(par);
                            }
                        }
                        if (limpio.length() > 0) {
                            destino += "?" + limpio;
                        }
                    }
                    if (destino.equals("/login") || destino.equals("/registro")
                            || destino.startsWith("/login?") || destino.startsWith("/registro?")) {
                        destino = "/";
                    }
                } catch (Exception e) {
                    destino = "/";
                }
            }
            response.sendRedirect(destino);
        };
    }

    private AccessDeniedHandler denegadoHandler() {
        return (HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException exception) -> {
            log.warn("ACCESO DENEGADO: {} intento acceder a {}",
                    request.getRemoteUser(), request.getRequestURI());
            String base = request.getContextPath();
            String uri = request.getRequestURI();
            String prefijo = base + "/admin/";
            if (uri.startsWith(prefijo) && uri.length() > prefijo.length()) {
                String resto = uri.substring(prefijo.length());
                int barra = resto.indexOf('/');
                String seccion = barra >= 0 ? resto.substring(0, barra) : resto;
                response.sendRedirect(base + "/admin/acceso-denegado?seccion=" + seccion);
            } else if (uri.equals(base + "/admin")) {
                response.sendRedirect(base + "/admin/acceso-denegado");
            } else {
                response.sendRedirect(base + "/acceso-denegado");
            }
        };
    }

    private AuthenticationFailureHandler failureHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
            log.warn("LOGIN FALLIDO");
            String referencia = request.getHeader("Referer");
            String destino = "/?error=true";
            if (referencia != null && !referencia.isEmpty()) {
                try {
                    java.net.URI uri = java.net.URI.create(referencia);
                    destino = uri.getRawPath() + "?error=true";
                    if (destino.startsWith("/login") || destino.startsWith("/registro")) {
                        destino = "/?error=true";
                    }
                } catch (Exception e) {
                    destino = "/?error=true";
                }
            }
            response.sendRedirect(destino);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
