package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.modules.artes.Arte;
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
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitudes")
public class Solicitud extends BaseEntity {

    // ─── Relaciones ───────────────────────────────────────────────────────────

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "asesor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_solicitud_asesor")
    )
    private Usuario asesor;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_solicitud_empresa")
    )
    private Empresa empresa;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "contacto_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_contacto")
    )
    private Contacto contacto;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "marca_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_marca")
    )
    private Marca marca;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "producto_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_producto")
    )
    private Producto producto;

    
    @Column(name = "detalle_producto", length = 200)
    private String detalleProducto;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "solicitud_origen_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_origen")
    )
    private Solicitud solicitudOrigen;

    // ─── Códigos (RN-04, RN-06, RN-07, RN-08) ────────────────────────────────

    
    @Column(unique = true, updatable = false, length = 20)
    private String codigo;

    
    @Column(name = "codigo_base_datos", unique = true, length = 20)
    private String codigoBaseDatos;

    
    @Builder.Default
    @Column(name = "version_bd", nullable = false)
    private Short versionBd = 0;

    
    @Column(name = "motivo_version_bd", columnDefinition = "TEXT")
    private String motivoVersionBd;

    // ─── Clasificación ────────────────────────────────────────────────────────

    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitud", nullable = false, length = 20)
    private TipoSolicitud tipo;

    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.BORRADOR;

    // ─── Datos de producción ──────────────────────────────────────────────────

    
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    
    @Column(nullable = false)
    private Integer cantidad;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tecnica tecnica;

    
    @Column(precision = 8, scale = 2)
    private BigDecimal ancho;

    
    @Column(precision = 8, scale = 2)
    private BigDecimal largo;

    
    @Column(name = "colores_frente")
    private Short coloresFrente;

    
    @Column(name = "colores_reverso")
    private Short coloresReverso;

    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Costura costura;

    
    @Column(name = "orden_compra", length = 60)
    private String ordenCompra;

    
    @Column(name = "precio_referencia", precision = 12, scale = 2)
    private BigDecimal precioReferencia;

    
    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal abono = BigDecimal.ZERO;

    
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // ─── Cancelación (RN-13) ──────────────────────────────────────────────────

    
    @Column(name = "observacion_cancelacion", columnDefinition = "TEXT")
    private String observacionCancelacion;

    // ─── Arte vinculado (RN-18, RN-19, RN-20) ────────────────────────────────

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "arte_id",
            foreignKey = @ForeignKey(name = "fk_solicitud_arte")
    )
    private Arte arte;

    
    @Column(name = "version_arte_generada")
    private Short versionArteGenerada;

    
    @Column(name = "cantidad_versiones_entregadas")
    private Short cantidadVersionesEntregadas;
}
