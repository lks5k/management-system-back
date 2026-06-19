package com.imsas.erp.modules.empresas;

import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa a una empresa cliente de IMSAS.
 *
 * <p>Es la entidad raíz del modelo: toda solicitud debe pertenecer a una
 * empresa (RN-01), y los contactos y marcas también están asociados a ella.
 *
 * <p>El número de documento es único por empresa (RN-02), garantizado por
 * la restricción {@code uq_empresa_numero_documento}.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "empresas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_empresa_numero_documento",
                        columnNames = "numero_documento"
                )
        }
)
public class Empresa extends BaseEntity {

    /**
     * Tipo de documento tributario o de identidad de la empresa.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private TipoDocumento tipoDocumento;

    /**
     * Número de documento. Único en el sistema (RN-02).
     * Para NIT incluye el dígito de verificación (p. ej. {@code "900123456-1"}).
     */
    @Column(name = "numero_documento", nullable = false, length = 30)
    private String numeroDocumento;

    /**
     * Razón social o nombre legal completo de la empresa.
     */
    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    /**
     * Tipo de constitución legal de la empresa (SAS, SA, LTDA, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empresa", nullable = false, length = 20)
    private TipoEmpresa tipoEmpresa;

    /**
     * Dirección física o de correspondencia. Opcional.
     */
    @Column(length = 200)
    private String direccion;

    /**
     * Ciudad donde opera la empresa. Opcional.
     */
    @Column(length = 100)
    private String ciudad;

    /**
     * Número de teléfono principal. Opcional.
     */
    @Column(length = 30)
    private String telefono;

    /**
     * Sitio web de la empresa. Opcional.
     */
    @Column(name = "sitio_web", length = 200)
    private String sitioWeb;

    /**
     * Día del mes en que la empresa cierra su período contable (1-31). Opcional.
     * Se usa para programar recordatorios o cortes en futuros módulos.
     */
    @Column(name = "dia_cierre_contable")
    private Short diaCierreContable;

    /**
     * País de la empresa. Por defecto {@code "Colombia"}.
     */
    @Builder.Default
    @Column(nullable = false, length = 60)
    private String pais = "Colombia";

    /**
     * Usuario que registró la empresa en el sistema.
     * FK opcional; permite auditar quién creó el registro.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "creado_por",
            foreignKey = @ForeignKey(name = "fk_empresa_creado_por")
    )
    private Usuario creadoPor;
}
