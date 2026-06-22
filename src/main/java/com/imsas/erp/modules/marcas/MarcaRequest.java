package com.imsas.erp.modules.marcas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear o actualizar una marca.
 *
 * <p>El {@code empresaId} no forma parte del body: se toma siempre
 * del path variable {@code /empresas/{empresaId}/marcas} en creación,
 * y del registro existente en actualización.
 */
public record MarcaRequest(

        /**
         * Nombre de la marca. Se normaliza (trim + uppercase) en el servicio antes
         * de persistir (RN-25) y antes de verificar duplicados (RN-11).
         */
        @NotBlank(message = "El nombre de la marca es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre
) {}
