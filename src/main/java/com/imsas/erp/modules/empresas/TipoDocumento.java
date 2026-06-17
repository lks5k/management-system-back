package com.imsas.erp.modules.empresas;

/**
 * Tipo de documento de identidad o identificación tributaria de una empresa.
 *
 * <p>Valores según MODELO DE DATOS V1.1.
 */
public enum TipoDocumento {

    /** Número de Identificación Tributaria. Usado por personas jurídicas colombianas. */
    NIT,

    /** Cédula de Ciudadanía. Personas naturales colombianas. */
    CC,

    /** Cédula de Extranjería. Personas naturales extranjeras residentes en Colombia. */
    CE,

    /** Pasaporte. */
    PASAPORTE
}
