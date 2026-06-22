package com.imsas.erp.modules.contactos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ContactoRequest(

        @NotBlank(message = "El nombre del contacto es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @Size(max = 100, message = "El cargo no puede superar 100 caracteres")
        String cargo,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono,

        boolean esFacturacion,

        @NotEmpty(message = "El contacto debe tener al menos un correo electrónico")
        @Valid
        List<ContactoEmailRequest> emails
) {}
