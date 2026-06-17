package com.imsas.erp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio encargado de generar, firmar y validar tokens JWT.
 *
 * <p>Utiliza JJWT 0.12.x (split-jar). Los tokens se firman con HMAC-SHA256
 * usando la clave configurada en {@link JwtProperties#getSecret()}.
 *
 * <p>Claims incluidos en el token:
 * <ul>
 *   <li>{@code sub} — email del usuario (username de Spring Security)</li>
 *   <li>{@code rol} — nombre del rol (p. ej. {@code "ADMIN"})</li>
 *   <li>{@code iat} — timestamp de emisión</li>
 *   <li>{@code exp} — timestamp de expiración</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // ─── Generación ───────────────────────────────────────────────────────────

    /**
     * Genera un token JWT para el usuario dado, incluyendo el claim {@code rol}.
     *
     * @param userDetails detalles del usuario autenticado
     * @return token JWT compacto y firmado
     */
    public String generateToken(UserDetails userDetails) {
        String rol = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        return generateToken(Map.of("rol", rol), userDetails);
    }

    /**
     * Genera un token JWT con claims adicionales.
     *
     * @param extraClaims claims adicionales a incluir en el payload
     * @param userDetails detalles del usuario autenticado
     * @return token JWT compacto y firmado
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getExpirationMs()))
                .signWith(signingKey())
                .compact();
    }

    // ─── Extracción de claims ─────────────────────────────────────────────────

    /**
     * Extrae el {@code subject} (email) del token.
     *
     * @param token token JWT
     * @return email del usuario
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim individual del token usando un resolver funcional.
     *
     * @param token         token JWT
     * @param claimsResolver función que extrae el campo deseado del objeto {@link Claims}
     * @param <T>           tipo del claim
     * @return valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // ─── Validación ───────────────────────────────────────────────────────────

    /**
     * Valida que el token sea válido para el usuario dado.
     * Verifica firma, expiración y que el subject coincida con el username.
     *
     * @param token       token JWT
     * @param userDetails usuario contra el que validar
     * @return {@code true} si el token es válido
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica únicamente la firma y la expiración del token, sin cargar un usuario.
     * Usado en el filtro JWT para una validación previa antes de consultar la BD.
     *
     * @param token token JWT
     * @return {@code true} si el token tiene firma válida y no ha expirado
     */
    public boolean isTokenStructurallyValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token estructuralmente inválido: {}", e.getMessage());
            return false;
        }
    }

    // ─── Internos ─────────────────────────────────────────────────────────────

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Parsea y retorna todos los claims del token.
     * Lanza {@link JwtException} si la firma es inválida o el token está expirado.
     *
     * @param token token JWT
     * @return objeto {@link Claims} con todos los claims del payload
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Construye la clave HMAC-SHA256 a partir del secreto Base64 configurado.
     * La clave se construye en cada llamada de forma intencionada: el objeto
     * {@link SecretKey} es ligero y así evitamos guardar estado mutable.
     *
     * @return clave HMAC para firma y verificación
     */
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }
}
