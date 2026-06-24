package com.imsas.erp.modules.usuarios;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoUsuarioRequest(
        @NotNull(message = "El campo activo es obligatorio")
        Boolean activo
) {}
