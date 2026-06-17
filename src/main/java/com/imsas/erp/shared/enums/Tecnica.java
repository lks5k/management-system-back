package com.imsas.erp.shared.enums;

/**
 * Técnica de producción aplicada a la solicitud.
 *
 * <p>Define el proceso de fabricación que se usará para elaborar el producto
 * solicitado. Se almacena en la tabla {@code solicitudes}.
 */
public enum Tecnica {

    /** Impresión digital directa. */
    DIGITAL,

    /** Sublimación: transferencia de tinta por calor sobre poliéster. */
    SUBLIMACION,

    /** Flexografía: impresión con planchas flexibles de relieve. */
    FLEXOGRAFIA,

    /** Litografía: impresión offset sobre superficies planas. */
    LITOGRAFIA,

    /** Serigrafía (screen): impresión por malla con tinta. */
    SCREEN,

    /** Tejida: marquilla elaborada en telar jacquard. */
    TEJIDA,

    /** Transfer: diseño transferido al sustrato por calor y presión. */
    TRANSFER
}
