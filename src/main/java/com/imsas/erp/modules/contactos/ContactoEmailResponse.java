package com.imsas.erp.modules.contactos;

import java.time.Instant;
import java.util.UUID;

public record ContactoEmailResponse(
        UUID id,
        String email,
        boolean esPrincipal,
        Instant createdAt
) {

    
    public static ContactoEmailResponse from(ContactoEmail e) {
        return new ContactoEmailResponse(
                e.getId(),
                e.getEmail(),
                e.isEsPrincipal(),
                e.getCreatedAt()
        );
    }
}
