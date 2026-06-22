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

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactoService {

    private final ContactoRepository      contactoRepository;
    private final ContactoEmailRepository emailRepository;
    private final EmpresaRepository       empresaRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    
    @Transactional(readOnly = true)
    public Page<ContactoResponse> listar(UUID empresaId, Pageable pageable, Usuario autenticado) {
        Empresa empresa = findEmpresaActivaOrThrow(empresaId);
        verificarAccesoSobreEmpresa(empresa, autenticado, "listar contactos de");

        return contactoRepository
                .findAllByEmpresaIdAndActivoTrue(empresaId, pageable)
                .map(c -> ContactoResponse.from(c, emailRepository.findAllByContactoId(c.getId())));
    }

    
    @Transactional(readOnly = true)
    public ContactoResponse buscarPorId(UUID id, Usuario autenticado) {
        Contacto contacto = findActivoOrThrow(id);
        verificarAccesoSobreEmpresa(contacto.getEmpresa(), autenticado, "consultar el contacto de");

        List<ContactoEmail> emails = emailRepository.findAllByContactoId(id);
        return ContactoResponse.from(contacto, emails);
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    
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

    
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Contacto contacto = findActivoOrThrow(id);
        contacto.setActivo(false);
        contactoRepository.save(contacto);
        log.info("Contacto desactivado: '{}' [{}] por {}",
                contacto.getNombre(), contacto.getId(), autenticado.getEmail());
    }

    // ─── Métodos auxiliares privados ──────────────────────────────────────────

    
    private Contacto findActivoOrThrow(UUID id) {
        return contactoRepository.findById(id)
                .filter(Contacto::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Contacto", id));
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
