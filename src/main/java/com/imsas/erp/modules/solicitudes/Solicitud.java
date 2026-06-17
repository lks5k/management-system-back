package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.modules.contactos.Contacto;
import com.imsas.erp.modules.empresas.Empresa;
import com.imsas.erp.modules.marcas.Marca;
import com.imsas.erp.modules.productos.Producto;
import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.shared.entity.BaseEntity;
import com.imsas.erp.shared.enums.Costura;
import com.imsas.erp.shared.enums.EstadoSolicitud;
import com.imsas.erp.shared.enums.Tecnica;
import com.imsas.erp.shared.enums.TipoSolicitud;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad central del sistema. Representa un requerimiento comercial gestionado
 * por los procesos de diseño y producción de IMSAS.
 *
 * <h3>Ciclo de vida</h3>
 * <pre>
 * BORRADOR → CONFIRMAR → PENDIENTE → COMPLETADO
 *                      ↘
 *                       CANCELADO (desde cualquier estado excepto COMPLETADO)
 * </pre>
 *
 * <h3>Reglas críticas</h3>
 * <ul>
 *   <li>RN-01: {@code empresa} es FK obligatoria.</li>
 *   <li>RN-04/06: {@code codigo} se genera en BORRADOR→PENDIENTE y es inmutable ({@code updatable=false}).</li>
 *   <li>RN-07: {@code codigoBaseDatos} = "BD-" + sufijo de {@code codigo}; {@code versionBd} inicia en 0.</li>
 *   <li>RN-08: {@code versionBd} solo incrementa con {@code motivoVersionBd} obligatorio.</li>
 *   <li>RN-13: {@code observacionCancelacion} obligatoria al pasar a CANCELADO.</li>
 *   <li>RN-14: CANCELADO no puede volver a BORRADOR (validado en servicio).</li>
 *   <li>RN-15: PEDIDO/REPOSICION desde DISEÑO requieren {@code solicitudOrigen} (servicio).</li>
 * </ul>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitudes")
public class Solicitud extends BaseEntity {

    // ─── Relaciones ───────────────────────────────────────────────────────────

    /**
     * Asesor comercial responsable. FK obligatoria → {@code usuarios}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "asesor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_solicitud_asesor")
    )
    private Usuario asesor;

    /**
     * Empresa para la que se realiza la solicitud (RN-01). FK obligatoria → {@code empresas}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_solicitud_empresa")
    )
    private Empresa empresa;

    /**
     * Contacto de la empresa vinculado. FK opcional → {@code contactos}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contacto_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_contacto")
    )
    private Contacto contacto;

    /**
     * Marca vinculada. FK opcional → {@code marcas}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "marca_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_marca")
    )
    private Marca marca;

    /**
     * Producto seleccionado de la lista administrada (RN-16). FK opcional → {@code productos}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "producto_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_producto")
    )
    private Producto producto;

    /**
     * Solicitud de origen para PEDIDO/REPOSICION derivados de un DISEÑO (RN-15).
     * Autorreferencia a la misma tabla {@code solicitudes}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "solicitud_origen_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_origen")
    )
    private Solicitud solicitudOrigen;

    // ─── Códigos (RN-04, RN-06, RN-07, RN-08) ────────────────────────────────

    /**
     * Código único. Formato {@code P-S{N}} (p. ej. {@code P-S59644}).
     * {@code updatable = false} garantiza inmutabilidad a nivel JPA (RN-06).
     * Nulo hasta que la solicitud pasa a PENDIENTE (RN-04).
     */
    @Column(unique = true, updatable = false, length = 20)
    private String codigo;

    /**
     * Código base de datos. Formato {@code BD-S{N}}. Derivado de {@code codigo} (RN-07).
     */
    @Column(name = "codigo_base_datos", unique = true, length = 20)
    private String codigoBaseDatos;

    /**
     * Número de versión del código BD. Inicia en {@code 0} (= sufijo {@code -00}).
     * Solo incrementa con motivo obligatorio (RN-08).
     */
    @Builder.Default
    @Column(name = "version_bd", nullable = false)
    private Short versionBd = 0;

    /**
     * Motivo de la última versión BD. Obligatorio cuando {@code versionBd} > 0 (RN-08).
     */
    @Column(name = "motivo_version_bd", columnDefinition = "TEXT")
    private String motivoVersionBd;

    // ─── Clasificación ────────────────────────────────────────────────────────

    /**
     * Tipo de solicitud. Columna {@code tipo_solicitud} en BD.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitud", nullable = false, length = 20)
    private TipoSolicitud tipo;

    /**
     * Estado actual. Por defecto {@link EstadoSolicitud#BORRADOR}.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.BORRADOR;

    // ─── Datos de producción ──────────────────────────────────────────────────

    /**
     * Descripción libre del requerimiento.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Cantidad de unidades. NOT NULL y mayor a cero (validado en DTO y BD).
     */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Técnica de producción. NOT NULL: obligatoria en toda solicitud.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tecnica tecnica;

    /**
     * Ancho del producto en centímetros. Opcional.
     */
    @Column(precision = 8, scale = 2)
    private BigDecimal ancho;

    /**
     * Largo del producto en centímetros. Opcional.
     */
    @Column(precision = 8, scale = 2)
    private BigDecimal largo;

    /**
     * Número de colores en el frente. Opcional. SMALLINT en BD.
     */
    @Column(name = "colores_frente")
    private Short coloresFrente;

    /**
     * Número de colores en el reverso. Opcional. SMALLINT en BD.
     */
    @Column(name = "colores_reverso")
    private Short coloresReverso;

    /**
     * Posición o tipo de costura. Opcional.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Costura costura;

    /**
     * Número de orden de compra del cliente. Opcional.
     */
    @Column(name = "orden_compra", length = 60)
    private String ordenCompra;

    /**
     * Precio de referencia acordado con el cliente. Opcional.
     */
    @Column(name = "precio_referencia", precision = 12, scale = 2)
    private BigDecimal precioReferencia;

    /**
     * Abono recibido del cliente. Por defecto {@code 0}.
     */
    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal abono = BigDecimal.ZERO;

    /**
     * Observaciones internas adicionales. Opcional.
     */
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // ─── Cancelación (RN-13) ──────────────────────────────────────────────────

    /**
     * Obligatoria cuando {@code estado} = CANCELADO (RN-13). Nulo en otros estados.
     */
    @Column(name = "observacion_cancelacion", columnDefinition = "TEXT")
    private String observacionCancelacion;
}
