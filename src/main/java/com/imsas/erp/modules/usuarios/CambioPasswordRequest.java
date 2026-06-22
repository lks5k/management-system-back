package com.imsas.erp.modules.usuarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para cambiar la contraseña de un usuario.
 *
 * <h3>Semántica de {@code passwordActual}</h3>
 * <ul>
 *   <li>Si el solicitante es el <b>propio usuario</b>: {@code passwordActual} es obligatorio
 *       y el servicio verifica que coincida con el hash almacenado.</li>
 *   <li>Si el solicitante es <b>ADMIN o SUPERADMIN</b> cambiando la contraseña de otro usuario:
 *       {@code passwordActual} puede ser {@code null} o vacío y se ignora.</li>
 * </ul>
 *
 * @param passwordActual contraseña actual en texto plano (requerida solo cuando se cambia
 *                       la propia contraseña; ignorada en reset administrativo)
 * @param passwordNuevo  nueva contraseña en texto plano (mín. 8 caracteres)
 */
public record CambioPasswordRequest(

        String passwordActual,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres")
        String passwordNuevo
) {}
