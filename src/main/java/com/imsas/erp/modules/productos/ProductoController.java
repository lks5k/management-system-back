package com.imsas.erp.modules.productos;

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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller REST para el catálogo de productos fabricables por IMSAS.
 *
 * <h3>Endpoints</h3>
 * <pre>
 * GET    /api/v1/productos      → Listar paginado   (ADMIN, SUPERADMIN, MANAGER, SALES_REP)
 * POST   /api/v1/productos      → Crear producto    (ADMIN, SUPERADMIN)
 * GET    /api/v1/productos/{id} → Buscar por ID     (ADMIN, SUPERADMIN, MANAGER, SALES_REP)
 * PUT    /api/v1/productos/{id} → Actualizar        (ADMIN, SUPERADMIN)
 * DELETE /api/v1/productos/{id} → Soft delete       (ADMIN, SUPERADMIN)
 * </pre>
 *
 * <p><b>OPERATOR no tiene acceso a ningún endpoint de este módulo (RN-24).</b>
 *
 * <p>SALES_REP y MANAGER tienen acceso de solo lectura: pueden consultar el
 * catálogo para seleccionar el producto al crear una solicitud, pero no pueden
 * crear, modificar ni desactivar productos.
 */
@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // ─── GET /api/v1/productos ────────────────────────────────────────────────

    /**
     * Lista todos los productos activos con paginación.
     *
     * @param pageable    parámetros de paginación ({@code page}, {@code size}, {@code sort})
     * @return página de productos en el envelope estándar
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<PageResponse<ProductoResponse>>> listar(
            @PageableDefault(size = 50, sort = "nombre", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<ProductoResponse> pagina = productoService.listar(pageable);
        return PageResponse.ok(pagina);
    }

    // ─── POST /api/v1/productos ───────────────────────────────────────────────

    /**
     * Registra un nuevo producto en el catálogo.
     *
     * <p>El nombre se normaliza automáticamente en el servicio (trim + uppercase).
     * Si ya existe un producto con el mismo nombre normalizado, devuelve 409.
     *
     * @param request     datos del producto validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 201 Created con los datos del producto creado
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(
            @Valid @RequestBody ProductoRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(productoService.crear(request, autenticado));
    }

    // ─── GET /api/v1/productos/{id} ───────────────────────────────────────────

    /**
     * Obtiene los datos de un producto activo por ID.
     *
     * @param id UUID del producto
     * @return datos del producto en el envelope estándar
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<ProductoResponse>> buscarPorId(
            @PathVariable UUID id
    ) {
        return ApiResponse.ok(productoService.buscarPorId(id));
    }

    // ─── PUT /api/v1/productos/{id} ───────────────────────────────────────────

    /**
     * Actualiza el nombre de un producto activo.
     *
     * <p>El nombre se normaliza automáticamente. Si el nuevo nombre (normalizado)
     * ya existe en el catálogo, devuelve 409.
     *
     * @param id          UUID del producto a actualizar
     * @param request     datos actualizados validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 200 OK con los datos actualizados
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProductoRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(productoService.actualizar(id, request, autenticado));
    }

    // ─── DELETE /api/v1/productos/{id} ────────────────────────────────────────

    /**
     * Desactiva un producto del catálogo (soft delete). Nunca borra físicamente.
     *
     * <p>Solo ADMIN y SUPERADMIN pueden desactivar productos.
     *
     * @param id          UUID del producto a desactivar
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<Void> desactivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        productoService.desactivar(id, autenticado);
        return ApiResponse.noContent();
    }
}
