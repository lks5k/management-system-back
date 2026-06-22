package com.imsas.erp.modules.marcas;

import com.imsas.erp.modules.empresas.Empresa;
import com.imsas.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
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
        name = "marcas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_marca_nombre_empresa",
                        columnNames = {"nombre", "empresa_id"}
                )
        }
)
public class Marca extends BaseEntity {

    
    @Column(nullable = false, length = 120)
    private String nombre;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_marca_empresa")
    )
    private Empresa empresa;
}
