package com.imsas.erp.modules.artes;

import com.imsas.erp.modules.empresas.Empresa;
import com.imsas.erp.modules.marcas.Marca;
import com.imsas.erp.modules.productos.Producto;
import com.imsas.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "artes")
public class Arte extends BaseEntity {

    
    @Column(unique = true, updatable = false, nullable = false, length = 15)
    private String codigo;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_arte_empresa")
    )
    private Empresa empresa;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "marca_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_arte_marca")
    )
    private Marca marca;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "producto_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_arte_producto")
    )
    private Producto producto;

    
    @Builder.Default
    @Column(name = "version_actual", nullable = false)
    private Short versionActual = 0;
}
