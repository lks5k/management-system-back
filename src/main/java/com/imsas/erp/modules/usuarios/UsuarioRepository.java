package com.imsas.erp.modules.usuarios;

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
     * Usado para validar unicidad antes de crear o actualizar un usuario.
     *
     * @param email dirección de correo electrónico
     * @return {@code true} si el email ya está registrado
     */
    boolean existsByEmail(String email);

    /**
     * Lista todos los usuarios activos con paginación.
     *
     * @param pageable configuración de página y ordenamiento
     * @return página de usuarios activos
     */
    Page<Usuario> findAllByActivoTrue(Pageable pageable);
}
