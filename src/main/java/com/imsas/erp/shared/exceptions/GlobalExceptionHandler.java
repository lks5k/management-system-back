package com.imsas.erp.shared.exceptions;

import com.imsas.erp.shared.dto.ApiError;
import com.imsas.erp.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Manejador global de excepciones para toda la API REST.
 *
 * <p>Intercepta excepciones lanzadas en cualquier {@code @RestController} y las
 * convierte en respuestas con el envelope estándar {@link ApiResponse}, garantizando
 * que el frontend siempre reciba el mismo formato independientemente del tipo de error.
 *
 * <p>Jerarquía de handlers (de más específico a más general):
 * <ol>
 *   <li>{@link MethodArgumentNotValidException} → 400 (validación JSR-380)</li>
 *   <li>{@link HttpMessageNotReadableException} → 400 (JSON malformado)</li>
 *   <li>{@link MethodArgumentTypeMismatchException} → 400 (tipo incorrecto en path/query)</li>
 *   <li>{@link EntityNotFoundException} → 404</li>
 *   <li>{@link BusinessException} → status dinámico</li>
 *   <li>{@link AuthenticationException} → 401</li>
 *   <li>{@link AccessDeniedException} → 403</li>
 *   <li>{@link Exception} → 500 (fallback genérico)</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Validación JSR-380 ───────────────────────────────────────────────────

    /**
     * Maneja errores de validación de DTOs anotados con {@code @Valid}.
     * Convierte cada {@link FieldError} en un {@link ApiError} con el nombre del campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ApiError.validation(
                        fe.getField(),
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valor inválido"
                ))
                .toList();

        log.debug("Error de validación: {} campos fallidos", errors.size());
        return ApiResponse.errors(HttpStatus.BAD_REQUEST, errors);
    }

    // ─── JSON malformado ──────────────────────────────────────────────────────

    /**
     * Maneja solicitudes con cuerpo JSON que no se puede deserializar.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.debug("Cuerpo de solicitud ilegible: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.BAD_REQUEST,
                ApiError.of("INVALID_REQUEST_BODY", "El cuerpo de la solicitud tiene formato inválido"));
    }

    // ─── Tipo incorrecto en path/query param ──────────────────────────────────

    /**
     * Maneja parámetros de path o query con tipo incompatible (p. ej. UUID malformado).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "El parámetro '%s' recibió un valor con formato incorrecto: '%s'",
                ex.getName(), ex.getValue()
        );
        log.debug("Tipo incorrecto en parámetro: {}", message);
        return ApiResponse.error(HttpStatus.BAD_REQUEST,
                ApiError.of("INVALID_PARAMETER", message));
    }

    // ─── Entidad no encontrada ────────────────────────────────────────────────

    /**
     * Maneja {@link EntityNotFoundException} devolviendo 404.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex) {
        log.debug("Entidad no encontrada: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.NOT_FOUND,
                ApiError.of(ex.getErrorCode(), ex.getMessage()));
    }

    // ─── Reglas de negocio ────────────────────────────────────────────────────

    /**
     * Maneja cualquier {@link BusinessException} usando el status HTTP que ella misma define.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.warn("Error de negocio [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ApiResponse.error(ex.getStatus(),
                ApiError.of(ex.getErrorCode(), ex.getMessage()));
    }

    // ─── Seguridad ────────────────────────────────────────────────────────────

    /**
     * Maneja fallos de autenticación (token ausente, expirado o inválido) → 401.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.debug("Fallo de autenticación: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED,
                ApiError.of("AUTHENTICATION_FAILED", "Autenticación requerida o credenciales inválidas"));
    }

    /**
     * Maneja accesos denegados por falta de roles/permisos → 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.debug("Acceso denegado: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.FORBIDDEN,
                ApiError.of("ACCESS_DENIED", "No tienes permisos para realizar esta acción"));
    }

    // ─── Fallback genérico ────────────────────────────────────────────────────

    /**
     * Captura cualquier excepción no manejada y devuelve 500.
     * El detalle del error se registra en el log pero NO se expone al cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Error interno no esperado", ex);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ApiError.of("INTERNAL_ERROR", "Ocurrió un error interno. Por favor intenta de nuevo más tarde"));
    }
}
