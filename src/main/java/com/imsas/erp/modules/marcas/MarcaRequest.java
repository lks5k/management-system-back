package com.imsas.erp.modules.marcas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarcaRequest(

        
        @NotBlank(message = "El nombre de la marca es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre
) {}
