package com.imsas.erp.modules.empresas;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 30, message = "El número de documento no puede superar 30 caracteres")
        String numeroDocumento,

        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 200, message = "La razón social no puede superar 200 caracteres")
        String razonSocial,

        @NotNull(message = "El tipo de empresa es obligatorio")
        TipoEmpresa tipoEmpresa,

        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion,

        @Size(max = 100, message = "La ciudad no puede superar 100 caracteres")
        String ciudad,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono,

        @Size(max = 200, message = "El sitio web no puede superar 200 caracteres")
        String sitioWeb,

        @Min(value = 1, message = "El día de cierre contable debe ser entre 1 y 31")
        @Max(value = 31, message = "El día de cierre contable debe ser entre 1 y 31")
        Short diaCierreContable,

        @Size(max = 60, message = "El país no puede superar 60 caracteres")
        String pais
) {}
