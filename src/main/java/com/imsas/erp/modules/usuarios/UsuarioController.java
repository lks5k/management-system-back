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

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ─── GET /api/v1/usuarios ─────────────────────────────────────────────────

    
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

    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN') or #id.equals(authentication.principal.id)")
    public ResponseEntity<ApiResponse<UsuarioResponse>> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(usuarioService.buscarPorId(id, autenticado));
    }

    // ─── POST /api/v1/usuarios ────────────────────────────────────────────────

    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(
            @Valid @RequestBody UsuarioRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(usuarioService.crear(request, autenticado));
    }

    // ─── PUT /api/v1/usuarios/{id} ────────────────────────────────────────────

    
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

    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<Void> desactivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        usuarioService.desactivar(id, autenticado);
        return ApiResponse.noContent();
    }

    // ─── PATCH /api/v1/usuarios/{id}/estado ──────────────────────────────────

    
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarEstadoUsuarioRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(usuarioService.cambiarEstado(id, request, autenticado));
    }

    // ─── PATCH /api/v1/usuarios/{id}/password ─────────────────────────────────

    
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
