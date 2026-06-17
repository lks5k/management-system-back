package com.imsas.erp.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción base para errores de lógica de negocio de la aplicación.
 *
 * <p>Permite asociar un {@link HttpStatus} y un código interno al error,
 * de modo que el {@link GlobalExceptionHandler} pueda construir la respuesta
 * correcta sin lógica adicional en la capa de servicios.
 *
 * <p>Uso recomendado: lanzar subclases o instancias directas desde la capa de
 * servicio cuando se viola una regla de negocio (RN-xx).
 *
 * <pre>{@code
 * // Desde un servicio:
 * throw new BusinessException(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND",
 *     "Usuario no encontrado con id: " + id);
 *
 * // Con subclase:
 * throw new EntityNotFoundException("Usuario", id);
 * }</pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** Código HTTP que debe devolver la respuesta al cliente. */
    private final HttpStatus status;

    /**
     * Código interno legible por el frontend para reaccionar programáticamente
     * (p. ej. {@code "ENTITY_NOT_FOUND"}, {@code "DUPLICATE_CODE"}).
     */
    private final String errorCode;

    /**
     * Construye una {@code BusinessException} con status, código y mensaje.
     *
     * @param status    código HTTP de la respuesta
     * @param errorCode código interno del error
     * @param message   descripción del error en español
     */
    public BusinessException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Construye una {@code BusinessException} con causa raíz encadenada.
     *
     * @param status    código HTTP de la respuesta
     * @param errorCode código interno del error
     * @param message   descripción del error en español
     * @param cause     excepción original que provocó este error
     */
    public BusinessException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
