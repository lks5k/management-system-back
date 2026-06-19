package com.imsas.erp.modules.contactos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Correo electrónico asociado a un {@link Contacto}.
 *
 * <p>Un contacto puede tener múltiples correos. Uno puede marcarse como principal.
 * El formato se valida en el DTO con {@code @Email} (RN-03).
 *
 * <p>Esta entidad NO extiende {@link com.imsas.erp.shared.entity.BaseEntity} porque
 * el MODELO DE DATOS V1.1 no define el campo {@code activo} en {@code contacto_emails}.
 * Solo tiene {@code id}, {@code created_at} y {@code updated_at}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "contacto_emails",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_contacto_email",
                        columnNames = {"contacto_id", "email"}
                )
        }
)
public class ContactoEmail {

    /** Identificador único UUID v4. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Contacto al que pertenece este correo.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "contacto_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_contacto_email_contacto")
    )
    private Contacto contacto;

    /**
     * Dirección de correo electrónico.
     * La validación de formato (RN-03) se aplica en el DTO con {@code @Email}.
     */
    @Column(nullable = false, length = 120)
    private String email;

    /**
     * Indica si este es el correo principal del contacto.
     * La unicidad de {@code esPrincipal = true} por contacto se valida en el servicio.
     */
    @Builder.Default
    @Column(name = "es_principal", nullable = false)
    private boolean esPrincipal = false;

    /** Timestamp de creación en UTC. Asignado en {@link #prePersist()}. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp de última modificación en UTC. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Asigna timestamps de auditoría antes de la primera inserción.
     */
    @jakarta.persistence.PrePersist
    protected void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Actualiza el timestamp de modificación antes de cada UPDATE.
     */
    @jakarta.persistence.PreUpdate
    protected void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
