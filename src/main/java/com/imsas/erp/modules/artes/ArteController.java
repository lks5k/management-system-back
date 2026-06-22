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

/**
 * Controller REST de solo lectura para el módulo de Artes.
 *
 * <h3>Endpoints</h3>
 * <pre>
 * GET /api/v1/artes       → Listar paginado con filtros opcionales  (SUPERADMIN, ADMIN, MANAGER, SALES_REP)
 * GET /api/v1/artes/{id}  → Obtener Arte por ID (vigente o histórico)(SUPERADMIN, ADMIN, MANAGER, SALES_REP)
 * </pre>
 *
 * <p><b>OPERATOR no tiene acceso a ningún endpoint de este módulo (RN-24).</b>
 *
 * <p>No existen endpoints de escritura en este controller: los Artes se crean y
 * versionan exclusivamente desde el módulo de solicitudes (SolicitudService).
 */
@RestController
@RequiredArgsConstructor
public class ArteController {

    private final ArteService arteService;

    // ─── GET /api/v1/artes ────────────────────────────────────────────────────

    /**
     * Lista Artes con filtros opcionales y paginación.
     *
     * <p>Todos los parámetros de filtro son opcionales: omitirlos equivale a "sin filtro".
     * El parámetro {@code soloVigentes} (por defecto {@code true}) permite incluir
     * Artes históricos ({@code activo = false}) pasando {@code soloVigentes=false}.
     *
     * <p>Ejemplo de uso:
     * <pre>
     * GET /api/v1/artes?empresaId=&lt;uuid&gt;&amp;soloVigentes=true&amp;page=0&amp;size=20
     * GET /api/v1/artes?empresaId=&lt;uuid&gt;&amp;marcaId=&lt;uuid&gt;&amp;soloVigentes=false
     * </pre>
     *
     * @param empresaId    filtro por empresa (UUID); omitir para ver todos
     * @param marcaId      filtro por marca (UUID); omitir para ver todas
     * @param productoId   filtro por producto (UUID); omitir para ver todos
     * @param soloVigentes {@code true} (por defecto) muestra solo Artes activos;
     *                     {@code false} incluye también Artes históricos
     * @param pageable     parámetros de paginación y ordenamiento
     * @return página de {@link ArteResponse} en el envelope estándar
     */
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

    /**
     * Obtiene los datos de un Arte por ID.
     *
     * <p>Devuelve el Arte sin importar si está vigente o histórico, ya que una
     * solicitud puede referenciar un Arte supersedido y el historial debe ser
     * siempre consultable.
     *
     * @param id UUID del Arte a buscar
     * @return datos del Arte en el envelope estándar
     * @throws com.imsas.erp.shared.exceptions.EntityNotFoundException si no existe Arte con ese ID
     */
    @GetMapping("/api/v1/artes/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN','MANAGER','SALES_REP')")
    public ResponseEntity<ApiResponse<ArteResponse>> buscarPorId(
            @PathVariable UUID id
    ) {
        return ApiResponse.ok(arteService.buscarPorId(id));
    }
}
