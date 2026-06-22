package com.imsas.erp.modules.artes;

import com.imsas.erp.shared.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de solo lectura para el módulo de Artes.
 *
 * <h3>Concepto de acceso</h3>
 * <p>Los Artes no tienen "dueño" ni control de cartera: cualquier solicitud de
 * cualquier asesor puede referenciar el mismo Arte. Por este motivo todos los roles
 * con acceso (SUPERADMIN, ADMIN, MANAGER, SALES_REP) ven el mismo catálogo completo
 * sin restricciones adicionales.
 *
 * <h3>Vigentes vs. históricos</h3>
 * <p>{@link #listar} ofrece el parámetro {@code soloVigentes} para filtrar:
 * <ul>
 *   <li>{@code true}  → solo Artes con {@code activo = true}  (vista operativa).</li>
 *   <li>{@code false} → vigentes e históricos (útil para auditoría o debug).</li>
 * </ul>
 * <p>{@link #buscarPorId} devuelve cualquier Arte sin importar {@code activo}, porque
 * una solicitud puede referenciar un Arte ya supersedido y el historial debe ser
 * consultable (RN-Opción A acordada en sesión 2026-06-22).
 *
 * <h3>OPERATOR</h3>
 * <p>Sin acceso a este módulo (RN-24). Bloqueado en el controller con {@code @PreAuthorize}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArteService {

    private final ArteRepository arteRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    /**
     * Lista Artes con filtros opcionales, paginado.
     *
     * <p>Todos los filtros UUID son nullable; {@code null} significa "no filtrar".
     * El parámetro {@code soloVigentes} controla si se incluyen Artes históricos:
     * <ul>
     *   <li>{@code true}  → pasa {@link Boolean#TRUE} al repositorio → {@code activo = true}.</li>
     *   <li>{@code false} → pasa {@code null} al repositorio → sin restricción de {@code activo}.</li>
     * </ul>
     *
     * @param empresaId    ID de la empresa; {@code null} = todas
     * @param marcaId      ID de la marca; {@code null} = todas
     * @param productoId   ID del producto; {@code null} = todos
     * @param soloVigentes {@code true} para mostrar solo Artes activos
     * @param pageable     configuración de página y ordenamiento
     * @return página de {@link ArteResponse}
     */
    @Transactional(readOnly = true)
    public Page<ArteResponse> listar(
            UUID empresaId,
            UUID marcaId,
            UUID productoId,
            boolean soloVigentes,
            Pageable pageable
    ) {
        Boolean activoFiltro = soloVigentes ? Boolean.TRUE : null;

        log.debug("Listando artes — empresaId={} marcaId={} productoId={} soloVigentes={}",
                empresaId, marcaId, productoId, soloVigentes);

        return arteRepository
                .buscarConFiltros(empresaId, marcaId, productoId, activoFiltro, pageable)
                .map(ArteResponse::from);
    }

    /**
     * Busca un Arte por ID, sin filtrar por {@code activo}.
     *
     * <p>Devuelve tanto Artes vigentes como históricos para que el detalle de una
     * solicitud pueda mostrar el Arte que tenía al momento de su creación, incluso
     * si ese Arte fue supersedido posteriormente.
     *
     * <p>Las asociaciones LAZY (empresa, marca, producto) se cargan dentro de la
     * transacción activa cuando {@link ArteResponse#from(Arte)} accede a ellas.
     *
     * @param id UUID del Arte
     * @return DTO del Arte encontrado
     * @throws EntityNotFoundException si no existe ningún Arte con ese ID
     */
    @Transactional(readOnly = true)
    public ArteResponse buscarPorId(UUID id) {
        Arte arte = arteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Arte", id));

        log.debug("Arte consultado: {} [{}] activo={}", arte.getCodigo(), id, arte.isActivo());

        return ArteResponse.from(arte);
    }
}
