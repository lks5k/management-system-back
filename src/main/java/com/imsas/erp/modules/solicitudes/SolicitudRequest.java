package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.shared.enums.Costura;
import com.imsas.erp.shared.enums.Tecnica;
import com.imsas.erp.shared.enums.TipoSolicitud;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de entrada para crear o actualizar una solicitud.
 * Las validaciones JSR-380 aplican sobre todos los campos obligatorios.
 */
public record SolicitudRequest(

        @NotNull(message = "La empresa es obligatoria")
        UUID empresaId,

        UUID contactoId,

        UUID marcaId,

        UUID productoId,

        @Size(max = 200, message = "El detalle de producto no puede superar 200 caracteres")
        String detalleProducto,

        /** Solicitud de origen para PEDIDO/REPOSICION derivados de un DISEÑO (RN-15). */
        UUID solicitudOrigenId,

        @NotNull(message = "El tipo de solicitud es obligatorio")
        TipoSolicitud tipo,

        String descripcion,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad,

        @NotNull(message = "La técnica es obligatoria")
        Tecnica tecnica,

        @DecimalMin(value = "0.0", inclusive = false, message = "El ancho debe ser mayor a cero")
        BigDecimal ancho,

        @DecimalMin(value = "0.0", inclusive = false, message = "El largo debe ser mayor a cero")
        BigDecimal largo,

        Short coloresFrente,

        Short coloresReverso,

        Costura costura,

        @Size(max = 60, message = "La orden de compra no puede superar 60 caracteres")
        String ordenCompra,

        @DecimalMin(value = "0.0", message = "El precio de referencia no puede ser negativo")
        BigDecimal precioReferencia,

        @DecimalMin(value = "0.0", message = "El abono no puede ser negativo")
        BigDecimal abono,

        String observaciones,

        /**
         * Número de versiones de Arte entregadas en esta solicitud DISEÑO (máx. 3).
         * Solo aplica en solicitudes tipo DISEÑO; se puede actualizar antes de completar.
         */
        Short cantidadVersionesEntregadas
) {}
