package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.shared.enums.EstadoSolicitud;
import com.imsas.erp.shared.enums.TipoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudRepository
        extends JpaRepository<Solicitud, UUID>, JpaSpecificationExecutor<Solicitud> {

    // ─── Búsqueda por código ──────────────────────────────────────────────────

    Optional<Solicitud> findByCodigoAndActivoTrue(String codigo);

    boolean existsByCodigo(String codigo);

    // ─── Listados paginados simples ───────────────────────────────────────────

    Page<Solicitud> findAllByActivoTrue(Pageable pageable);

    Page<Solicitud> findAllByEmpresaIdAndActivoTrue(UUID empresaId, Pageable pageable);

    Page<Solicitud> findAllByAsesorIdAndActivoTrue(UUID asesorId, Pageable pageable);

    Page<Solicitud> findAllByEstadoAndActivoTrue(EstadoSolicitud estado, Pageable pageable);

    Page<Solicitud> findAllByTipoAndActivoTrue(TipoSolicitud tipo, Pageable pageable);

    // ─── Relaciones ───────────────────────────────────────────────────────────

    Page<Solicitud> findAllBySolicitudOrigenIdAndActivoTrue(
            UUID solicitudOrigenId, Pageable pageable);

    // ─── Filtro combinado (servicio principal de listado) ─────────────────────

    /**
     * Listado paginado con filtros opcionales. Los parámetros nulos se ignoran.
     * El {@code @EntityGraph} carga de forma ansiosa todas las relaciones {@code @ManyToOne}
     * para evitar el problema N+1 en {@link SolicitudResponse#from}.
     */
    @EntityGraph(attributePaths = {
            "asesor", "empresa", "contacto", "marca", "producto",
            "arte", "solicitudOrigen"
    })
    @Query(
            value = """
                    SELECT s FROM Solicitud s
                    WHERE s.activo = true
                      AND (:empresaId IS NULL OR s.empresa.id = :empresaId)
                      AND (:asesorId  IS NULL OR s.asesor.id  = :asesorId)
                      AND (:estado    IS NULL OR s.estado      = :estado)
                      AND (:tipo      IS NULL OR s.tipo        = :tipo)
                    """,
            countQuery = """
                    SELECT count(s) FROM Solicitud s
                    WHERE s.activo = true
                      AND (:empresaId IS NULL OR s.empresa.id = :empresaId)
                      AND (:asesorId  IS NULL OR s.asesor.id  = :asesorId)
                      AND (:estado    IS NULL OR s.estado      = :estado)
                      AND (:tipo      IS NULL OR s.tipo        = :tipo)
                    """
    )
    Page<Solicitud> buscarConFiltros(
            @Param("empresaId") UUID            empresaId,
            @Param("asesorId")  UUID            asesorId,
            @Param("estado")    EstadoSolicitud estado,
            @Param("tipo")      TipoSolicitud   tipo,
            Pageable pageable
    );

    // ─── Consecutivo para generación de código (RN-04, RN-05) ────────────────

    /**
     * Obtiene el siguiente valor de la secuencia global de solicitudes.
     * La secuencia nunca se reinicia (RN-05).
     */
    @Query(value = "SELECT nextval('seq_solicitud_consecutivo')", nativeQuery = true)
    long nextConsecutivo();
}
