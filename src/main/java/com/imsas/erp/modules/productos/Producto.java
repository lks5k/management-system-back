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

/**
 * Entidad que representa un tipo de producto fabricable por IMSAS.
 *
 * <p>Lista administrable exclusivamente por {@code ADMIN} y {@code SUPERADMIN} (RN-16).
 * Los asesores seleccionan el producto desde esta lista al crear una solicitud;
 * no pueden escribir el nombre libremente.
 *
 * <p>Ejemplos: "Etiqueta tejida", "Etiqueta digital", "Marquilla corte".
 */
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

    /**
     * Nombre del producto. Único en el sistema.
     */
    @Column(nullable = false, length = 120)
    private String nombre;
}
