package com.imsas.erp.modules.productos;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida para un producto.
 *
 * @param id            identificador único del producto
 * @param nombre        nombre normalizado (trim + uppercase)
 * @param activo        estado lógico del registro
 * @param creadoEn      timestamp de creación en UTC
 * @param actualizadoEn timestamp de última modificación en UTC
 */
public record ProductoResponse(
        UUID id,
        String nombre,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {

    /**
     * Construye un {@link ProductoResponse} a partir de la entidad JPA.
     *
     * @param producto entidad persistida
     * @return DTO listo para serializar
     */
    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.isActivo(),
                producto.getCreadoEn(),
                producto.getActualizadoEn()
        );
    }
}
