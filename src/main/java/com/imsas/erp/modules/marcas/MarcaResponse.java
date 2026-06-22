package com.imsas.erp.modules.marcas;

import java.time.Instant;
import java.util.UUID;

public record MarcaResponse(
        UUID id,
        String nombre,
        UUID empresaId,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {

    
    public static MarcaResponse from(Marca marca) {
        return new MarcaResponse(
                marca.getId(),
                marca.getNombre(),
                marca.getEmpresa().getId(),
                marca.isActivo(),
                marca.getCreadoEn(),
                marca.getActualizadoEn()
        );
    }
}
