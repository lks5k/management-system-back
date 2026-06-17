package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.shared.enums.EstadoSolicitud;
import com.imsas.erp.shared.enums.TipoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Solicitud}.
 *
 * <p>Extiende también {@link JpaSpecificationExecutor} para habilitar búsquedas
 * dinámicas multi-filtro (por estado, tipo, empresa, asesor, fecha) mediante
 * el patrón Specification, sin necesidad de crear un método derivado por cada
 * combinación posible de filtros.
 */
@Repository
public interface SolicitudRepository
        extends JpaRepository<Solicitud, UUID>, JpaSpecificationExecutor<Solicitud> {

    // ─── Búsqueda por código ──────────────────────────────────────────────────

    /**
     * Busca una solicitud activa por su código único (p. ej. {@code "P-S59644"}).
     *
     * @param codigo código de solicitud
     * @return {@link Optional} con la solicitud si existe y está activa
     */
    Optional<Solicitud> findByCodigoAndActivoTrue(String codigo);

    /**
     * Verifica si ya existe una solicitud con ese código (activa o no).
     * Evita duplicados antes de asignar un nuevo código (RN-06).
     *
     * @param codigo código a verificar
     * @return {@code true} si el código ya existe
     */
    boolean existsByCodigo(String codigo);

    // ─── Listados paginados ───────────────────────────────────────────────────

    /**
     * Lista todas las solicitudes activas con paginación.
     *
     * @param pageable configuración de página y ordenamiento
     * @return página de solicitudes activas
     */
    Page<Solicitud> findAllByActivoTrue(Pageable pageable);

    /**
     * Lista las solicitudes activas de una empresa con paginación.
     *
     * @param empresaId ID de la empresa
     * @param pageable  configuración de página
     * @return página de solicitudes de la empresa
     */
    Page<Solicitud> findAllByEmpresaIdAndActivoTrue(UUID empresaId, Pageable pageable);

    /**
     * Lista las solicitudes activas de un asesor con paginación.
     * Usado cuando el rol del usuario autenticado es SALES_REP u OFFICER
     * (solo ven sus propias solicitudes).
     *
     * @param asesorId ID del usuario asesor
     * @param pageable configuración de página
     * @return página de solicitudes del asesor
     */
    Page<Solicitud> findAllByAsesorIdAndActivoTrue(UUID asesorId, Pageable pageable);

    /**
     * Lista las solicitudes activas filtradas por estado con paginación.
     *
     * @param estado   estado de solicitud
     * @param pageable configuración de página
     * @return página de solicitudes en ese estado
     */
    Page<Solicitud> findAllByEstadoAndActivoTrue(EstadoSolicitud estado, Pageable pageable);

    /**
     * Lista las solicitudes activas filtradas por tipo con paginación.
     *
     * @param tipo     tipo de solicitud
     * @param pageable configuración de página
     * @return página de solicitudes de ese tipo
     */
    Page<Solicitud> findAllByTipoAndActivoTrue(TipoSolicitud tipo, Pageable pageable);

    // ─── Relaciones ───────────────────────────────────────────────────────────

    /**
     * Lista las solicitudes derivadas de una solicitud origen (PEDIDO o REPOSICION
     * originados desde un DISEÑO). Usado en la pantalla de detalle de solicitud.
     *
     * @param solicitudOrigenId ID de la solicitud origen
     * @return página de solicitudes derivadas
     */
    Page<Solicitud> findAllBySolicitudOrigenIdAndActivoTrue(
            UUID solicitudOrigenId, Pageable pageable);

    // ─── Consecutivo para generación de código (RN-04, RN-05) ────────────────

    /**
     * Obtiene el siguiente valor de la secuencia global de solicitudes.
     * Se invoca exactamente una vez al pasar una solicitud de BORRADOR a PENDIENTE.
     *
     * <p>La secuencia {@code seq_solicitud_consecutivo} es {@code NO CYCLE},
     * garantizando que nunca se reinicie (RN-05).
     *
     * @return siguiente valor del consecutivo global
     */
    @Query(value = "SELECT nextval('seq_solicitud_consecutivo')", nativeQuery = true)
    long nextConsecutivo();
}
