package com.imsas.erp.modules.artes;

import com.imsas.erp.shared.dto.ApiResponse;
import com.imsas.erp.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ArteController {

    private final ArteService arteService;

    // ─── GET /api/v1/artes ────────────────────────────────────────────────────

    
    @GetMapping("/api/v1/artes")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<PageResponse<ArteResponse>>> listar(
            @RequestParam(required = false) UUID empresaId,
            @RequestParam(required = false) UUID marcaId,
            @RequestParam(required = false) UUID productoId,
            @RequestParam(defaultValue = "true") boolean soloVigentes,
            @PageableDefault(size = 20, sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ArteResponse> pagina = arteService.listar(empresaId, marcaId, productoId, soloVigentes, pageable);
        return PageResponse.ok(pagina);
    }

    // ─── GET /api/v1/artes/{id} ───────────────────────────────────────────────

    
    @GetMapping("/api/v1/artes/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<ArteResponse>> buscarPorId(
            @PathVariable UUID id
    ) {
        return ApiResponse.ok(arteService.buscarPorId(id));
    }
}
