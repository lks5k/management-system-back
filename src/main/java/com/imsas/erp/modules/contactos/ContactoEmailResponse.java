package com.imsas.erp.modules.contactos;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida para un correo electrónico de contacto.
 *
 * <p>Se devuelve como elemento de la lista {@code emails} dentro de
 * {@link ContactoResponse}.
 *
 * @param id          UUID del registro de correo
 * @param email       dirección de correo electrónico
 * @param esPrincipal {@code true} si es el correo principal del contacto
 * @param createdAt   timestamp UTC de creación del registro
 */
public record ContactoEmailResponse(
        UUID id,
        String email,
        boolean esPrincipal,
        Instant createdAt
) {

    /**
     * Construye un {@code ContactoEmailResponse} a partir de la entidad {@link ContactoEmail}.
     *
     * @param e entidad origen; no debe ser {@code null}
     * @return DTO listo para serializar
     */
    public static ContactoEmailResponse from(ContactoEmail e) {
        return new ContactoEmailResponse(
                e.getId(),
                e.getEmail(),
                e.isEsPrincipal(),
                e.getCreatedAt()
        );
    }
}
