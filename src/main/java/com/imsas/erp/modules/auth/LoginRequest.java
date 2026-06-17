package com.imsas.erp.modules.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

/**
 * DTO de solicitud de inicio de sesión.
 *
 * <p>Recibido en el body de {@code POST /api/v1/auth/login}.
 * Las validaciones JSR-380 son procesadas por el {@code GlobalExceptionHandler}
 * antes de que el request llegue al servicio.
 */
@Value
public class LoginRequest {

    /**
     * Correo electrónico del usuario. Actúa como identificador único (username).
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    String email;

    /**
     * Contraseña en texto plano. Se compara contra el hash BCrypt almacenado.
     * Nunca se registra en logs.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    String password;
}
