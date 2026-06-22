package com.imsas.erp.modules.marcas;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida para una marca.
 *
 * @param id         identificador único de la marca
 * @param nombre     nombre normalizado (trim + uppercase)
 * @param empresaId  ID de la empresa dueña de la marca
 * @param activo     estado lógico del registro
 * @param creadoEn   timestamp de creación en UTC
 * @param actualizadoEn timestamp de última modificación en UTC
 */
public record MarcaResponse(
        UUID id,
        String nombre,
        UUID empresaId,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {

    /**
     * Construye un {@link MarcaResponse} a partir de la entidad JPA.
     *
     * @param marca entidad persistida
     * @return DTO listo para serializar
     */
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
