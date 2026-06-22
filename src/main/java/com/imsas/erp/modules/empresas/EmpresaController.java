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

@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    // ─── GET /api/v1/empresas ─────────────────────────────────────────────────

    
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

    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<EmpresaResponse>> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.ok(empresaService.buscarPorId(id, autenticado));
    }

    // ─── POST /api/v1/empresas ────────────────────────────────────────────────

    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','SALES_REP')")
    public ResponseEntity<ApiResponse<EmpresaResponse>> crear(
            @Valid @RequestBody EmpresaRequest request,
            @AuthenticationPrincipal Usuario autenticado
    ) {
        return ApiResponse.created(empresaService.crear(request, autenticado));
    }

    // ─── PUT /api/v1/empresas/{id} ────────────────────────────────────────────

    
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
