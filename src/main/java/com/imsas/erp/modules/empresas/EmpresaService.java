package com.imsas.erp.modules.empresas;

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
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    
    @Transactional(readOnly = true)
    public Page<EmpresaResponse> listar(
            String termino,
            TipoEmpresa tipoEmpresa,
            String ciudad,
            Pageable pageable,
            Usuario autenticado
    ) {
        UUID creadoPorId = autenticado.getRol() == Rol.SALES_REP
                ? autenticado.getId()
                : null;

        String terminoNorm = (termino != null && !termino.isBlank()) ? termino.trim() : null;
        String ciudadNorm  = (ciudad  != null && !ciudad.isBlank())  ? ciudad.trim()  : null;

        return empresaRepository
                .buscarConFiltros(terminoNorm, tipoEmpresa, ciudadNorm, creadoPorId, pageable)
                .map(EmpresaResponse::from);
    }

    
    @Transactional(readOnly = true)
    public EmpresaResponse buscarPorId(UUID id, Usuario autenticado) {
        Empresa empresa = findActivaOrThrow(id);
        verificarAccesoSobreEmpresa(empresa, autenticado, "consultar");
        return EmpresaResponse.from(empresa);
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    
    @Transactional
    public EmpresaResponse crear(EmpresaRequest request, Usuario autenticado) {
        verificarNumeroDocumentoUnico(request.numeroDocumento(), null);

        Empresa empresa = Empresa.builder()
                .tipoDocumento(request.tipoDocumento())
                .numeroDocumento(request.numeroDocumento().trim())
                .razonSocial(request.razonSocial().trim().toUpperCase())
                .tipoEmpresa(request.tipoEmpresa())
                .direccion(request.direccion())
                .ciudad(request.ciudad())
                .telefono(request.telefono())
                .sitioWeb(request.sitioWeb())
                .diaCierreContable(request.diaCierreContable())
                .pais(request.pais() != null && !request.pais().isBlank()
                        ? request.pais().trim()
                        : "Colombia")
                .creadoPor(autenticado)
                .build();

        Empresa guardada = empresaRepository.save(empresa);
        log.info("Empresa creada: '{}' [{}] por {}",
                guardada.getRazonSocial(), guardada.getId(), autenticado.getEmail());

        return EmpresaResponse.from(guardada);
    }

    // ─── Actualización ────────────────────────────────────────────────────────

    
    @Transactional
    public EmpresaResponse actualizar(UUID id, EmpresaRequest request, Usuario autenticado) {
        Empresa empresa = findActivaOrThrow(id);
        verificarAccesoSobreEmpresa(empresa, autenticado, "modificar");
        verificarNumeroDocumentoUnico(request.numeroDocumento(), id);

        empresa.setTipoDocumento(request.tipoDocumento());
        empresa.setNumeroDocumento(request.numeroDocumento().trim());
        empresa.setRazonSocial(request.razonSocial().trim().toUpperCase());
        empresa.setTipoEmpresa(request.tipoEmpresa());
        empresa.setDireccion(request.direccion());
        empresa.setCiudad(request.ciudad());
        empresa.setTelefono(request.telefono());
        empresa.setSitioWeb(request.sitioWeb());
        empresa.setDiaCierreContable(request.diaCierreContable());
        empresa.setPais(request.pais() != null && !request.pais().isBlank()
                ? request.pais().trim()
                : "Colombia");

        Empresa guardada = empresaRepository.save(empresa);
        log.info("Empresa actualizada: '{}' [{}] por {}",
                guardada.getRazonSocial(), guardada.getId(), autenticado.getEmail());

        return EmpresaResponse.from(guardada);
    }

    // ─── Soft delete ──────────────────────────────────────────────────────────

    
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Empresa empresa = findActivaOrThrow(id);
        empresa.setActivo(false);
        empresaRepository.save(empresa);
        log.info("Empresa desactivada: '{}' [{}] por {}",
                empresa.getRazonSocial(), empresa.getId(), autenticado.getEmail());
    }

    // ─── Métodos auxiliares privados ──────────────────────────────────────────

    
    private Empresa findActivaOrThrow(UUID id) {
        return empresaRepository.findById(id)
                .filter(Empresa::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Empresa", id));
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

    
    private void verificarNumeroDocumentoUnico(String numeroDocumento, UUID excluirId) {
        String ndNorm = numeroDocumento.trim();
        boolean existe = excluirId == null
                ? empresaRepository.existsByNumeroDocumento(ndNorm)
                : empresaRepository.existsByNumeroDocumentoAndIdNot(ndNorm, excluirId);

        if (existe) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_NUMERO_DOCUMENTO",
                    "El número de documento '" + ndNorm + "' ya está registrado en el sistema");
        }
    }
}
