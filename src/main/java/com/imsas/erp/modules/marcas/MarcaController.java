package com.imsas.erp.modules.marcas;

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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller REST para gestión de marcas comerciales.
 *
 * <h3>Endpoints</h3>
 * <pre>
 * GET    /api/v1/empresas/{empresaId}/marcas  → Listar paginado     (ADMIN, SUPERADMIN, MANAGER, SALES_REP)
 * POST   /api/v1/empresas/{empresaId}/marcas  → Crear marca         (ADMIN, SUPERADMIN, SALES_REP)
 * GET    /api/v1/marcas/{id}                  → Buscar por ID       (ADMIN, SUPERADMIN, MANAGER, SALES_REP)
 * PUT    /api/v1/marcas/{id}                  → Actualizar marca    (ADMIN, SUPERADMIN, SALES_REP)
 * DELETE /api/v1/marcas/{id}                  → Soft delete         (ADMIN, SUPERADMIN)
 * </pre>
 *
 * <p>Diseño de rutas mixto idéntico al de contactos: la colección usa ruta anidada bajo
 * {@code /empresas/{empresaId}/marcas} para expresar la jerarquía; las operaciones sobre
 * un ítem individual usan ruta plana {@code /marcas/{id}}.
 *
 * <p><b>OPERATOR no tiene acceso a ningún endpoint de este módulo (RN-24).</b>
 *
 * <p>La autorización fina (cartera SALES_REP, acceso a empresa ajena, etc.)
 * se delega al {@link MarcaService}.
 */
@RestController
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    // ─── GET /api/v1/empresas/{empresaId}/marcas ──────────────────────────────

    /**
     * Lista las marcas activas de una empresa con paginación.
     *
     * <p>SALES_REP solo ve marcas de empresas de su cartera; el servicio lanzará
     * 403 si la empresa no le pertenece.
     *
     * @param empresaId   UUID de la empresa
     * @param pageable    parámetros de paginación ({@code page}, {@code size}, {@code sort})
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return página de marcas en el envelope estándar
     */
    @GetMapping("/api/v1/empresas/{empresaId}/marcas")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<PageResponse<MarcaResponse>>> listar(
            @PathVariable UUID empresaId,
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC)
            Pageable pageable,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        Page<MarcaResponse> pagina = marcaService.listar(empresaId, pageable, autenticado);
        return PageResponse.ok(pagina);
    }

    // ─── POST /api/v1/empresas/{empresaId}/marcas ─────────────────────────────

    /**
     * Registra una nueva marca en una empresa.
     *
     * <p>El nombre se normaliza automáticamente en el servicio (trim + uppercase).
     * Si ya existe una marca con el mismo nombre normalizado en la empresa, devuelve 409.
     * SALES_REP solo puede crear marcas en empresas de su cartera.
     *
     * @param empresaId   UUID de la empresa donde se crea la marca
     * @param request     datos de la marca validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 201 Created con los datos de la marca creada
     */
    @PostMapping("/api/v1/empresas/{empresaId}/marcas")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<MarcaResponse>> crear(
            @PathVariable UUID empresaId,
            @Valid @RequestBody MarcaRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(marcaService.crear(empresaId, request, autenticado));
    }

    // ─── GET /api/v1/marcas/{id} ──────────────────────────────────────────────

    /**
     * Obtiene los datos de una marca activa por ID.
     *
     * <p>SALES_REP solo puede consultar marcas de empresas de su cartera.
     *
     * @param id          UUID de la marca
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return datos de la marca en el envelope estándar
     */
    @GetMapping("/api/v1/marcas/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<MarcaResponse>> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(marcaService.buscarPorId(id, autenticado));
    }

    // ─── PUT /api/v1/marcas/{id} ──────────────────────────────────────────────

    /**
     * Actualiza el nombre de una marca activa.
     *
     * <p>El nombre se normaliza automáticamente. Si el nuevo nombre (normalizado)
     * ya existe en la empresa, devuelve 409. La empresa de la marca no puede
     * cambiarse. SALES_REP solo puede modificar marcas de empresas de su cartera.
     *
     * @param id          UUID de la marca a actualizar
     * @param request     datos actualizados validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 200 OK con los datos actualizados
     */
    @PutMapping("/api/v1/marcas/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<MarcaResponse>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody MarcaRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(marcaService.actualizar(id, request, autenticado));
    }

    // ─── DELETE /api/v1/marcas/{id} ───────────────────────────────────────────

    /**
     * Desactiva una marca (soft delete). Nunca borra el registro físicamente.
     *
     * <p>Solo ADMIN y SUPERADMIN pueden desactivar marcas.
     * SALES_REP y MANAGER no tienen acceso a este endpoint.
     *
     * @param id          UUID de la marca a desactivar
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 204 No Content
     */
    @DeleteMapping("/api/v1/marcas/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<Void> desactivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        marcaService.desactivar(id, autenticado);
        return ApiResponse.noContent();
    }
}
