package com.imsas.erp.modules.artes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
