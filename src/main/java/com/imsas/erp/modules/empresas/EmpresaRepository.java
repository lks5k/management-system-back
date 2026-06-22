package com.imsas.erp.modules.empresas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    
    Page<Empresa> findAllByActivoTrue(Pageable pageable);

    
    Optional<Empresa> findByNumeroDocumentoAndActivoTrue(String numeroDocumento);

    
    boolean existsByNumeroDocumento(String numeroDocumento);

    
    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, UUID id);

    
    @Query("""
            SELECT e FROM Empresa e
            WHERE e.activo = true
              AND (LOWER(e.razonSocial) LIKE LOWER(CONCAT('%', :termino, '%'))
                OR e.numeroDocumento LIKE CONCAT('%', :termino, '%'))
            """)
    Page<Empresa> buscarActivas(@Param("termino") String termino, Pageable pageable);

    
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
