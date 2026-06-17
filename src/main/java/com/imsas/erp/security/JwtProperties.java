package com.imsas.erp.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuración JWT leídas desde {@code application.yml}
 * bajo el prefijo {@code app.jwt}.
 *
 * <p>La clase está anotada con {@code @Validated} para que Spring valide los
 * valores en el arranque. Si {@code secret} está en blanco o {@code expirationMs}
 * no es positivo, la aplicación falla inmediatamente con un mensaje claro.
 *
 * <p>Ejemplo de configuración en {@code application.yml}:
 * <pre>{@code
 * app:
 *   jwt:
 *     secret: ${JWT_SECRET}
 *     expiration-ms: ${JWT_EXPIRATION_MS:86400000}
 * }</pre>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * Clave secreta Base64 para firmar los tokens JWT.
     * Debe tener al menos 256 bits (32 bytes) para HMAC-SHA256.
     * Se lee desde la variable de entorno {@code JWT_SECRET}.
     */
    @NotBlank(message = "JWT secret no puede estar en blanco")
    private String secret;

    /**
     * Tiempo de expiración del token en milisegundos.
     * Valor por defecto: 86400000 ms = 24 horas.
     */
    @Positive(message = "JWT expiration-ms debe ser mayor a cero")
    private long expirationMs = 86_400_000L;
}
