package com.tienda.backend_techzone.Security;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.Base64URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Emite y verifica tokens JWT firmados con HMAC-SHA256 (HS256).
 * La clave viene de la variable de entorno JWT_SECRET (.env).
 */
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoderAdapter decoder;
    private final long duracionMinutos;
    private final long refrescoDias;

    public JwtService(
            @Value("${jwt.secret}") String secreto,
            @Value("${jwt.duracion-minutos}") long duracionMinutos,
            @Value("${jwt.refresco-dias}") long refrescoDias) {

        if (secreto == null || secreto.isBlank() || secreto.length() < 32) {
            throw new IllegalStateException(
                    "jwt.secret debe tener al menos 32 caracteres. Definela en .env como JWT_SECRET.");
        }

        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        SecretKey clave = new SecretKeySpec(bytes, "HmacSHA256");

        OctetSequenceKey jwk = new OctetSequenceKey.Builder(Base64URL.encode(bytes))
                .algorithm(JWSAlgorithm.HS256)
                .keyID("techzone-hs256")
                .build();
        JWKSource<SecurityContext> fuente = (selector, context) -> selector.select(new JWKSet(jwk));

        this.encoder = new NimbusJwtEncoder(fuente);
        this.decoder = new JwtDecoderAdapter(NimbusJwtDecoder.withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build());
        this.duracionMinutos = duracionMinutos;
        this.refrescoDias = refrescoDias;
    }

    public String generarToken(String correo, Collection<String> roles, Long idUsuario) {
        return firmar(correo, roles, idUsuario, Duration.ofMinutes(duracionMinutos));
    }

    public String generarTokenRefresco(String correo, Collection<String> roles, Long idUsuario) {
        return firmar(correo, roles, idUsuario, Duration.ofDays(refrescoDias));
    }

    private String firmar(String correo, Collection<String> roles, Long idUsuario, Duration validez) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("techzone-backend")
                .subject(correo)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(validez))
                .claim("roles", List.copyOf(roles))
                .claim("idUsuario", idUsuario)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type(JOSEObjectType.JWT.getType())
                .build();

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * Devuelve el correo (subject) del token. Lanza excepcion si es invalido o
     * vencio.
     */
    public String obtenerCorreo(String token) {
        return decoder.decodificar(token).getSubject();
    }

    public List<String> obtenerRoles(String token) {
        List<String> roles = decoder.decodificar(token).getClaimAsStringList("roles");
        return roles == null ? List.of() : roles;
    }

    public Long obtenerIdUsuario(String token) {
        Jwt jwt = decoder.decodificar(token);
        Object valor = jwt.getClaim("idUsuario");
        return (valor instanceof Number n) ? n.longValue() : null;
    }

    public boolean esValido(String token) {
        try {
            decoder.decodificar(token);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Evita exponer JwtDecoder en la interfaz; solo se usa internamente. */
    private record JwtDecoderAdapter(org.springframework.security.oauth2.jwt.JwtDecoder impl) {
        Jwt decodificar(String token) {
            return impl.decode(token);
        }
    }
}
