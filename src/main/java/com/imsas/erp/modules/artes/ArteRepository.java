package com.imsas.erp.modules.artes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Arte}.
 */
@Repository
public interface ArteRepository extends JpaRepository<Arte, UUID> {

    /**
     * Busca el Arte vigente (activo) para una combinación empresa/marca/producto.
     *
     * <p>RN-19: la capa de servicio debe llamar este método antes de crear un Arte nuevo
     * para determinar si ya existe uno vigente para la combinación dada.
     *
     * @param empresaId  ID de la empresa
     * @param marcaId    ID de la marca
     * @param productoId ID del producto
     * @return Arte vigente si existe
     */
    Optional<Arte> findByEmpresaIdAndMarcaIdAndProductoIdAndActivoTrue(
            UUID empresaId, UUID marcaId, UUID productoId);

    /**
     * Lista todos los Artes activos de una empresa con paginación.
     *
     * @param empresaId ID de la empresa
     * @param pageable  configuración de página
     * @return página de Artes vigentes de la empresa
     */
    Page<Arte> findAllByEmpresaIdAndActivoTrue(UUID empresaId, Pageable pageable);

    /**
     * Lista todos los Artes (activos e históricos) de una empresa con paginación.
     * Útil para consultar el historial completo de versiones.
     *
     * @param empresaId ID de la empresa
     * @param pageable  configuración de página
     * @return página de todos los Artes de la empresa
     */
    Page<Arte> findAllByEmpresaId(UUID empresaId, Pageable pageable);

    /**
     * Verifica si ya existe un Arte con el código dado (duplicados de secuencia).
     *
     * @param codigo código a verificar
     * @return {@code true} si el código ya está registrado
     */
    boolean existsByCodigo(String codigo);

    /**
     * Búsqueda paginada con filtros opcionales por empresa, marca, producto y estado de vigencia.
     *
     * <p>Todos los parámetros son nullable: un valor {@code null} equivale a "no filtrar por este campo".
     * Para {@code activoFiltro}: {@code Boolean.TRUE} retorna solo vigentes; {@code null} retorna
     * vigentes e históricos.
     *
     * <p>{@code @EntityGraph} inicializa las asociaciones LAZY {@code empresa}, {@code marca} y
     * {@code producto} en un único JOIN, evitando el problema N+1 en listados paginados.
     * A diferencia de {@code JOIN FETCH} en JPQL, {@code @EntityGraph} no interfiere con la
     * query COUNT que Spring Data genera automáticamente para la paginación.
     *
     * @param empresaId    ID de la empresa; {@code null} = sin filtro
     * @param marcaId      ID de la marca; {@code null} = sin filtro
     * @param productoId   ID del producto; {@code null} = sin filtro
     * @param activoFiltro {@code true} = solo vigentes; {@code null} = vigentes + históricos
     * @param pageable     configuración de página y ordenamiento
     * @return página de Artes que cumplen los filtros indicados
     */
    @EntityGraph(attributePaths = {"empresa", "marca", "producto"})
    @Query("""
            SELECT a FROM Arte a
            WHERE (:empresaId    IS NULL OR a.empresa.id  = :empresaId)
              AND (:marcaId      IS NULL OR a.marca.id    = :marcaId)
              AND (:productoId   IS NULL OR a.producto.id = :productoId)
              AND (:activoFiltro IS NULL OR a.activo      = :activoFiltro)
            """)
    Page<Arte> buscarConFiltros(
            @Param("empresaId")    UUID    empresaId,
            @Param("marcaId")      UUID    marcaId,
            @Param("productoId")   UUID    productoId,
            @Param("activoFiltro") Boolean activoFiltro,
            Pageable pageable
    );
}
