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

    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private TipoDocumento tipoDocumento;

    
    @Column(name = "numero_documento", nullable = false, length = 30)
    private String numeroDocumento;

    
    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_empresa", nullable = false, length = 20)
    private TipoEmpresa tipoEmpresa;

    
    @Column(length = 200)
    private String direccion;

    
    @Column(length = 100)
    private String ciudad;

    
    @Column(length = 30)
    private String telefono;

    
    @Column(name = "sitio_web", length = 200)
    private String sitioWeb;

    
    @Column(name = "dia_cierre_contable")
    private Short diaCierreContable;

    
    @Builder.Default
    @Column(nullable = false, length = 60)
    private String pais = "Colombia";

    @Column(length = 100)
    private String departamento;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "creado_por",
            foreignKey = @ForeignKey(name = "fk_empresa_creado_por")
    )
    private Usuario creadoPor;
}
