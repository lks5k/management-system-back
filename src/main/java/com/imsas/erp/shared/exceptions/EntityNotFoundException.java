package com.imsas.erp.shared.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class EntityNotFoundException extends BusinessException {

    
    public EntityNotFoundException(String entityName, UUID id) {
        super(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND",
                entityName + " no encontrado con id: " + id);
    }

    
    public EntityNotFoundException(String entityName, String field, String value) {
        super(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND",
                entityName + " no encontrado con " + field + ": " + value);
    }
}
