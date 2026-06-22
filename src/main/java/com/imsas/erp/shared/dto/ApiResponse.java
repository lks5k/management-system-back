package com.imsas.erp.shared.dto;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

@Value
@Builder
public class ApiResponse<T> {

    
    T data;

    
    ApiMeta meta;

    
    @Builder.Default
    List<ApiError> errors = Collections.emptyList();

    // ─── Fábrica: éxito 200 ───────────────────────────────────────────────────

    
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String version) {
        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .data(data)
                        .meta(ApiMeta.of(version))
                        .build()
        );
    }

    
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok(data, "v1");
    }

    // ─── Fábrica: éxito 201 ───────────────────────────────────────────────────

    
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<T>builder()
                        .data(data)
                        .meta(ApiMeta.of("v1"))
                        .build()
        );
    }

    // ─── Fábrica: éxito 204 ───────────────────────────────────────────────────

    
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    // ─── Fábrica: error ───────────────────────────────────────────────────────

    
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, ApiError error) {
        return ResponseEntity.status(status).body(
                ApiResponse.<T>builder()
                        .meta(ApiMeta.of("v1"))
                        .errors(List.of(error))
                        .build()
        );
    }

    
    public static <T> ResponseEntity<ApiResponse<T>> errors(HttpStatus status, List<ApiError> errors) {
        return ResponseEntity.status(status).body(
                ApiResponse.<T>builder()
                        .meta(ApiMeta.of("v1"))
                        .errors(errors)
                        .build()
        );
    }
}
