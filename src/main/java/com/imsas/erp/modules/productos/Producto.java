package com.imsas.erp.modules.productos;

import com.imsas.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "productos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_producto_nombre",
                        columnNames = "nombre"
                )
        }
)
public class Producto extends BaseEntity {

    
    @Column(nullable = false, length = 120)
    private String nombre;

    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
