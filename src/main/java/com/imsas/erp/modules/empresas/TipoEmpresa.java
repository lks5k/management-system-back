package com.imsas.erp.modules.empresas;

/**
 * Tipo de constitución legal de la empresa.
 *
 * <p>Valores según MODELO DE DATOS V1.1.
 */
public enum TipoEmpresa {

    /** Sociedad por Acciones Simplificada. */
    SAS,

    /** Sociedad Anónima. */
    SA,

    /** Sociedad de Responsabilidad Limitada. */
    LTDA,

    /** Persona natural que ejerce actividad comercial. */
    PERSONA_NATURAL,

    /** Otra forma jurídica no contemplada en las anteriores. */
    OTRO
}
