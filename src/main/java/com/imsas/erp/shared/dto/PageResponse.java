package com.imsas.erp.shared.dto;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Envelope de respuesta paginada. Envuelve el contenido de un {@link Page} de Spring Data
 * en la estructura estándar de la API, incluyendo metadatos de paginación.
 *
 * <p>Estructura JSON resultante:
 * <pre>{@code
 * {
 *   "data": {
 *     "content":        [ ... ],
 *     "page":           0,
 *     "size":           20,
 *     "total_elements": 150,
 *     "total_pages":    8,
 *     "first":          true,
 *     "last":           false
 *   },
 *   "meta":   { "timestamp": "...", "version": "v1" },
 *   "errors": []
 * }
 * }</pre>
 *
 * <p>Uso en controllers:
 * <pre>{@code
 * Page<UsuarioDto> page = usuarioService.listar(pageable);
 * return PageResponse.of(page);
 * }</pre>
 *
 * @param <T> tipo de los elementos paginados
 */
@Value
@Builder
public class PageResponse<T> {

    /** Elementos de la página actual. */
    List<T> content;

    /** Número de página actual (base 0). */
    int page;

    /** Tamaño máximo de la página. */
    int size;

    /** Total de elementos en todas las páginas. */
    long totalElements;

    /** Total de páginas disponibles. */
    int totalPages;

    /** {@code true} si esta es la primera página. */
    boolean first;

    /** {@code true} si esta es la última página. */
    boolean last;

    /**
     * Construye un {@code PageResponse} a partir de un {@link Page} de Spring Data.
     *
     * @param page página resultante de una consulta con {@code Pageable}
     * @param <T>  tipo de los elementos
     * @return instancia de {@code PageResponse} con los metadatos de paginación
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * Convierte un {@link Page} directamente en un {@link ResponseEntity} 200 OK
     * con el envelope completo ({@link ApiResponse} + {@link PageResponse}).
     *
     * @param page página resultante de una consulta con {@code Pageable}
     * @param <T>  tipo de los elementos
     * @return {@link ResponseEntity} lista para devolver desde un controller
     */
    public static <T> ResponseEntity<ApiResponse<PageResponse<T>>> ok(Page<T> page) {
        return ApiResponse.ok(PageResponse.from(page));
    }
}
