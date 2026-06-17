package com.imsas.erp.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Superclase mapeada con los campos comunes a todas las entidades del sistema.
 *
 * <p>Provee:
 * <ul>
 *   <li>{@code id} — UUID v4 generado por la BD (estrategia {@code UUID}).</li>
 *   <li>{@code activo} — flag de eliminación lógica (RN-12). Nunca DELETE físico.</li>
 *   <li>{@code creadoEn} — timestamp de inserción, asignado automáticamente en {@code @PrePersist}.</li>
 *   <li>{@code actualizadoEn} — timestamp de última modificación, actualizado en {@code @PreUpdate}.</li>
 * </ul>
 *
 * <p>Al usar {@code @MappedSuperclass} estos campos se mapean directamente en la tabla
 * de cada subclase, sin generar una tabla propia para {@code BaseEntity}.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * Identificador único de la entidad. UUID v4 generado por PostgreSQL.
     * Nunca se expone como Long autoincrement (convención del proyecto).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Indica si el registro está activo. {@code false} equivale a eliminación lógica (RN-12).
     * Valor por defecto: {@code true}.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Timestamp de creación del registro en UTC.
     * Asignado automáticamente por {@link #prePersist()} y nunca modificable.
     */
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    /**
     * Timestamp de la última modificación del registro en UTC.
     * Actualizado automáticamente por {@link #preUpdate()}.
     */
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    /**
     * Asigna los timestamps de creación y actualización antes de la primera inserción.
     */
    @PrePersist
    protected void prePersist() {
        Instant now = Instant.now();
        this.creadoEn = now;
        this.actualizadoEn = now;
    }

    /**
     * Actualiza el timestamp de modificación antes de cada UPDATE.
     */
    @PreUpdate
    protected void preUpdate() {
        this.actualizadoEn = Instant.now();
    }
}
