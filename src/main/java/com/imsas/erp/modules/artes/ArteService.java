package com.imsas.erp.modules.artes;

import com.imsas.erp.shared.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArteService {

    private final ArteRepository arteRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    
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

    
    @Transactional(readOnly = true)
    public ArteResponse buscarPorId(UUID id) {
        Arte arte = arteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Arte", id));

        log.debug("Arte consultado: {} [{}] activo={}", arte.getCodigo(), id, arte.isActivo());

        return ArteResponse.from(arte);
    }
}
