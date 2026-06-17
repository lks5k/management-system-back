package com.imsas.erp.modules.auth;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * DTO de respuesta al inicio de sesión exitoso.
 *
 * <p>Devuelto dentro del envelope {@code ApiResponse<LoginResponse>} en
 * {@code POST /api/v1/auth/login}. El frontend debe almacenar {@code token}
 * y enviarlo en el header {@code Authorization: Bearer {token}} en cada
 * solicitud subsiguiente.
 */
@Value
@Builder
public class LoginResponse {

    /**
     * Token JWT firmado. El frontend lo incluye en el header {@code Authorization}.
     */
    String token;

    /**
     * Tipo de token. Siempre {@code "Bearer"}.
     */
    @Builder.Default
    String tipo = "Bearer";

    /** ID único del usuario autenticado. */
    UUID id;

    /** Nombre completo del usuario. */
    String nombre;

    /** Email del usuario autenticado. */
    String email;

    /**
     * Rol del usuario (p. ej. {@code "ADMIN"}, {@code "SALES_REP"}).
     * El frontend lo usa para mostrar/ocultar opciones de UI.
     */
    String rol;
}
