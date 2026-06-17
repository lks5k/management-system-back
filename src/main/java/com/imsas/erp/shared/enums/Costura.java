package com.imsas.erp.shared.enums;

/**
 * Posición o tipo de costura de la marquilla en la prenda.
 *
 * <p>Indica cómo y dónde se cose o adhiere el producto al artículo final.
 * Se almacena en la tabla {@code solicitudes}.
 */
public enum Costura {

    /** Costura en la parte superior de la marquilla. */
    ARRIBA,

    /** Costura en la parte inferior de la marquilla. */
    ABAJO,

    /** Costura en el lado izquierdo de la marquilla. */
    IZQUIERDA,

    /** Costura en el lado derecho de la marquilla. */
    DERECHA,

    /** La marquilla se entrega doblada para costura posterior. */
    PARA_DOBLAR,

    /** Costura en los cuatro lados de la marquilla. */
    TODOS_LOS_LADOS,

    /** La marquilla no lleva costura (p. ej. transfer o adhesivo). */
    SIN_COSTURA,

    /** Forma trapezoidal; costura en la base mayor. */
    TRAPECIO,

    /** Costura en dos lados opuestos de la marquilla. */
    A_DOS_LADOS
}
