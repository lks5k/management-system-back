package com.imsas.erp.modules.usuarios;

import com.imsas.erp.shared.enums.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Usuario}.
 *
 * <p>Extiende {@link JpaRepository} para operaciones CRUD estándar.
 * Las queries de filtro por {@code activo} garantizan que el soft delete
 * (RN-12) sea transparente para la capa de servicio.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Busca un usuario activo por email. Usado por {@code UserDetailsService}
     * durante la autenticación JWT.
     *
     * @param email dirección de correo electrónico
     * @return {@link Optional} con el usuario si existe y está activo
     */
    Optional<Usuario> findByEmailAndActivoTrue(String email);

    /**
     * Verifica si ya existe un usuario con el email dado (activo o no).
     * Usado para validar unicidad antes de crear un usuario.
     *
     * @param email dirección de correo electrónico
     * @return {@code true} si el email ya está registrado
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe otro usuario con el mismo email, excluyendo un ID concreto.
     * Usado para validar unicidad al actualizar sin colisionar consigo mismo.
     *
     * @param email dirección de correo electrónico
     * @param id    ID del usuario a excluir de la búsqueda
     * @return {@code true} si hay otro usuario activo o inactivo con ese email
     */
    boolean existsByEmailAndIdNot(String email, UUID id);

    /**
     * Lista todos los usuarios activos con paginación.
     *
     * @param pageable configuración de página y ordenamiento
     * @return página de usuarios activos
     */
    Page<Usuario> findAllByActivoTrue(Pageable pageable);

    /**
     * Lista los usuarios activos excluyendo un rol específico con paginación.
     *
     * <p>Usado por ADMIN para listar usuarios sin incluir los de rol SUPERADMIN,
     * con quienes no tiene permisos de gestión.
     *
     * @param rol      rol a excluir de los resultados
     * @param pageable configuración de página y ordenamiento
     * @return página de usuarios activos con rol distinto al indicado
     */
    Page<Usuario> findAllByActivoTrueAndRolNot(Rol rol, Pageable pageable);
}
