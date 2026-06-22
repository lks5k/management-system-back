package com.imsas.erp.modules.productos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Producto}.
 *
 * <p>Los métodos de existencia usan comparación <em>exacta</em> porque el servicio
 * siempre normaliza el nombre con {@code trim().toUpperCase()} antes de invocarlos
 * (RN-25). No se usa {@code IgnoreCase} para no enmascarar fallos de normalización.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    /**
     * Lista todos los productos activos con paginación.
     * Es el catálogo que consultan los asesores al crear una solicitud.
     *
     * @param pageable configuración de página
     * @return página de productos activos
     */
    Page<Producto> findAllByActivoTrue(Pageable pageable);

    /**
     * Verifica si ya existe un producto con ese nombre exacto en el sistema.
     * El llamador debe pasar el nombre ya normalizado (trim + uppercase).
     *
     * @param nombre nombre del producto ya normalizado
     * @return {@code true} si el nombre ya está registrado
     */
    boolean existsByNombre(String nombre);

    /**
     * Verifica duplicado excluyendo el propio producto al actualizar.
     * El llamador debe pasar el nombre ya normalizado (trim + uppercase).
     *
     * @param nombre nombre del producto ya normalizado
     * @param id     ID del producto a excluir
     * @return {@code true} si hay otro producto con ese nombre
     */
    boolean existsByNombreAndIdNot(String nombre, UUID id);
}
