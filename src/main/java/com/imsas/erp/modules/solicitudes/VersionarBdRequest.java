package com.imsas.erp.modules.solicitudes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para versionar el código Base de Datos de una solicitud (RN-07, RN-08).
 * El motivo es obligatorio y debe explicar la corrección estructural del cliente
 * (tallas, cantidades, colores, referencias).
 */
public record VersionarBdRequest(

        @NotBlank(message = "El motivo de versionado BD es obligatorio")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
        String motivo
) {}
