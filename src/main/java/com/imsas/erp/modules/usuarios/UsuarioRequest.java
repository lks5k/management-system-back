package com.imsas.erp.modules.usuarios;

import com.imsas.erp.shared.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear y actualizar usuarios.
 *
 * <h3>Uso por operación</h3>
 * <ul>
 *   <li><b>Crear</b> ({@code POST /api/v1/usuarios}): {@code passwordInicial} es obligatorio;
 *       el servicio lanzará {@code BusinessException} si se omite.</li>
 *   <li><b>Actualizar</b> ({@code PUT /api/v1/usuarios/{id}}): {@code passwordInicial} se ignora.
 *       Para cambiar contraseña usar {@code PATCH /api/v1/usuarios/{id}/password}.</li>
 * </ul>
 *
 * @param nombre          nombre completo del usuario
 * @param email           correo electrónico, actúa como username en Spring Security
 * @param passwordInicial contraseña inicial en texto plano (solo creación, mín. 8 caracteres)
 * @param rol             rol asignado; solo SUPERADMIN puede asignar {@code SUPERADMIN}
 */
public record UsuarioRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120, message = "El email no puede superar 120 caracteres")
        String email,

        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String passwordInicial,

        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {}
