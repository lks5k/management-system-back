package com.imsas.erp.shared.enums;

/**
 * Tipo de solicitud de producción.
 *
 * <p>Define la naturaleza del requerimiento comercial. Determina el flujo
 * que seguirá la solicitud y las validaciones aplicables (RN-15).
 *
 * <p>Los tipos {@link #PEDIDO} y {@link #REPOSICION}, cuando se originan desde
 * un {@link #DISEÑO} aprobado, deben referenciar {@code solicitud_origen_id} (RN-15).
 */
public enum TipoSolicitud {

    /**
     * Primera solicitud del proceso. Se pide el diseño antes de producir.
     * Puede originar solicitudes de tipo {@link #PEDIDO} y {@link #REPOSICION}.
     */
    DISEÑO,

    /**
     * Solicitud de muestras físicas. No necesariamente precede un pedido en firme.
     */
    MUESTRAS,

    /**
     * Producción en firme. Puede originarse desde un {@link #DISEÑO} aprobado.
     * En ese caso, requiere {@code solicitud_origen_id} (RN-15).
     */
    PEDIDO,

    /**
     * Repetición de un pedido anterior. Puede heredar el código BD original
     * o generar uno nuevo si la base de datos cambió.
     * Requiere {@code solicitud_origen_id} cuando se origina desde un {@link #DISEÑO} (RN-15).
     */
    REPOSICION,

    /**
     * El cliente entrega un rollo de marquillas impreso para corte.
     */
    CORTE,

    /**
     * Préstamo de material o insumos al cliente, a un compañero o a otro proveedor.
     */
    PRESTAMO
}
