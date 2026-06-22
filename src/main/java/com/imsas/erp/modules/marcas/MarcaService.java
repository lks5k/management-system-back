package com.imsas.erp.modules.marcas;

import com.imsas.erp.modules.empresas.Empresa;
import com.imsas.erp.modules.empresas.EmpresaRepository;
import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.shared.enums.Rol;
import com.imsas.erp.shared.exceptions.BusinessException;
import com.imsas.erp.shared.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository    marcaRepository;
    private final EmpresaRepository  empresaRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    
    @Transactional(readOnly = true)
    public Page<MarcaResponse> listar(UUID empresaId, Pageable pageable, Usuario autenticado) {
        Empresa empresa = findEmpresaActivaOrThrow(empresaId);
        verificarAccesoSobreEmpresa(empresa, autenticado, "listar marcas de");

        return marcaRepository
                .findAllByEmpresaIdAndActivoTrue(empresaId, pageable)
                .map(MarcaResponse::from);
    }

    
    @Transactional(readOnly = true)
    public MarcaResponse buscarPorId(UUID id, Usuario autenticado) {
        Marca marca = findActivaOrThrow(id);
        verificarAccesoSobreEmpresa(marca.getEmpresa(), autenticado, "consultar la marca de");
        return MarcaResponse.from(marca);
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    
    @Transactional
    public MarcaResponse crear(UUID empresaId, MarcaRequest request, Usuario autenticado) {
        Empresa empresa = findEmpresaActivaOrThrow(empresaId);
        verificarAccesoSobreEmpresa(empresa, autenticado, "crear marcas en");

        String nombreNorm = normalizar(request.nombre());
        verificarNombreUnico(nombreNorm, empresaId, null);

        Marca marca = Marca.builder()
                .nombre(nombreNorm)
                .empresa(empresa)
                .build();

        Marca guardada = marcaRepository.save(marca);
        log.info("Marca creada: '{}' en empresa [{}] por {}",
                guardada.getNombre(), empresaId, autenticado.getEmail());

        return MarcaResponse.from(guardada);
    }

    // ─── Actualización ────────────────────────────────────────────────────────

    
    @Transactional
    public MarcaResponse actualizar(UUID id, MarcaRequest request, Usuario autenticado) {
        Marca marca = findActivaOrThrow(id);
        verificarAccesoSobreEmpresa(marca.getEmpresa(), autenticado, "modificar la marca de");

        String nombreNorm = normalizar(request.nombre());
        verificarNombreUnico(nombreNorm, marca.getEmpresa().getId(), id);

        marca.setNombre(nombreNorm);

        Marca guardada = marcaRepository.save(marca);
        log.info("Marca actualizada: '{}' [{}] por {}",
                guardada.getNombre(), guardada.getId(), autenticado.getEmail());

        return MarcaResponse.from(guardada);
    }

    // ─── Soft delete ──────────────────────────────────────────────────────────

    
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Marca marca = findActivaOrThrow(id);
        marca.setActivo(false);
        marcaRepository.save(marca);
        log.info("Marca desactivada: '{}' [{}] por {}",
                marca.getNombre(), marca.getId(), autenticado.getEmail());
    }

    // ─── Métodos auxiliares privados ──────────────────────────────────────────

    
    private Marca findActivaOrThrow(UUID id) {
        return marcaRepository.findById(id)
                .filter(Marca::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Marca", id));
    }

    
    private Empresa findEmpresaActivaOrThrow(UUID empresaId) {
        return empresaRepository.findById(empresaId)
                .filter(Empresa::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Empresa", empresaId));
    }

    
    private void verificarAccesoSobreEmpresa(Empresa empresa, Usuario autenticado, String accion) {
        if (autenticado.getRol() != Rol.SALES_REP) return;

        boolean esDesuCartera = empresa.getCreadoPor() != null
                && empresa.getCreadoPor().getId().equals(autenticado.getId());

        if (!esDesuCartera) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "No tienes permisos para " + accion + " esta empresa");
        }
    }

    
    private void verificarNombreUnico(String nombreNorm, UUID empresaId, UUID excluirId) {
        boolean existe = excluirId == null
                ? marcaRepository.existsByNombreAndEmpresaIdAndActivoTrue(nombreNorm, empresaId)
                : marcaRepository.existsByNombreAndEmpresaIdAndActivoTrueAndIdNot(nombreNorm, empresaId, excluirId);

        if (existe) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_MARCA",
                    "Ya existe una marca con el nombre '" + nombreNorm + "' en esta empresa");
        }
    }

    
    private String normalizar(String nombre) {
        return nombre.trim().toUpperCase();
    }
}
