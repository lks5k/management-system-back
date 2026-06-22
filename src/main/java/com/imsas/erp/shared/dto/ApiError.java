package com.imsas.erp.shared.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiError {

    
    String field;

    
    String message;

    
    String code;

    
    public static ApiError validation(String field, String message) {
        return ApiError.builder()
                .field(field)
                .message(message)
                .code("VALIDATION_ERROR")
                .build();
    }

    
    public static ApiError of(String code, String message) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .build();
    }
}
