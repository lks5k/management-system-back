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

}
