package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.shared.dto.ApiResponse;
import com.imsas.erp.shared.dto.PageResponse;
import com.imsas.erp.shared.enums.EstadoSolicitud;
import com.imsas.erp.shared.enums.TipoSolicitud;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    // ─── GET /api/v1/solicitudes ──────────────────────────────────────────────

    /**
     * Listado paginado con filtros opcionales.
     * OPERATOR y todos los roles autenticados pueden consultar.
     * SALES_REP solo ve sus propias solicitudes (filtro aplicado en servicio).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','OPERATOR','SALES_REP')")
    public ResponseEntity<ApiResponse<PageResponse<SolicitudResponse>>> listar(
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) UUID asesorId,
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) TipoSolicitud tipo,
            @PageableDefault(size = 20, sort = "creadoEn", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        Page<SolicitudResponse> pagina = solicitudService.listar(
                empresaId, asesorId, estado, tipo, pageable, autenticado);
        return PageResponse.ok(pagina);
    }

    // ─── GET /api/v1/solicitudes/{id} ─────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','OPERATOR','SALES_REP')")
    public ResponseEntity<ApiResponse<SolicitudResponse>> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(solicitudService.buscarPorId(id, autenticado));
    }

    // ─── POST /api/v1/solicitudes ─────────────────────────────────────────────

    /**
     * Crea una solicitud en estado BORRADOR.
     * Si es tipo DISEÑO con marca+producto, el sistema busca o crea el Arte automáticamente (RN-19).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<SolicitudResponse>> crear(
            @Valid @RequestBody SolicitudRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(solicitudService.crear(request, autenticado));
    }

    // ─── PUT /api/v1/solicitudes/{id} ─────────────────────────────────────────

    /**
     * Actualiza una solicitud en estado BORRADOR.
     * SALES_REP solo puede editar sus propias solicitudes.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<SolicitudResponse>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody SolicitudRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(solicitudService.actualizar(id, request, autenticado));
    }

    // ─── DELETE /api/v1/solicitudes/{id} ──────────────────────────────────────

    /**
     * Soft delete. Solo ADMIN y SUPERADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<Void> desactivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        solicitudService.desactivar(id, autenticado);
        return ApiResponse.noContent();
    }

    // ─── PATCH /api/v1/solicitudes/{id}/estado ────────────────────────────────

    /**
     * Transición de estado según la máquina de estados (SDD §6.3).
     * OPERATOR no tiene acceso a este endpoint.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<SolicitudResponse>> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarEstadoRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(solicitudService.cambiarEstado(id, request, autenticado));
    }

    // ─── PATCH /api/v1/solicitudes/{id}/version-bd ────────────────────────────

    /**
     * Versiona el código Base de Datos del cliente (RN-08, RN-09).
     * Roles: SUPERADMIN, ADMIN, SALES_REP (propio).
     */
    @PatchMapping("/{id}/version-bd")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<SolicitudResponse>> versionarBd(
            @PathVariable UUID id,
            @Valid @RequestBody VersionarBdRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(solicitudService.versionarBd(id, request, autenticado));
    }

    // ─── POST /api/v1/solicitudes/{id}/convertir ──────────────────────────────

    /**
     * Convierte una solicitud DISEÑO (o PEDIDO) en un PEDIDO o REPOSICION (CU-06).
     * La nueva solicitud se crea en BORRADOR y referencia la solicitud origen.
     */
    @PostMapping("/{id}/convertir")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<SolicitudResponse>> convertir(
            @PathVariable UUID id,
            @Valid @RequestBody ConvertirRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(solicitudService.convertir(id, request, autenticado));
    }
}
