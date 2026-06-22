package com.imsas.erp.modules.contactos;

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

import java.util.List;
import java.util.UUID;

/**
 * Servicio de gestión de contactos de empresas cliente.
 *
 * <h3>Modelo de permisos</h3>
 * <ul>
 *   <li><b>SUPERADMIN / ADMIN</b>: CRUD completo sobre cualquier contacto.</li>
 *   <li><b>MANAGER</b>: solo lectura sobre todos los contactos.</li>
 *   <li><b>SALES_REP</b>: crea, edita y consulta contactos de empresas de su cartera.</li>
 *   <li><b>OPERATOR</b>: sin acceso a este módulo (RN-24). Bloqueado en el controller.</li>
 * </ul>
 *
 * <h3>Reglas críticas implementadas</h3>
 * <ul>
 *   <li>Unicidad de correo por contacto (campo {@code email} + {@code contacto_id}):
 *       la constraint de BD {@code uq_contacto_email} es el guard final;
 *       el servicio lo verifica previamente para devolver un error descriptivo.</li>
 *   <li>Unicidad de {@code esPrincipal = true} dentro de la lista de emails
 *       de un mismo contacto: como máximo uno puede ser principal.</li>
 *   <li>{@code esFacturacion} es un campo puramente informativo; no tiene
 *       restricción de unicidad por empresa.</li>
 *   <li>Reemplazo completo de emails en {@link #actualizar}: los emails anteriores
 *       se eliminan físicamente porque {@link ContactoEmail} no tiene valor histórico
 *       propio (no extiende {@code BaseEntity}, sin campo {@code activo}).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactoService {

    private final ContactoRepository      contactoRepository;
    private final ContactoEmailRepository emailRepository;
    private final EmpresaRepository       empresaRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    /**
     * Lista los contactos activos de una empresa con paginación.
     *
     * <p>SALES_REP solo puede listar contactos de empresas de su cartera.
     *
     * @param empresaId   UUID de la empresa
     * @param pageable    configuración de página y ordenamiento
     * @param autenticado usuario que realiza la consulta
     * @return página de {@link ContactoResponse} con los emails de cada contacto
     * @throws EntityNotFoundException si la empresa no existe o no está activa
     * @throws BusinessException       403 si SALES_REP intenta acceder a una empresa que no es suya
     */
    @Transactional(readOnly = true)
    public Page<ContactoResponse> listar(UUID empresaId, Pageable pageable, Usuario autenticado) {
        Empresa empresa = findEmpresaActivaOrThrow(empresaId);
        verificarAccesoSobreEmpresa(empresa, autenticado, "listar contactos de");

        return contactoRepository
                .findAllByEmpresaIdAndActivoTrue(empresaId, pageable)
                .map(c -> ContactoResponse.from(c, emailRepository.findAllByContactoId(c.getId())));
    }

    /**
     * Busca un contacto activo por ID junto con sus correos.
     *
     * <p>SALES_REP solo puede consultar contactos de empresas de su cartera.
     *
     * @param id          UUID del contacto
     * @param autenticado usuario que realiza la consulta
     * @return DTO del contacto encontrado con su lista de emails
     * @throws EntityNotFoundException si no existe contacto activo con ese ID
     * @throws BusinessException       403 si SALES_REP no tiene acceso a la empresa del contacto
     */
    @Transactional(readOnly = true)
    public ContactoResponse buscarPorId(UUID id, Usuario autenticado) {
        Contacto contacto = findActivoOrThrow(id);
        verificarAccesoSobreEmpresa(contacto.getEmpresa(), autenticado, "consultar el contacto de");

        List<ContactoEmail> emails = emailRepository.findAllByContactoId(id);
        return ContactoResponse.from(contacto, emails);
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo contacto en una empresa.
     *
     * <p>Validaciones aplicadas:
     * <ol>
     *   <li>La empresa debe existir y estar activa.</li>
     *   <li>SALES_REP solo puede crear contactos en empresas de su cartera.</li>
     *   <li>La lista de emails no puede tener más de un elemento con {@code esPrincipal = true}.</li>
     *   <li>No puede haber emails duplicados dentro del mismo request.</li>
     * </ol>
     *
     * @param empresaId   UUID de la empresa a la que se agrega el contacto
     * @param request     datos del contacto validados por JSR-380
     * @param autenticado usuario que realiza la operación
     * @return DTO del contacto creado con sus emails
     * @throws EntityNotFoundException si la empresa no existe o no está activa
     * @throws BusinessException       400 si hay emails duplicados o más de un principal;
     *                                 403 si SALES_REP no tiene acceso a la empresa
     */
    @Transactional
    public ContactoResponse crear(UUID empresaId, ContactoRequest request, Usuario autenticado) {
        Empresa empresa = findEmpresaActivaOrThrow(empresaId);
        verificarAccesoSobreEmpresa(empresa, autenticado, "crear contactos en");
        validarListaEmails(request.emails());

        Contacto contacto = Contacto.builder()
                .empresa(empresa)
                .nombre(request.nombre().trim())
                .cargo(request.cargo())
                .telefono(request.telefono())
                .esFacturacion(request.esFacturacion())
                .build();

        for (ContactoEmailRequest emailReq : request.emails()) {
            ContactoEmail email = ContactoEmail.builder()
                    .email(emailReq.email().trim().toLowerCase())
                    .esPrincipal(emailReq.esPrincipal())
                    .build();
            contacto.addEmail(email);
        }

        Contacto guardado = contactoRepository.save(contacto);
        log.info("Contacto creado: '{}' en empresa [{}] por {}",
                guardado.getNombre(), empresaId, autenticado.getEmail());

        List<ContactoEmail> emails = emailRepository.findAllByContactoId(guardado.getId());
        return ContactoResponse.from(guardado, emails);
    }

    // ─── Actualización ────────────────────────────────────────────────────────

    /**
     * Actualiza los datos de un contacto activo y reemplaza su lista de emails.
     *
     * <p>El reemplazo de emails es completo: se eliminan físicamente todos los
     * correos anteriores y se persisten los del request. Esta operación no viola
     * RN-12 porque {@link ContactoEmail} no tiene valor histórico propio.
     *
     * <p>La empresa del contacto no puede cambiarse; el campo {@code empresaId}
     * proviene del contacto existente.
     *
     * @param id          UUID del contacto a actualizar
     * @param request     datos actualizados validados por JSR-380
     * @param autenticado usuario que realiza la operación
     * @return DTO actualizado con la nueva lista de emails
     * @throws EntityNotFoundException si no existe contacto activo con ese ID
     * @throws BusinessException       400 si hay emails duplicados o más de un principal;
     *                                 403 si SALES_REP no tiene acceso a la empresa
     */
    @Transactional
    public ContactoResponse actualizar(UUID id, ContactoRequest request, Usuario autenticado) {
        Contacto contacto = findActivoOrThrow(id);
        verificarAccesoSobreEmpresa(contacto.getEmpresa(), autenticado, "modificar el contacto de");
        validarListaEmails(request.emails());

        contacto.setNombre(request.nombre().trim());
        contacto.setCargo(request.cargo());
        contacto.setTelefono(request.telefono());
        contacto.setEsFacturacion(request.esFacturacion());

        emailRepository.deleteAllByContactoId(id);

        contacto.getEmails().clear();
        for (ContactoEmailRequest emailReq : request.emails()) {
            ContactoEmail email = ContactoEmail.builder()
                    .email(emailReq.email().trim().toLowerCase())
                    .esPrincipal(emailReq.esPrincipal())
                    .build();
            contacto.addEmail(email);
        }

        Contacto guardado = contactoRepository.save(contacto);
        log.info("Contacto actualizado: '{}' [{}] por {}",
                guardado.getNombre(), guardado.getId(), autenticado.getEmail());

        List<ContactoEmail> emails = emailRepository.findAllByContactoId(guardado.getId());
        return ContactoResponse.from(guardado, emails);
    }

    // ─── Soft delete ──────────────────────────────────────────────────────────

    /**
     * Desactiva un contacto (soft delete). Solo ADMIN y SUPERADMIN pueden ejecutar
     * esta operación; el controller lo refuerza con {@code @PreAuthorize}.
     *
     * @param id          UUID del contacto a desactivar
     * @param autenticado usuario que realiza la operación
     * @throws EntityNotFoundException si no existe contacto activo con ese ID
     */
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Contacto contacto = findActivoOrThrow(id);
        contacto.setActivo(false);
        contactoRepository.save(contacto);
        log.info("Contacto desactivado: '{}' [{}] por {}",
                contacto.getNombre(), contacto.getId(), autenticado.getEmail());
    }

    // ─── Métodos auxiliares privados ──────────────────────────────────────────

    /**
     * Busca un contacto activo por ID o lanza {@link EntityNotFoundException}.
     *
     * @param id UUID del contacto
     * @return entidad encontrada
     * @throws EntityNotFoundException si no existe contacto activo con ese ID
     */
    private Contacto findActivoOrThrow(UUID id) {
        return contactoRepository.findById(id)
                .filter(Contacto::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Contacto", id));
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
     * Verifica que el usuario autenticado tenga acceso a la empresa dada.
     *
     * <p>SALES_REP solo puede operar sobre empresas donde él mismo es {@code creadoPor}.
     * MANAGER, ADMIN y SUPERADMIN tienen acceso sin restricción de cartera.
     *
     * @param empresa     empresa que contiene el contacto
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
     * Valida las reglas de negocio sobre la lista de emails del request:
     * <ul>
     *   <li>No puede haber más de un email con {@code esPrincipal = true}.</li>
     *   <li>No puede haber emails duplicados (misma dirección) dentro del mismo request.</li>
     * </ul>
     *
     * @param emails lista de emails del request
     * @throws BusinessException 400 si se viola alguna regla
     */
    private void validarListaEmails(List<ContactoEmailRequest> emails) {
        long principalesCount = emails.stream()
                .filter(ContactoEmailRequest::esPrincipal)
                .count();

        if (principalesCount > 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MULTIPLE_PRINCIPAL_EMAIL",
                    "Solo puede haber un correo marcado como principal por contacto");
        }

        long emailsDistintos = emails.stream()
                .map(e -> e.email().trim().toLowerCase())
                .distinct()
                .count();

        if (emailsDistintos < emails.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "DUPLICATE_EMAIL",
                    "La lista de correos contiene direcciones duplicadas");
        }
    }
}
