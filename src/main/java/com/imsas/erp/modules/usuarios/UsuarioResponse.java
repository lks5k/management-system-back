package com.imsas.erp.modules.usuarios;

import com.imsas.erp.shared.enums.Rol;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida para exponer datos de un usuario.
 *
 * <p>Nunca incluye el {@code passwordHash} ni ningún campo sensible.
 * El campo {@code activo} permite que el frontend diferencie usuarios
 * vigentes de los desactivados por soft delete.
 *
 * @param id       identificador único del usuario
 * @param nombre   nombre completo
 * @param email    correo electrónico (username)
 * @param rol      rol asignado
 * @param activo   {@code false} si el usuario fue desactivado (soft delete)
 * @param creadoEn timestamp UTC de creación del registro
 */
public record UsuarioResponse(
        UUID    id,
        String  nombre,
        String  email,
        Rol     rol,
        boolean activo,
        Instant creadoEn
) {

    /**
     * Construye un {@code UsuarioResponse} a partir de la entidad JPA.
     *
     * @param usuario entidad origen
     * @return DTO listo para serializar
     */
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
