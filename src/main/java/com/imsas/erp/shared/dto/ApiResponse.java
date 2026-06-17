package com.imsas.erp.shared.dto;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

/**
 * Envelope genérico para todas las respuestas REST de la API.
 *
 * <p>Estructura JSON resultante:
 * <pre>{@code
 * {
 *   "data":   { ... },
 *   "meta":   { "timestamp": "2026-06-16T...", "version": "v1" },
 *   "errors": []
 * }
 * }</pre>
 *
 * <p>Uso en controllers:
 * <pre>{@code
 * return ApiResponse.ok(usuarioDto);
 * return ApiResponse.created(nuevaEntidad);
 * return ApiResponse.error(HttpStatus.NOT_FOUND, ApiError.of("NOT_FOUND", "Usuario no encontrado"));
 * }</pre>
 *
 * @param <T> tipo del objeto contenido en {@code data}
 */
@Value
@Builder
public class ApiResponse<T> {

    /** Payload principal de la respuesta. {@code null} en respuestas de error. */
    T data;

    /** Metadatos de la respuesta: timestamp y versión de la API. */
    ApiMeta meta;

    /** Lista de errores. Vacía cuando la operación fue exitosa. */
    @Builder.Default
    List<ApiError> errors = Collections.emptyList();

    // ─── Fábrica: éxito 200 ───────────────────────────────────────────────────

    /**
     * Respuesta 200 OK con datos.
     *
     * @param data    payload a devolver
     * @param version versión de la API
     * @param <T>     tipo del payload
     * @return {@link ResponseEntity} con status 200
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String version) {
        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .data(data)
                        .meta(ApiMeta.of(version))
                        .build()
        );
    }

    /**
     * Respuesta 200 OK con datos, usando la versión por defecto {@code "v1"}.
     *
     * @param data payload a devolver
     * @param <T>  tipo del payload
     * @return {@link ResponseEntity} con status 200
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok(data, "v1");
    }

    // ─── Fábrica: éxito 201 ───────────────────────────────────────────────────

    /**
     * Respuesta 201 Created con la entidad recién creada.
     *
     * @param data payload de la entidad creada
     * @param <T>  tipo del payload
     * @return {@link ResponseEntity} con status 201
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<T>builder()
                        .data(data)
                        .meta(ApiMeta.of("v1"))
                        .build()
        );
    }

    // ─── Fábrica: éxito 204 ───────────────────────────────────────────────────

    /**
     * Respuesta 204 No Content (p. ej. para operaciones de eliminación lógica).
     *
     * @return {@link ResponseEntity} sin cuerpo con status 204
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    // ─── Fábrica: error ───────────────────────────────────────────────────────

    /**
     * Respuesta de error con un único {@link ApiError}.
     *
     * @param status código HTTP de error
     * @param error  descripción del error
     * @param <T>    tipo del payload (será {@code null})
     * @return {@link ResponseEntity} con el status y el error indicados
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, ApiError error) {
        return ResponseEntity.status(status).body(
                ApiResponse.<T>builder()
                        .meta(ApiMeta.of("v1"))
                        .errors(List.of(error))
                        .build()
        );
    }

    /**
     * Respuesta de error con múltiples {@link ApiError} (p. ej. errores de validación).
     *
     * @param status código HTTP de error
     * @param errors lista de errores
     * @param <T>    tipo del payload (será {@code null})
     * @return {@link ResponseEntity} con el status y los errores indicados
     */
    public static <T> ResponseEntity<ApiResponse<T>> errors(HttpStatus status, List<ApiError> errors) {
        return ResponseEntity.status(status).body(
                ApiResponse.<T>builder()
                        .meta(ApiMeta.of("v1"))
                        .errors(errors)
                        .build()
        );
    }
}
