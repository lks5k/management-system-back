package com.imsas.erp.shared.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Representa un error individual dentro del array {@code errors} del envelope de respuesta.
 *
 * <p>Cuando la solicitud es exitosa el array {@code errors} está vacío. Cuando falla,
 * contiene uno o más {@code ApiError} describiendo qué salió mal.
 *
 * <p>Ejemplos de uso:
 * <ul>
 *   <li>Error de validación: {@code field="nombre", message="El nombre es obligatorio"}</li>
 *   <li>Error de negocio: {@code field=null, message="El código ya existe"}</li>
 * </ul>
 */
@Value
@Builder
public class ApiError {

    /**
     * Campo específico que originó el error. {@code null} cuando el error es global
     * (no asociado a un campo puntual).
     */
    String field;

    /**
     * Descripción del error en español, legible para el usuario final.
     */
    String message;

    /**
     * Código interno del error para que el frontend pueda reaccionar programáticamente
     * sin depender del texto del mensaje (p. ej. {@code "VALIDATION_ERROR"},
     * {@code "ENTITY_NOT_FOUND"}).
     */
    String code;

    /**
     * Crea un {@code ApiError} de validación asociado a un campo específico.
     *
     * @param field   nombre del campo que falló la validación
     * @param message descripción del error
     * @return nueva instancia de {@code ApiError}
     */
    public static ApiError validation(String field, String message) {
        return ApiError.builder()
                .field(field)
                .message(message)
                .code("VALIDATION_ERROR")
                .build();
    }

    /**
     * Crea un {@code ApiError} global (no asociado a ningún campo).
     *
     * @param code    código interno del error
     * @param message descripción del error
     * @return nueva instancia de {@code ApiError}
     */
    public static ApiError of(String code, String message) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .build();
    }
}
