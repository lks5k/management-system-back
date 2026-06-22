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

@RestController
@RequiredArgsConstructor
public class ContactoController {

    private final ContactoService contactoService;

    // ─── GET /api/v1/empresas/{empresaId}/contactos ───────────────────────────

    
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

    
    @GetMapping("/api/v1/contactos/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<ContactoResponse>> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(contactoService.buscarPorId(id, autenticado));
    }

    // ─── PUT /api/v1/contactos/{id} ───────────────────────────────────────────

    
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
