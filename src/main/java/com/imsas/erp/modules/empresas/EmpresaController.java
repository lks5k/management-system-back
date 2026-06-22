package com.imsas.erp.modules.empresas;

import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.shared.dto.ApiResponse;
import com.imsas.erp.shared.dto.PageResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller REST para gestión de empresas cliente.
 *
 * <h3>Endpoints</h3>
 * <pre>
 * GET    /api/v1/empresas          → Listar paginado con filtros   (ADMIN, SUPERADMIN, MANAGER, SALES_REP)
 * GET    /api/v1/empresas/{id}     → Buscar por ID                 (ADMIN, SUPERADMIN, MANAGER, SALES_REP)
 * POST   /api/v1/empresas          → Crear empresa                 (ADMIN, SUPERADMIN, SALES_REP)
 * PUT    /api/v1/empresas/{id}     → Actualizar empresa            (ADMIN, SUPERADMIN, SALES_REP)
 * DELETE /api/v1/empresas/{id}     → Soft delete                   (ADMIN, SUPERADMIN)
 * </pre>
 *
 * <p><b>OPERATOR no tiene acceso a ningún endpoint de este módulo (RN-24).</b>
 * El {@code @PreAuthorize} de cada método enumera explícitamente los roles permitidos,
 * excluyendo OPERATOR por omisión.
 *
 * <p>La autorización fina (cartera de SALES_REP, acceso a empresa ajena, etc.)
 * se delega al {@link EmpresaService}.
 */
@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    // ─── GET /api/v1/empresas ─────────────────────────────────────────────────

    /**
     * Lista empresas activas con paginación y filtros opcionales.
     *
     * <p>Query params aceptados:
     * <ul>
     *   <li>{@code termino} — texto libre sobre razón social o número de documento</li>
     *   <li>{@code tipoEmpresa} — enum {@link TipoEmpresa} (SAS, SA, LTDA, PERSONA_NATURAL, OTRO)</li>
     *   <li>{@code ciudad} — nombre de ciudad (parcial, insensible a mayúsculas)</li>
     *   <li>{@code page}, {@code size}, {@code sort} — paginación estándar de Spring</li>
     * </ul>
     *
     * <p>SALES_REP solo ve su propia cartera; MANAGER, ADMIN y SUPERADMIN ven todo.
     *
     * @param termino     texto libre de búsqueda (opcional)
     * @param tipoEmpresa filtro por tipo de constitución (opcional)
     * @param ciudad      filtro por ciudad (opcional)
     * @param pageable    parámetros de paginación
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return página de empresas en el envelope estándar
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<PageResponse<EmpresaResponse>>> listar(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) TipoEmpresa tipoEmpresa,
            @RequestParam(required = false) String ciudad,
            @PageableDefault(size = 20, sort = "razonSocial", direction = Sort.Direction.ASC)
            Pageable pageable,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        Page<EmpresaResponse> pagina = empresaService.listar(
                termino, tipoEmpresa, ciudad, pageable, autenticado);
        return PageResponse.ok(pagina);
    }

    // ─── GET /api/v1/empresas/{id} ────────────────────────────────────────────

    /**
     * Obtiene los datos de una empresa activa por ID.
     *
     * <p>SALES_REP solo puede consultar empresas de su cartera;
     * el servicio lanzará 403 si intenta acceder a una empresa de otro representante.
     *
     * @param id          UUID de la empresa
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return datos de la empresa en el envelope estándar
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<EmpresaResponse>> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(empresaService.buscarPorId(id, autenticado));
    }

    // ─── POST /api/v1/empresas ────────────────────────────────────────────────

    /**
     * Registra una nueva empresa cliente.
     *
     * <p>El usuario autenticado queda almacenado como {@code creadoPor}.
     * El número de documento debe ser único en el sistema, incluidos registros inactivos (RN-02).
     *
     * @param request     datos de la empresa validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 201 Created con los datos de la empresa creada
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<EmpresaResponse>> crear(
            @Valid @RequestBody EmpresaRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(empresaService.crear(request, autenticado));
    }

    // ─── PUT /api/v1/empresas/{id} ────────────────────────────────────────────

    /**
     * Actualiza los datos de una empresa activa.
     *
     * <p>SALES_REP solo puede actualizar empresas de su propia cartera;
     * el servicio lanzará 403 si intenta modificar una empresa de otro representante.
     *
     * @param id          UUID de la empresa a actualizar
     * @param request     datos actualizados validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 200 OK con los datos actualizados
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<EmpresaResponse>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EmpresaRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(empresaService.actualizar(id, request, autenticado));
    }

    // ─── DELETE /api/v1/empresas/{id} ─────────────────────────────────────────

    /**
     * Desactiva una empresa (soft delete). Nunca borra el registro físicamente.
     *
     * <p>Solo ADMIN y SUPERADMIN pueden desactivar empresas.
     * SALES_REP y MANAGER no tienen acceso a este endpoint.
     *
     * @param id          UUID de la empresa a desactivar
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<Void> desactivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        empresaService.desactivar(id, autenticado);
        return ApiResponse.noContent();
    }
}
