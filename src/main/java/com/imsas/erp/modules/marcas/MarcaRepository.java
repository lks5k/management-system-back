package com.imsas.erp.modules.marcas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Marca}.
 *
 * <p>Los métodos de existencia usan comparación <em>exacta</em> porque el servicio
 * siempre normaliza el nombre con {@code trim().toUpperCase()} antes de invocarlos
 * (RN-25). No se usa {@code IgnoreCase} para no enmascarar fallos de normalización.
 */
@Repository
public interface MarcaRepository extends JpaRepository<Marca, UUID> {

    /**
     * Lista las marcas activas de una empresa con paginación.
     *
     * @param empresaId ID de la empresa
     * @param pageable  configuración de página
     * @return página de marcas activas
     */
    Page<Marca> findAllByEmpresaIdAndActivoTrue(UUID empresaId, Pageable pageable);

    /**
     * Verifica si ya existe una marca activa con ese nombre exacto en la empresa (RN-11).
     * El llamador debe pasar el nombre ya normalizado (trim + uppercase).
     *
     * @param nombre    nombre de la marca ya normalizado
     * @param empresaId ID de la empresa
     * @return {@code true} si el nombre ya existe en la empresa
     */
    boolean existsByNombreAndEmpresaIdAndActivoTrue(String nombre, UUID empresaId);

    /**
     * Verifica duplicado excluyendo la propia marca al actualizar (RN-11).
     * El llamador debe pasar el nombre ya normalizado (trim + uppercase).
     *
     * @param nombre    nombre de la marca ya normalizado
     * @param empresaId ID de la empresa
     * @param id        ID de la marca a excluir
     * @return {@code true} si hay otra marca activa con ese nombre en la empresa
     */
    boolean existsByNombreAndEmpresaIdAndActivoTrueAndIdNot(String nombre, UUID empresaId, UUID id);
}
