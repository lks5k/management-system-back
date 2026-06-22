package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.shared.enums.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para cambio de estado de una solicitud.
 * Si {@code nuevoEstado} es CANCELADO, {@code observacionCancelacion} es obligatoria (RN-13).
 * La validación de esta regla condicional se realiza en el servicio.
 */
public record CambiarEstadoRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoSolicitud nuevoEstado,

        /** Obligatorio cuando nuevoEstado == CANCELADO (RN-13). */
        String observacionCancelacion
) {}
