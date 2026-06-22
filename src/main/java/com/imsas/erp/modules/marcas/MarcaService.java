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

/**
 * Servicio de gestión de marcas comerciales asociadas a empresas cliente.
 *
 * <h3>Modelo de permisos</h3>
 * <ul>
 *   <li><b>SUPERADMIN / ADMIN</b>: CRUD completo sobre cualquier marca.</li>
 *   <li><b>MANAGER</b>: solo lectura sobre todas las marcas.</li>
 *   <li><b>SALES_REP</b>: crea, edita y consulta marcas de empresas de su cartera
 *       ({@code empresa.creadoPor = autenticado}).</li>
 *   <li><b>OPERATOR</b>: sin acceso a este módulo (RN-24). Bloqueado en el controller.</li>
 * </ul>
 *
 * <h3>Reglas críticas implementadas</h3>
 * <ul>
 *   <li>RN-11: nombre normalizado (trim + uppercase) verificado contra marcas activas
 *       de la misma empresa antes de guardar; conflicto exacto → 409 DUPLICATE_MARCA.</li>
 *   <li>RN-25: {@code nombre} siempre se persiste en mayúsculas y sin espacios extremos.</li>
 *   <li>RN-12: soft delete; nunca DELETE físico.</li>
 *   <li>Solo ADMIN/SUPERADMIN pueden desactivar marcas.</li>
 *   <li>SALES_REP no puede operar sobre marcas de empresas ajenas a su cartera.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository    marcaRepository;
    private final EmpresaRepository  empresaRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    /**
     * Lista las marcas activas de una empresa con paginación.
     *
     * <p>SALES_REP solo puede listar marcas de empresas de su cartera.
     *
     * @param empresaId   UUID de la empresa
     * @param pageable    configuración de página y ordenamiento
     * @param autenticado usuario que realiza la consulta
     * @return página de {@link MarcaResponse}
     * @throws EntityNotFoundException si la empresa no existe o no está activa
     * @throws BusinessException       403 si SALES_REP intenta listar marcas de una empresa que no es suya
     */
    @Transactional(readOnly = true)
    public Page<MarcaResponse> listar(UUID empresaId, Pageable pageable, Usuario autenticado) {
        Empresa empresa = findEmpresaActivaOrThrow(empresaId);
        verificarAccesoSobreEmpresa(empresa, autenticado, "listar marcas de");

        return marcaRepository
                .findAllByEmpresaIdAndActivoTrue(empresaId, pageable)
                .map(MarcaResponse::from);
    }

    /**
     * Busca una marca activa por ID.
     *
     * <p>SALES_REP solo puede consultar marcas de empresas de su cartera.
     *
     * @param id          UUID de la marca
     * @param autenticado usuario que realiza la consulta
     * @return DTO de la marca encontrada
     * @throws EntityNotFoundException si no existe marca activa con ese ID
     * @throws BusinessException       403 si SALES_REP no tiene acceso a la empresa de la marca
     */
    @Transactional(readOnly = true)
    public MarcaResponse buscarPorId(UUID id, Usuario autenticado) {
        Marca marca = findActivaOrThrow(id);
        verificarAccesoSobreEmpresa(marca.getEmpresa(), autenticado, "consultar la marca de");
        return MarcaResponse.from(marca);
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    /**
     * Registra una nueva marca en una empresa.
     *
     * <p>El nombre se normaliza (trim + uppercase) antes de la verificación de
     * unicidad y antes de persistir (RN-25). Si ya existe una marca activa con
     * el mismo nombre normalizado en la empresa, se lanza 409 (RN-11).
     *
     * @param empresaId   UUID de la empresa a la que pertenece la marca
     * @param request     datos de la nueva marca validados por JSR-380
     * @param autenticado usuario que realiza la operación
     * @return DTO de la marca creada
     * @throws EntityNotFoundException si la empresa no existe o no está activa
     * @throws BusinessException       403 si SALES_REP no tiene acceso a la empresa;
     *                                 409 si ya existe una marca con ese nombre en la empresa
     */
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

    /**
     * Actualiza el nombre de una marca activa.
     *
     * <p>El nombre se normaliza antes de verificar unicidad y antes de persistir (RN-25 / RN-11).
     * La empresa de la marca no puede cambiarse.
     *
     * @param id          UUID de la marca a actualizar
     * @param request     datos actualizados validados por JSR-380
     * @param autenticado usuario que realiza la operación
     * @return DTO actualizado
     * @throws EntityNotFoundException si no existe marca activa con ese ID
     * @throws BusinessException       403 si SALES_REP no tiene acceso a la empresa;
     *                                 409 si el nuevo nombre ya existe en la empresa
     */
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

    /**
     * Desactiva una marca (soft delete). Solo ADMIN y SUPERADMIN pueden ejecutar
     * esta operación; el controller lo refuerza con {@code @PreAuthorize}.
     *
     * @param id          UUID de la marca a desactivar
     * @param autenticado usuario que realiza la operación
     * @throws EntityNotFoundException si no existe marca activa con ese ID
     */
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Marca marca = findActivaOrThrow(id);
        marca.setActivo(false);
        marcaRepository.save(marca);
        log.info("Marca desactivada: '{}' [{}] por {}",
                marca.getNombre(), marca.getId(), autenticado.getEmail());
    }

    // ─── Métodos auxiliares privados ──────────────────────────────────────────

    /**
     * Busca una marca activa por ID o lanza {@link EntityNotFoundException}.
     *
     * @param id UUID de la marca
     * @return entidad encontrada
     * @throws EntityNotFoundException si no existe marca activa con ese ID
     */
    private Marca findActivaOrThrow(UUID id) {
        return marcaRepository.findById(id)
                .filter(Marca::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Marca", id));
    }

    /**
     * Busca una empresa activa por ID o lanza {@link EntityNotFoundException}.
     *
     * @param empresaId UUID de la empresa
     * @return entidad encontrada
     * @throws EntityNotFoundException si no existe empresa activa con ese ID
     */
    private Empresa findEmpresaActivaOrThrow(UUID empresaId) {
        return empresaRepository.findById(empresaId)
                .filter(Empresa::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Empresa", empresaId));
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
     * Verifica que el nombre normalizado no esté en uso por otra marca activa
     * de la misma empresa (RN-11).
     *
     * @param nombreNorm nombre ya normalizado (trim + uppercase)
     * @param empresaId  ID de la empresa
     * @param excluirId  ID de la marca a excluir en actualizaciones; {@code null} en creación
     * @throws BusinessException 409 si el nombre ya existe en la empresa
     */
    private void verificarNombreUnico(String nombreNorm, UUID empresaId, UUID excluirId) {
        boolean existe = excluirId == null
                ? marcaRepository.existsByNombreAndEmpresaIdAndActivoTrue(nombreNorm, empresaId)
                : marcaRepository.existsByNombreAndEmpresaIdAndActivoTrueAndIdNot(nombreNorm, empresaId, excluirId);

        if (existe) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_MARCA",
                    "Ya existe una marca con el nombre '" + nombreNorm + "' en esta empresa");
        }
    }

    /**
     * Normaliza un nombre de marca: elimina espacios extremos y convierte a mayúsculas (RN-25).
     *
     * @param nombre nombre a normalizar
     * @return nombre normalizado
     */
    private String normalizar(String nombre) {
        return nombre.trim().toUpperCase();
    }
}
