package com.imsas.erp.modules.usuarios;

import com.imsas.erp.shared.enums.Rol;

import java.time.Instant;
import java.util.UUID;

public record UsuarioResponse(
        UUID    id,
        String  nombre,
        String  email,
        Rol     rol,
        boolean activo,
        Instant creadoEn
) {

    
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getCreadoEn()
        );
    }
}
