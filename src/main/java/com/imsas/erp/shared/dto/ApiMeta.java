package com.imsas.erp.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Metadatos incluidos en todas las respuestas de la API.
 *
 * <p>Contiene la marca de tiempo de la respuesta (en UTC ISO-8601) y la versión
 * de la API que atendió la solicitud. Se serializa como campo {@code meta} dentro
 * del envelope {@link ApiResponse}.
 */
@Value
@Builder
public class ApiMeta {

    /**
     * Marca de tiempo en la que se generó la respuesta, expresada en UTC.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp;

    /**
     * Versión de la API que procesó la solicitud (p. ej. {@code "v1"}).
     */
    String version;

    /**
     * Crea una instancia de {@code ApiMeta} con la marca de tiempo actual y la
     * versión especificada.
     *
     * @param version versión de la API (p. ej. {@code "v1"})
     * @return nueva instancia de {@code ApiMeta}
     */
    public static ApiMeta of(String version) {
        return ApiMeta.builder()
                .timestamp(Instant.now())
                .version(version)
                .build();
    }
}
