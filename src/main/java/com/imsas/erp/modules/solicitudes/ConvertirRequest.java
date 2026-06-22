package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.shared.enums.TipoSolicitud;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para convertir una solicitud DISEÑO (o PEDIDO) en un PEDIDO o REPOSICION (CU-06).
 * Todos los campos del origen se copian; los aquí presentes los sobreescriben si no son nulos.
 */
public record ConvertirRequest(

        @NotNull(message = "El tipo destino es obligatorio")
        TipoSolicitud tipo,

        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad,

        BigDecimal ancho,

        BigDecimal largo,

        String ordenCompra,

        String observaciones
) {}
