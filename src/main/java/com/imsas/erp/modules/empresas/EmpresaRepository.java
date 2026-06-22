package com.imsas.erp.modules.empresas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Empresa}.
 */
@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    /**
     * Lista todas las empresas activas con paginación.
     *
     * @param pageable configuración de página y ordenamiento
     * @return página de empresas activas
     */
    Page<Empresa> findAllByActivoTrue(Pageable pageable);

    /**
     * Busca una empresa activa por número de documento.
     *
     * @param numeroDocumento número de documento (NIT, CC, etc.)
     * @return {@link Optional} con la empresa si existe y está activa
     */
    Optional<Empresa> findByNumeroDocumentoAndActivoTrue(String numeroDocumento);

    /**
     * Verifica si ya existe una empresa con el número de documento dado (RN-02).
     * Aplica independientemente del estado {@code activo}.
     *
     * @param numeroDocumento número de documento a verificar
     * @return {@code true} si el número de documento ya está registrado
     */
    boolean existsByNumeroDocumento(String numeroDocumento);

    /**
     * Verifica si ya existe otra empresa con el mismo número de documento,
     * excluyendo la empresa con el ID dado. Usado al actualizar para no
     * disparar falso positivo contra sí misma.
     *
     * @param numeroDocumento número de documento a verificar
     * @param id              ID de la empresa a excluir
     * @return {@code true} si existe otra empresa con ese número de documento
     */
    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, UUID id);

    /**
     * Búsqueda de empresas activas por razón social o número de documento
     * (insensible a mayúsculas). Útil para el buscador del formulario de solicitudes.
     *
     * @param termino  texto a buscar
     * @param pageable configuración de página
     * @return página de empresas que coinciden
     */
    @Query("""
            SELECT e FROM Empresa e
            WHERE e.activo = true
              AND (LOWER(e.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%'))
                OR e.numeroDocumento LIKE CONCAT('%', :termino, '%'))
            """)
    Page<Empresa> buscarActivas(@Param("termino") String termino, Pageable pageable);

    /**
     * Búsqueda paginada de empresas activas con filtros opcionales combinados.
     *
     * <p>Todos los parámetros son opcionales: si son {@code null} se ignoran
     * en el predicado correspondiente. El parámetro {@code creadoPorId} permite
     * restringir la cartera a las empresas del propio SALES_REP.
     *
     * @param termino      texto libre sobre razón social o número de documento;
     *                     si es {@code null} o vacío se omite el filtro de texto
     * @param tipoEmpresa  tipo de constitución legal; {@code null} para no filtrar
     * @param ciudad       nombre de ciudad (parcial, insensible a mayúsculas);
     *                     {@code null} para no filtrar
     * @param creadoPorId  UUID del usuario creador para restringir cartera (SALES_REP);
     *                     {@code null} para ver todas
     * @param pageable     configuración de página y ordenamiento
     * @return página de empresas que cumplen todos los criterios
     */
    @Query("""
            SELECT e FROM Empresa e
            WHERE e.activo = true
              AND (:termino IS NULL
                OR LOWER(e.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%'))
                OR e.numeroDocumento LIKE CONCAT('%', :termino, '%'))
              AND (:tipoEmpresa IS NULL OR e.tipoEmpresa = :tipoEmpresa)
              AND (:ciudad IS NULL OR LOWER(e.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%')))
              AND (:creadoPorId IS NULL OR e.creadoPor.id = :creadoPorId)
            """)
    Page<Empresa> buscarConFiltros(
            @Param("termino") String termino,
            @Param("tipoEmpresa") TipoEmpresa tipoEmpresa,
            @Param("ciudad") String ciudad,
            @Param("creadoPorId") UUID creadoPorId,
            Pageable pageable
    );
}
