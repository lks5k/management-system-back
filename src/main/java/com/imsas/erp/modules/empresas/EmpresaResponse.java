package com.imsas.erp.modules.empresas;

import java.time.Instant;
import java.util.UUID;

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
        String departamento,
        UUID creadoPorId,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {

    
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
                empresa.getDepartamento(),
                empresa.getCreadoPor() != null ? empresa.getCreadoPor().getId() : null,
                empresa.isActivo(),
                empresa.getCreadoEn(),
                empresa.getActualizadoEn()
        );
    }
}
