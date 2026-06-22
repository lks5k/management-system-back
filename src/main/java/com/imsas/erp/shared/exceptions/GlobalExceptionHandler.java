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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Validación JSR-380 ───────────────────────────────────────────────────

    
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

    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.debug("Cuerpo de solicitud ilegible: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.BAD_REQUEST,
                ApiError.of("INVALID_REQUEST_BODY", "El cuerpo de la solicitud tiene formato inválido"));
    }

    // ─── Tipo incorrecto en path/query param ──────────────────────────────────

    
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

    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex) {
        log.debug("Entidad no encontrada: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.NOT_FOUND,
                ApiError.of(ex.getErrorCode(), ex.getMessage()));
    }

    // ─── Reglas de negocio ────────────────────────────────────────────────────

    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.warn("Error de negocio [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ApiResponse.error(ex.getStatus(),
                ApiError.of(ex.getErrorCode(), ex.getMessage()));
    }

    // ─── Seguridad ────────────────────────────────────────────────────────────

    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.debug("Fallo de autenticación: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED,
                ApiError.of("AUTHENTICATION_FAILED", "Autenticación requerida o credenciales inválidas"));
    }

    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.debug("Acceso denegado: {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.FORBIDDEN,
                ApiError.of("ACCESS_DENIED", "No tienes permisos para realizar esta acción"));
    }

    // ─── Fallback genérico ────────────────────────────────────────────────────

    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Error interno no esperado", ex);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                ApiError.of("INTERNAL_ERROR", "Ocurrió un error interno. Por favor intenta de nuevo más tarde"));
    }
}
