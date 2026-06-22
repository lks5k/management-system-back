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

    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "contacto_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_contacto_email_contacto")
    )
    private Contacto contacto;

    
    @Column(nullable = false, length = 120)
    private String email;

    
    @Builder.Default
    @Column(name = "es_principal", nullable = false)
    private boolean esPrincipal = false;

    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    
    @jakarta.persistence.PrePersist
    protected void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    
    @jakarta.persistence.PreUpdate
    protected void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
