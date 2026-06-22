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

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // ─── GET /api/v1/productos ────────────────────────────────────────────────

    
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

    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(
            @Valid @RequestBody ProductoRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(productoService.crear(request, autenticado));
    }

    // ─── GET /api/v1/productos/{id} ───────────────────────────────────────────

    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<ProductoResponse>> buscarPorId(
            @PathVariable UUID id
    ) {
        return ApiResponse.ok(productoService.buscarPorId(id));
    }

    // ─── PUT /api/v1/productos/{id} ───────────────────────────────────────────

    
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
