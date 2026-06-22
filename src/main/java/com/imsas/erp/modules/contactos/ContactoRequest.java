package com.imsas.erp.modules.contactos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO de entrada para crear y actualizar un contacto.
 *
 * <h3>Notas de uso</h3>
 * <ul>
 *   <li>El {@code empresaId} <b>no</b> forma parte de este record: en creación
 *       proviene del path variable ({@code /empresas/{empresaId}/contactos}),
 *       y en actualización la empresa de un contacto no cambia.</li>
 *   <li>{@code esFacturacion} es un campo puramente informativo; no hay restricción
 *       de unicidad por empresa.</li>
 *   <li>{@code emails} debe tener al menos un elemento. Si más de uno tiene
 *       {@code esPrincipal = true} el servicio rechaza la petición con 400.</li>
 * </ul>
 *
 * @param nombre        nombre completo del contacto
 * @param cargo         cargo o rol del contacto en la empresa (opcional)
 * @param telefono      número de teléfono del contacto (opcional)
 * @param esFacturacion {@code true} si el contacto es responsable de facturación (informativo)
 * @param emails        lista de correos electrónicos; mínimo uno requerido
 */
public record ContactoRequest(

        @NotBlank(message = "El nombre del contacto es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @Size(max = 100, message = "El cargo no puede superar 100 caracteres")
        String cargo,

        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono,

        boolean esFacturacion,

        @NotEmpty(message = "El contacto debe tener al menos un correo electrónico")
        @Valid
        List<ContactoEmailRequest> emails
) {}
