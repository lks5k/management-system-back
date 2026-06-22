package com.imsas.erp.modules.usuarios;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller REST para gestión de usuarios.
 *
 * <h3>Endpoints</h3>
 * <pre>
 * GET    /api/v1/usuarios              → Listar paginado      (ADMIN, SUPERADMIN)
 * GET    /api/v1/usuarios/{id}         → Buscar por ID        (ADMIN, SUPERADMIN, propio perfil)
 * POST   /api/v1/usuarios              → Crear usuario        (ADMIN, SUPERADMIN)
 * PUT    /api/v1/usuarios/{id}         → Actualizar usuario   (ADMIN, SUPERADMIN)
 * DELETE /api/v1/usuarios/{id}         → Soft delete          (ADMIN, SUPERADMIN)
 * PATCH  /api/v1/usuarios/{id}/password → Cambiar contraseña  (propio usuario o ADMIN/SUPERADMIN)
 * </pre>
 *
 * <p>La autorización fina (no tocar SUPERADMIN siendo ADMIN, no autodesactivarse, etc.)
 * se delega al {@link UsuarioService}, que lanza {@code BusinessException} con el
 * status HTTP apropiado cuando se viola una regla.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ─── GET /api/v1/usuarios ─────────────────────────────────────────────────

    /**
     * Lista usuarios activos con paginación.
     *
     * <p>SUPERADMIN ve todos; ADMIN ve todos excepto SUPERADMIN.
     * Por defecto ordena por {@code creadoEn} descendente.
     *
     * @param pageable    parámetros de paginación ({@code page}, {@code size}, {@code sort})
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return página de usuarios en el envelope estándar
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UsuarioResponse>>> listar(
            @PageableDefault(size = 20, sort = "creadoEn", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        Page<UsuarioResponse> pagina = usuarioService.listar(pageable, autenticado);
        return PageResponse.ok(pagina);
    }

    // ─── GET /api/v1/usuarios/{id} ────────────────────────────────────────────

    /**
     * Obtiene el perfil de un usuario por ID.
     *
     * <p>ADMIN y SUPERADMIN pueden consultar cualquier perfil (con restricción sobre
     * SUPERADMIN para ADMIN, manejada en el servicio).
     * Cualquier usuario autenticado puede consultar su propio perfil.
     *
     * @param id          UUID del usuario
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return perfil del usuario en el envelope estándar
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN') or #id.equals(authentication.principal.id)")
    public ResponseEntity<ApiResponse<UsuarioResponse>> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(usuarioService.buscarPorId(id, autenticado));
    }

    // ─── POST /api/v1/usuarios ────────────────────────────────────────────────

    /**
     * Crea un nuevo usuario.
     *
     * <p>El cuerpo debe incluir {@code passwordInicial} (mín. 8 caracteres).
     * Solo SUPERADMIN puede crear usuarios con rol {@code SUPERADMIN}.
     *
     * @param request     datos del nuevo usuario validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 201 Created con el perfil del usuario creado
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(
            @Valid @RequestBody UsuarioRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(usuarioService.crear(request, autenticado));
    }

    // ─── PUT /api/v1/usuarios/{id} ────────────────────────────────────────────

    /**
     * Actualiza nombre, email y rol de un usuario activo.
     *
     * <p>La contraseña no se modifica por este endpoint; para eso existe
     * {@code PATCH /api/v1/usuarios/{id}/password}.
     *
     * @param id          UUID del usuario a actualizar
     * @param request     datos actualizados validados por JSR-380
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 200 OK con el perfil actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody UsuarioRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(usuarioService.actualizar(id, request, autenticado));
    }

    // ─── DELETE /api/v1/usuarios/{id} ─────────────────────────────────────────

    /**
     * Desactiva un usuario (soft delete — RN-12). Nunca borra el registro físicamente.
     *
     * <p>Restricciones adicionales manejadas en el servicio:
     * un usuario no puede desactivarse a sí mismo, y ADMIN no puede
     * desactivar a un SUPERADMIN.
     *
     * @param id          UUID del usuario a desactivar
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<Void> desactivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        usuarioService.desactivar(id, autenticado);
        return ApiResponse.noContent();
    }

    // ─── PATCH /api/v1/usuarios/{id}/password ─────────────────────────────────

    /**
     * Cambia la contraseña de un usuario activo.
     *
     * <p>Si el solicitante es el <b>propio usuario</b>, el campo {@code passwordActual}
     * es obligatorio y se verifica. Si es <b>ADMIN o SUPERADMIN</b> actuando sobre
     * otro usuario, {@code passwordActual} se ignora (reset administrativo).
     * ADMIN no puede resetear la contraseña de un SUPERADMIN.
     *
     * @param id          UUID del usuario cuya contraseña se cambia
     * @param request     DTO con {@code passwordActual} (opcional) y {@code passwordNuevo}
     * @param autenticado usuario autenticado inyectado por Spring Security
     * @return 204 No Content
     */
    @PatchMapping("/{id}/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cambiarPassword(
            @PathVariable UUID id,
            @Valid @RequestBody CambioPasswordRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        usuarioService.cambiarPassword(id, request, autenticado);
        return ApiResponse.noContent();
    }
}
