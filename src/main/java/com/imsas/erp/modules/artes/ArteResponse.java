package com.imsas.erp.modules.artes;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de solo lectura que representa un Arte en las respuestas de la API.
 *
 * <p>Incluye los datos desnormalizados de la empresa, marca y producto asociados,
 * evitando que el frontend tenga que hacer llamadas adicionales para obtener los nombres.
 *
 * <p>El campo {@code activo} permite al frontend distinguir entre Artes vigentes
 * ({@code activo = true}) e históricos ({@code activo = false}) que han sido
 * supersedidos al alcanzar la versión máxima ({@code 99}).
 *
 * <p>Construido exclusivamente mediante el factory method {@link #from(Arte)},
 * que accede a las asociaciones LAZY una vez inicializadas por el repositorio
 * (vía {@code @EntityGraph}) o dentro de una transacción abierta.
 */
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

    /**
     * Construye un {@code ArteResponse} a partir de una entidad {@link Arte}.
     *
     * <p>Requiere que las asociaciones {@code empresa}, {@code marca} y {@code producto}
     * estén inicializadas (bien por {@code @EntityGraph} en la query de lista,
     * bien dentro de una transacción activa para la consulta por ID).
     *
     * @param arte entidad Arte con sus relaciones cargadas
     * @return DTO listo para serializar
     */
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
