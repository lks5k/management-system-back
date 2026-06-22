package com.imsas.erp.modules.productos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear o actualizar un producto.
 *
 * <p>El nombre se normaliza (trim + uppercase) en el servicio antes de persistir
 * y antes de verificar unicidad (RN-25).
 */
public record ProductoRequest(

        /**
         * Nombre del producto. Único en todo el sistema (la tabla {@code productos}
         * tiene constraint {@code uq_producto_nombre}).
         */
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre
) {}
