package com.imsas.erp.modules.artes;

import java.time.Instant;
import java.util.UUID;

public record ArteResponse(
        UUID    id,
        String  codigo,
        UUID    empresaId,
        String  empresaRazonSocial,
        UUID    marcaId,
        String  marcaNombre,
        UUID    productoId,
        String  productoNombre,
        Short   versionActual,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {

    
    public static ArteResponse from(Arte arte) {
        return new ArteResponse(
                arte.getId(),
                arte.getCodigo(),
                arte.getEmpresa().getId(),
                arte.getEmpresa().getRazonSocial(),
                arte.getMarca().getId(),
                arte.getMarca().getNombre(),
                arte.getProducto().getId(),
                arte.getProducto().getNombre(),
                arte.getVersionActual(),
                arte.isActivo(),
                arte.getCreadoEn(),
                arte.getActualizadoEn()
        );
    }
}
