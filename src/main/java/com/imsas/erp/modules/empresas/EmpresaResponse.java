package com.imsas.erp.modules.empresas;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida para las operaciones del módulo de empresas.
 *
 * <p>Nunca expone la entidad JPA directamente; aplana solo los campos
 * necesarios para el cliente. El campo {@code creadoPorId} expone únicamente
 * el UUID del usuario que registró la empresa, sin detallar su perfil completo.
 *
 * @param id                 UUID de la empresa
 * @param tipoDocumento      tipo de documento tributario o de identidad
 * @param numeroDocumento    número de documento (NIT con dígito verificación, CC, etc.)
 * @param razonSocial        razón social o nombre legal completo
 * @param tipoEmpresa        tipo de constitución legal
 * @param direccion          dirección física o de correspondencia
 * @param ciudad             ciudad donde opera la empresa
 * @param telefono           número de teléfono principal
 * @param sitioWeb           URL del sitio web
 * @param diaCierreContable  día del mes del cierre contable (1–31), puede ser {@code null}
 * @param pais               país de la empresa
 * @param creadoPorId        UUID del usuario que registró la empresa; {@code null} si fue por seed
 * @param activo             {@code false} indica que la empresa ha sido desactivada (soft delete)
 * @param creadoEn           timestamp UTC de creación del registro
 * @param actualizadoEn      timestamp UTC de la última modificación
 */
public record EmpresaResponse(
        UUID id,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String razonSocial,
        TipoEmpresa tipoEmpresa,
        String direccion,
        String ciudad,
        String telefono,
        String sitioWeb,
        Short diaCierreContable,
        String pais,
        UUID creadoPorId,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {

    /**
     * Construye un {@code EmpresaResponse} a partir de una entidad {@link Empresa}.
     *
     * @param empresa entidad origen; no debe ser {@code null}
     * @return DTO listo para serializar
     */
    public static EmpresaResponse from(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getTipoDocumento(),
                empresa.getNumeroDocumento(),
                empresa.getRazonSocial(),
                empresa.getTipoEmpresa(),
                empresa.getDireccion(),
                empresa.getCiudad(),
                empresa.getTelefono(),
                empresa.getSitioWeb(),
                empresa.getDiaCierreContable(),
                empresa.getPais(),
                empresa.getCreadoPor() != null ? empresa.getCreadoPor().getId() : null,
                empresa.isActivo(),
                empresa.getCreadoEn(),
                empresa.getActualizadoEn()
        );
    }
}
