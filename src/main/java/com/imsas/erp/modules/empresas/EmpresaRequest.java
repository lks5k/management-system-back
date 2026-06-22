package com.imsas.erp.modules.empresas;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear y actualizar una empresa cliente.
 *
 * <h3>Notas de uso</h3>
 * <ul>
 *   <li>Tanto {@code POST} (crear) como {@code PUT} (actualizar) usan este mismo record.</li>
 *   <li>El campo {@code pais} es opcional; si llega {@code null} el servicio aplica
 *       el valor por defecto {@code "Colombia"}.</li>
 *   <li>Para NIT el {@code numeroDocumento} debe incluir el dígito de verificación
 *       (p. ej. {@code "900123456-1"}).</li>
 * </ul>
 *
 * @param tipoDocumento      tipo de documento tributario o de identidad (obligatorio)
 * @param numeroDocumento    número de documento, único en el sistema — RN-02 (obligatorio)
 * @param razonSocial        razón social o nombre legal completo (obligatorio)
 * @param tipoEmpresa        tipo de constitución legal (obligatorio)
 * @param direccion          dirección física o de correspondencia (opcional)
 * @param ciudad             ciudad donde opera la empresa (opcional)
 * @param telefono           número de teléfono principal (opcional)
 * @param sitioWeb           URL del sitio web (opcional)
 * @param diaCierreContable  día del mes del cierre contable, entre 1 y 31 (opcional)
 * @param pais               país de la empresa; si {@code null} se asume {@code "Colombia"} (opcional)
 */
public record EmpresaRequest(

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 30, message = "El número de documento no puede superar 30 caracteres")
        String numeroDocumento,

        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 200, message = "La razón social no puede superar 200 caracteres")
        String razonSocial,

        @NotNull(message = "El tipo de empresa es obligatorio")
        TipoEmpresa tipoEmpresa,

        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion,

        @Size(max = 100, message = "La ciudad no puede superar 100 caracteres")
        String ciudad,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono,

        @Size(max = 200, message = "El sitio web no puede superar 200 caracteres")
        String sitioWeb,

        @Min(value = 1, message = "El día de cierre contable debe ser entre 1 y 31")
        @Max(value = 31, message = "El día de cierre contable debe ser entre 1 y 31")
        Short diaCierreContable,

        @Size(max = 60, message = "El país no puede superar 60 caracteres")
        String pais
) {}
