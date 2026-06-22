package com.imsas.erp.modules.contactos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida para las operaciones del módulo de contactos.
 *
 * <p>Incluye la lista completa de correos activos del contacto como
 * {@link ContactoEmailResponse}. Los emails se pasan explícitamente al
 * factory method porque la colección en la entidad es LAZY y puede no
 * estar inicializada fuera de una transacción activa.
 *
 * @param id            UUID del contacto
 * @param empresaId     UUID de la empresa a la que pertenece
 * @param nombre        nombre completo del contacto
 * @param cargo         cargo o rol dentro de la empresa
 * @param telefono      número de teléfono
 * @param esFacturacion {@code true} si el contacto es responsable de facturación (informativo)
 * @param emails        correos electrónicos del contacto
 * @param activo        {@code false} indica que el contacto ha sido desactivado (soft delete)
 * @param creadoEn      timestamp UTC de creación del registro
 * @param actualizadoEn timestamp UTC de la última modificación
 */
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

    /**
     * Construye un {@code ContactoResponse} a partir de la entidad {@link Contacto}
     * y su lista de emails ya cargada.
     *
     * <p>Los emails se reciben como parámetro separado para evitar el acceso a la
     * colección LAZY fuera de contexto transaccional.
     *
     * @param contacto entidad origen; no debe ser {@code null}
     * @param emails   lista de emails del contacto ya resuelta en la transacción
     * @return DTO listo para serializar
     */
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
