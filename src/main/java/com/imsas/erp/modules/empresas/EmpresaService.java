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

/**
 * Servicio de gestión de empresas cliente.
 *
 * <h3>Modelo de permisos</h3>
 * <ul>
 *   <li><b>SUPERADMIN / ADMIN</b>: CRUD completo sobre cualquier empresa.</li>
 *   <li><b>MANAGER</b>: solo lectura sobre todas las empresas.</li>
 *   <li><b>SALES_REP</b>: crea empresas (queda como {@code creadoPor}); edita y
 *       consulta únicamente las empresas de su propia cartera.</li>
 *   <li><b>OPERATOR</b>: sin acceso a este módulo (RN-24). Bloqueado a nivel de
 *       controller con {@code @PreAuthorize}.</li>
 * </ul>
 *
 * <h3>Reglas críticas implementadas</h3>
 * <ul>
 *   <li>RN-02: unicidad de {@code numeroDocumento} en todo el sistema (activo e inactivo).</li>
 *   <li>Soft delete: nunca se borra físicamente; el campo {@code activo} se pone en {@code false}.</li>
 *   <li>Solo ADMIN/SUPERADMIN pueden desactivar empresas.</li>
 *   <li>SALES_REP no puede ver ni modificar empresas de otros representantes.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    /**
     * Lista empresas activas con paginación y filtros opcionales.
     *
     * <p>SALES_REP solo ve las empresas de su cartera ({@code creadoPor = autenticado}).
     * MANAGER, ADMIN y SUPERADMIN ven todas.
     *
     * @param termino      texto libre sobre razón social o número de documento; {@code null} omite el filtro
     * @param tipoEmpresa  filtro por tipo de constitución; {@code null} omite el filtro
     * @param ciudad       filtro parcial por ciudad (insensible a mayúsculas); {@code null} omite el filtro
     * @param pageable     configuración de página y ordenamiento
     * @param autenticado  usuario que realiza la consulta
     * @return página de {@link EmpresaResponse}
     */
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

    /**
     * Busca una empresa activa por ID.
     *
     * <p>SALES_REP solo puede consultar empresas de su cartera.
     *
     * @param id          UUID de la empresa
     * @param autenticado usuario que realiza la consulta
     * @return DTO de la empresa encontrada
     * @throws EntityNotFoundException si no existe empresa activa con ese ID
     * @throws BusinessException       403 si SALES_REP intenta ver una empresa que no es suya
     */
    @Transactional(readOnly = true)
    public EmpresaResponse buscarPorId(UUID id, Usuario autenticado) {
        Empresa empresa = findActivaOrThrow(id);
        verificarAccesoSobreEmpresa(empresa, autenticado, "consultar");
        return EmpresaResponse.from(empresa);
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    /**
     * Registra una nueva empresa cliente.
     *
     * <p>El usuario autenticado queda almacenado como {@code creadoPor}.
     * Se verifica la unicidad del {@code numeroDocumento} (RN-02) en toda la tabla,
     * incluyendo registros inactivos.
     *
     * @param request     datos de la nueva empresa validados por JSR-380
     * @param autenticado usuario que realiza la operación
     * @return DTO de la empresa creada
     * @throws BusinessException 409 si el número de documento ya existe en el sistema
     */
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

    /**
     * Actualiza los datos de una empresa activa.
     *
     * <p>SALES_REP solo puede actualizar empresas de su propia cartera.
     * El {@code numeroDocumento} puede cambiar mientras no colisione con otra empresa.
     *
     * @param id          UUID de la empresa a actualizar
     * @param request     datos actualizados validados por JSR-380
     * @param autenticado usuario que realiza la operación
     * @return DTO actualizado
     * @throws EntityNotFoundException si no existe empresa activa con ese ID
     * @throws BusinessException       403 si SALES_REP intenta modificar una empresa que no es suya;
     *                                 409 si el número de documento ya pertenece a otra empresa
     */
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

    /**
     * Desactiva una empresa (soft delete).
     *
     * <p>Solo ADMIN y SUPERADMIN pueden desactivar empresas.
     * Esta restricción se aplica también a nivel de {@code @PreAuthorize} en el controller.
     *
     * @param id          UUID de la empresa a desactivar
     * @param autenticado usuario que realiza la operación
     * @throws EntityNotFoundException si no existe empresa activa con ese ID
     */
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Empresa empresa = findActivaOrThrow(id);
        empresa.setActivo(false);
        empresaRepository.save(empresa);
        log.info("Empresa desactivada: '{}' [{}] por {}",
                empresa.getRazonSocial(), empresa.getId(), autenticado.getEmail());
    }

    // ─── Métodos auxiliares privados ──────────────────────────────────────────

    /**
     * Busca una empresa activa por ID o lanza {@link EntityNotFoundException}.
     *
     * @param id UUID de la empresa
     * @return entidad encontrada
     * @throws EntityNotFoundException si no existe empresa activa con ese ID
     */
    private Empresa findActivaOrThrow(UUID id) {
        return empresaRepository.findById(id)
                .filter(Empresa::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Empresa", id));
    }

    /**
     * Verifica que el usuario autenticado tenga acceso para operar sobre la empresa.
     *
     * <p>SALES_REP solo puede operar sobre empresas donde {@code creadoPor} es él mismo.
     * MANAGER, ADMIN y SUPERADMIN tienen acceso sin restricción de cartera.
     *
     * @param empresa     empresa sobre la que se opera
     * @param autenticado usuario que realiza la operación
     * @param accion      descripción de la acción (para el mensaje de error)
     * @throws BusinessException 403 si SALES_REP intenta acceder a una empresa que no es suya
     */
    private void verificarAccesoSobreEmpresa(Empresa empresa, Usuario autenticado, String accion) {
        if (autenticado.getRol() != Rol.SALES_REP) return;

        boolean esDesuCartera = empresa.getCreadoPor() != null
                && empresa.getCreadoPor().getId().equals(autenticado.getId());

        if (!esDesuCartera) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "No tienes permisos para " + accion + " esta empresa");
        }
    }

    /**
     * Verifica que el número de documento no esté en uso por ninguna otra empresa.
     *
     * <p>La unicidad aplica sobre toda la tabla, incluyendo registros inactivos (RN-02).
     *
     * @param numeroDocumento número de documento a verificar
     * @param excluirId       ID de la empresa a excluir en actualizaciones; {@code null} en creación
     * @throws BusinessException 409 si el número de documento ya está registrado
     */
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
