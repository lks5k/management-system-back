package com.imsas.erp.shared.enums;

/**
 * Estado del ciclo de vida de una solicitud de producción.
 *
 * <p>Diagrama de transiciones válidas:
 * <pre>
 * BORRADOR → CONFIRMAR → PENDIENTE → COMPLETADO
 *                      ↘
 *                       CANCELADO
 * </pre>
 *
 * <p>Reglas críticas:
 * <ul>
 *   <li>RN-04: el código de solicitud se genera al pasar de {@link #BORRADOR} a {@link #PENDIENTE}.</li>
 *   <li>RN-13: pasar a {@link #CANCELADO} requiere {@code observacion_cancelacion} obligatoria.</li>
 *   <li>RN-14: una solicitud {@link #CANCELADO} no puede volver a {@link #BORRADOR}.</li>
 *   <li>{@link #CANCELADO} puede aplicarse desde cualquier estado excepto {@link #COMPLETADO}.</li>
 * </ul>
 */
public enum EstadoSolicitud {

    /**
     * Solicitud incompleta. El asesor la guarda para continuar después.
     * En este estado no tiene código asignado aún.
     */
    BORRADOR,

    /**
     * Esperando confirmación del cliente para proceder.
     */
    CONFIRMAR,

    /**
     * El cliente confirmó. Producción puede proceder.
     * Al llegar a este estado se genera el código de solicitud (RN-04).
     */
    PENDIENTE,

    /**
     * Solicitud finalizada. Estado terminal: no admite más transiciones.
     */
    COMPLETADO,

    /**
     * Solicitud anulada. Requiere {@code observacion_cancelacion} obligatoria (RN-13).
     * No puede volver a {@link #BORRADOR} (RN-14).
     */
    CANCELADO
}
