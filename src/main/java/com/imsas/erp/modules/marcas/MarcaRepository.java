package com.imsas.erp.modules.marcas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Marca}.
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
     * Verifica si ya existe una marca con ese nombre exacto en la empresa (RN-11).
     * Insensible a mayúsculas para cubrir "HONDA" vs "Honda".
     *
     * @param nombre    nombre de la marca
     * @param empresaId ID de la empresa
     * @return {@code true} si el nombre ya existe en la empresa
     */
    boolean existsByNombreIgnoreCaseAndEmpresaIdAndActivoTrue(String nombre, UUID empresaId);

    /**
     * Verifica duplicado excluyendo la propia marca al actualizar.
     *
     * @param nombre    nombre de la marca
     * @param empresaId ID de la empresa
     * @param id        ID de la marca a excluir
     * @return {@code true} si hay otra marca activa con ese nombre en la empresa
     */
    boolean existsByNombreIgnoreCaseAndEmpresaIdAndActivoTrueAndIdNot(
            String nombre, UUID empresaId, UUID id);

    /**
     * Busca marcas activas de una empresa cuyo nombre contenga el texto dado (RN-11).
     * Sirve para la detección de duplicados aproximados antes de crear una marca nueva.
     *
     * @param empresaId ID de la empresa
     * @param termino   fragmento de nombre a buscar
     * @return lista de marcas similares
     */
    @Query("""
            SELECT m FROM Marca m
            WHERE m.empresa.id = :empresaId
              AND m.activo = true
              AND LOWER(m.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))
            """)
    List<Marca> buscarSimilares(
            @Param("empresaId") UUID empresaId,
            @Param("termino") String termino);
}
