package com.imsas.erp.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ApiMeta {

    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp;

    
    String version;

    
    public static ApiMeta of(String version) {
        return ApiMeta.builder()
                .timestamp(Instant.now())
                .version(version)
                .build();
    }
}
