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

@Repository
public interface ArteRepository extends JpaRepository<Arte, UUID> {

    
    Optional<Arte> findByEmpresaIdAndMarcaIdAndProductoIdAndActivoTrue(
            UUID empresaId, UUID marcaId, UUID productoId);

    
    Page<Arte> findAllByEmpresaIdAndActivoTrue(UUID empresaId, Pageable pageable);

    
    Page<Arte> findAllByEmpresaId(UUID empresaId, Pageable pageable);

    
    boolean existsByCodigo(String codigo);

    /**
     * Obtiene el siguiente valor de la secuencia global de artes.
     * La secuencia nunca se reinicia (RN-18).
     */
    @org.springframework.data.jpa.repository.Query(
            value = "SELECT nextval('seq_arte_consecutivo')",
            nativeQuery = true)
    long nextConsecutivoArte();

    
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
