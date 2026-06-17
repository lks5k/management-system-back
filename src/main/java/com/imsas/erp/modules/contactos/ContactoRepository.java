package com.imsas.erp.modules.contactos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Contacto}.
 */
@Repository
public interface ContactoRepository extends JpaRepository<Contacto, UUID> {

    /**
     * Lista los contactos activos de una empresa con paginación.
     *
     * @param empresaId ID de la empresa
     * @param pageable  configuración de página
     * @return página de contactos activos de la empresa
     */
    Page<Contacto> findAllByEmpresaIdAndActivoTrue(UUID empresaId, Pageable pageable);

    /**
     * Verifica si ya existe un contacto de facturación activo para la empresa.
     * Un solo contacto de facturación por empresa es la regla de negocio;
     * la validación de unicidad se hace en la capa de servicio con este método.
     *
     * @param empresaId ID de la empresa
     * @return {@code true} si ya hay un contacto de facturación activo
     */
    boolean existsByEmpresaIdAndEsFacturacionTrueAndActivoTrue(UUID empresaId);

    /**
     * Verifica si ya existe otro contacto de facturación activo, excluyendo
     * el contacto con el ID dado. Usado al actualizar.
     *
     * @param empresaId  ID de la empresa
     * @param contactoId ID del contacto a excluir
     * @return {@code true} si hay otro contacto de facturación activo
     */
    boolean existsByEmpresaIdAndEsFacturacionTrueAndActivoTrueAndIdNot(
            UUID empresaId, UUID contactoId);
}
