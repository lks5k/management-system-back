package com.imsas.erp.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    
    @NotBlank(message = "JWT secret no puede estar en blanco")
    private String secret;

    
    @Positive(message = "JWT expiration-ms debe ser mayor a cero")
    private long expirationMs = 86_400_000L;
}
