package com.imsas.erp.modules.contactos;

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
 * Controller REST para gestión de contactos de empresas cliente.
 *
 * <h3>Endpoints</h3>
 * <pre>
 * GET  /api/v1/empresas/{empresaId}/contactos  → Listar paginado       (ADMIN, SUPERADMIN, MANAGER, SALES_REP)
 * POST /api/v1/empresas/{empresaId}/contactos  → Crear contacto        (ADMIN, SUPERADMIN, SALES_REP)
 * GET  /api/v1/contactos/{id}                  → Buscar por ID         (ADMIN, SUPERADMIN, MANAGER, SALES_REP)
 * PUT  /api/v1/contactos/{id}                  → Actualizar contacto   (ADMIN, SUPERADMIN, SALES_REP)
 * DELETE /api/v1/contactos/{id}                → Soft delete           (ADMIN, SUPERADMIN)
 * </pre>
 *
 * <p>Diseño de rutas mixto: las operaciones de colección usan ruta anidada bajo
 * {@code /empresas/{empresaId}/contactos} para expresar la jerarquía del recurso;
 * las operaciones sobre un ítem individual usan ruta plana {@code /contactos/{id}}
 * ya que el ID del contacto es suficiente para identificarlo unívocamente.
 *
 * <p><b>OPERATOR no tiene acceso a ningún endpoint de este módulo (RN-24).</b>
 *
 * <p>La autorización fina (cartera SALES_REP, acceso a empresa ajena, etc.)
 * se delega al {@link ContactoService}.
 */
@RestController
@RequiredArgsConstructor
public class ContactoController {

    private final ContactoService contactoService;

    // ─── GET /api/v1/empresas/{empresaId}/contactos ───────────────────────────

    /**
     * Lista los contactos activos de una empresa con paginación.
     *
     * <p>SALES_REP solo ve contactos de empresas de su cartera;
     * el servicio lanzará 403 si la empresa no le pertenece.
     *
     * @param empresaId   UUID de la empresa
     * @param pageable    parámetros de paginación ({@code page}, {@code size}, {@code sort})
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return página de contactos en el envelope estándar
     */
    @GetMapping("/api/v1/empresas/{empresaId}/contactos")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<PageResponse<ContactoResponse>>> listar(
            @PathVariable UUID empresaId,
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC)
            Pageable pageable,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        Page<ContactoResponse> pagina = contactoService.listar(empresaId, pageable, autenticado);
        return PageResponse.ok(pagina);
    }

    // ─── POST /api/v1/empresas/{empresaId}/contactos ──────────────────────────

    /**
     * Registra un nuevo contacto en una empresa.
     *
     * <p>La lista {@code emails} debe tener al menos un elemento y como máximo
     * uno con {@code esPrincipal = true}. SALES_REP solo puede crear contactos
     * en empresas de su cartera.
     *
     * @param empresaId   UUID de la empresa donde se registra el contacto
     * @param request     datos del contacto validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 201 Created con los datos del contacto creado
     */
    @PostMapping("/api/v1/empresas/{empresaId}/contactos")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<ContactoResponse>> crear(
            @PathVariable UUID empresaId,
            @Valid @RequestBody ContactoRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(contactoService.crear(empresaId, request, autenticado));
    }

    // ─── GET /api/v1/contactos/{id} ───────────────────────────────────────────

    /**
     * Obtiene los datos de un contacto activo por ID, incluyendo su lista de emails.
     *
     * <p>SALES_REP solo puede consultar contactos de empresas de su cartera.
     *
     * @param id          UUID del contacto
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return datos del contacto en el envelope estándar
     */
    @GetMapping("/api/v1/contactos/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<ContactoResponse>> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(contactoService.buscarPorId(id, autenticado));
    }

    // ─── PUT /api/v1/contactos/{id} ───────────────────────────────────────────

    /**
     * Actualiza los datos de un contacto activo y reemplaza su lista de emails.
     *
     * <p>El reemplazo de emails es completo: los anteriores se eliminan y se
     * persisten los del request. SALES_REP solo puede modificar contactos de
     * empresas de su cartera.
     *
     * @param id          UUID del contacto a actualizar
     * @param request     datos actualizados validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 200 OK con los datos actualizados
     */
    @PutMapping("/api/v1/contactos/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<ContactoResponse>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ContactoRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(contactoService.actualizar(id, request, autenticado));
    }

    // ─── DELETE /api/v1/contactos/{id} ────────────────────────────────────────

    /**
     * Desactiva un contacto (soft delete). Nunca borra el registro físicamente.
     *
     * <p>Solo ADMIN y SUPERADMIN pueden desactivar contactos.
     * SALES_REP y MANAGER no tienen acceso a este endpoint.
     *
     * @param id          UUID del contacto a desactivar
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 204 No Content
     */
    @DeleteMapping("/api/v1/contactos/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<Void> desactivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        contactoService.desactivar(id, autenticado);
        return ApiResponse.noContent();
    }
}
