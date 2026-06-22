package com.imsas.erp.modules.contactos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para un correo electrónico asociado a un contacto.
 *
 * <p>Se envía como elemento de la lista {@code emails} dentro de
 * {@link ContactoRequest}. La unicidad de {@code esPrincipal = true}
 * por contacto se valida en el servicio: si la lista contiene más de
 * un elemento con {@code esPrincipal = true} se rechaza con 400.
 *
 * @param email      dirección de correo electrónico (RN-03)
 * @param esPrincipal {@code true} si es el correo principal del contacto
 */
public record ContactoEmailRequest(

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        String email,

        boolean esPrincipal
) {}
