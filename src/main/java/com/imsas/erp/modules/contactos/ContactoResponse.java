package com.imsas.erp.modules.contactos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ContactoResponse(
        UUID id,
        UUID empresaId,
        String nombre,
        String cargo,
        String telefono,
        boolean esFacturacion,
        List<ContactoEmailResponse> emails,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {

    
    public static ContactoResponse from(Contacto contacto, List<ContactoEmail> emails) {
        return new ContactoResponse(
                contacto.getId(),
                contacto.getEmpresa().getId(),
                contacto.getNombre(),
                contacto.getCargo(),
                contacto.getTelefono(),
                contacto.isEsFacturacion(),
                emails.stream().map(ContactoEmailResponse::from).toList(),
                contacto.isActivo(),
                contacto.getCreadoEn(),
                contacto.getActualizadoEn()
        );
    }
}
