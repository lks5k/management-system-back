package com.imsas.erp.modules.productos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Producto}.
 *
 * <p>Solo {@code ADMIN} y {@code SUPERADMIN} pueden modificar esta lista (RN-16).
 * Esa restricción se aplica en la capa de seguridad del controller.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    /**
     * Lista todos los productos activos con paginación.
     * Es el endpoint que ven los asesores para seleccionar producto en una solicitud.
     *
     * @param pageable configuración de página
     * @return página de productos activos
     */
    Page<Producto> findAllByActivoTrue(Pageable pageable);

    /**
     * Verifica si ya existe un producto con ese nombre (insensible a mayúsculas).
     *
     * @param nombre nombre del producto
     * @return {@code true} si el nombre ya existe
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Verifica duplicado excluyendo el propio producto al actualizar.
     *
     * @param nombre nombre del producto
     * @param id     ID del producto a excluir
     * @return {@code true} si hay otro producto activo con ese nombre
     */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);
}
