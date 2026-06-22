package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.shared.enums.Costura;
import com.imsas.erp.shared.enums.EstadoSolicitud;
import com.imsas.erp.shared.enums.Tecnica;
import com.imsas.erp.shared.enums.TipoSolicitud;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida para solicitudes.
 * Expone datos desnormalizados de las relaciones para evitar llamadas adicionales
 * desde el frontend.
 */
public record SolicitudResponse(

        UUID id,

        // ─── Códigos ─────────────────────────────────────────────────────────
        /** Código P-[A-Z]{1}[0-9]{5}. Nulo mientras esté en BORRADOR. */
        String codigo,
        /** Código BD-[A-Z]{1}[0-9]{5}-[0-9]{2} con versión actual. Nulo mientras esté en BORRADOR. */
        String codigoBaseDatos,
        Short  versionBd,
        String motivoVersionBd,

        // ─── Asesor ───────────────────────────────────────────────────────────
        UUID   asesorId,
        String asesorNombre,

        // ─── Empresa ──────────────────────────────────────────────────────────
        UUID   empresaId,
        String empresaRazonSocial,

        // ─── Contacto (nullable) ──────────────────────────────────────────────
        UUID   contactoId,
        String contactoNombre,

        // ─── Marca (nullable) ─────────────────────────────────────────────────
        UUID   marcaId,
        String marcaNombre,

        // ─── Producto (nullable) ──────────────────────────────────────────────
        UUID   productoId,
        String productoNombre,
        String detalleProducto,

        // ─── Solicitud origen (nullable) ─────────────────────────────────────
        UUID   solicitudOrigenId,
        /** Código P- de la solicitud origen, si existe. */
        String solicitudOrigenCodigo,

        // ─── Arte vinculado (nullable) ────────────────────────────────────────
        UUID   arteId,
        String arteCodigo,
        Short  arteVersionActual,
        Short  versionArteGenerada,
        Short  cantidadVersionesEntregadas,

        // ─── Clasificación ────────────────────────────────────────────────────
        TipoSolicitud  tipo,
        EstadoSolicitud estado,

        // ─── Datos de producción ──────────────────────────────────────────────
        String     descripcion,
        Integer    cantidad,
        Tecnica    tecnica,
        BigDecimal ancho,
        BigDecimal largo,
        Short      coloresFrente,
        Short      coloresReverso,
        Costura    costura,
        String     ordenCompra,
        BigDecimal precioReferencia,
        BigDecimal abono,
        String     observaciones,

        // ─── Cancelación ─────────────────────────────────────────────────────
        String observacionCancelacion,

        // ─── Auditoría ────────────────────────────────────────────────────────
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {

    /**
     * Construye el DTO a partir de la entidad. Requiere que las relaciones LAZY
     * estén inicializadas (asegurado por {@code @EntityGraph} o transacción abierta).
     */
    public static SolicitudResponse from(Solicitud s) {
        return new SolicitudResponse(
                s.getId(),

                s.getCodigo(),
                s.getCodigoBaseDatos(),
                s.getVersionBd(),
                s.getMotivoVersionBd(),

                s.getAsesor().getId(),
                s.getAsesor().getNombre(),

                s.getEmpresa().getId(),
                s.getEmpresa().getRazonSocial(),

                s.getContacto() != null ? s.getContacto().getId()     : null,
                s.getContacto() != null ? s.getContacto().getNombre() : null,

                s.getMarca()    != null ? s.getMarca().getId()         : null,
                s.getMarca()    != null ? s.getMarca().getNombre()     : null,

                s.getProducto() != null ? s.getProducto().getId()     : null,
                s.getProducto() != null ? s.getProducto().getNombre() : null,
                s.getDetalleProducto(),

                s.getSolicitudOrigen() != null ? s.getSolicitudOrigen().getId()     : null,
                s.getSolicitudOrigen() != null ? s.getSolicitudOrigen().getCodigo() : null,

                s.getArte() != null ? s.getArte().getId()             : null,
                s.getArte() != null ? s.getArte().getCodigo()         : null,
                s.getArte() != null ? s.getArte().getVersionActual()  : null,
                s.getVersionArteGenerada(),
                s.getCantidadVersionesEntregadas(),

                s.getTipo(),
                s.getEstado(),

                s.getDescripcion(),
                s.getCantidad(),
                s.getTecnica(),
                s.getAncho(),
                s.getLargo(),
                s.getColoresFrente(),
                s.getColoresReverso(),
                s.getCostura(),
                s.getOrdenCompra(),
                s.getPrecioReferencia(),
                s.getAbono(),
                s.getObservaciones(),

                s.getObservacionCancelacion(),

                s.isActivo(),
                s.getCreadoEn(),
                s.getActualizadoEn()
        );
    }
}
