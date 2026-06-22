package com.imsas.erp.modules.productos;

import java.time.Instant;
import java.util.UUID;

public record ProductoResponse(
        UUID id,
        String nombre,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {

    
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
