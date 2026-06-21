package com.imsas.erp.modules.contactos;

import com.imsas.erp.modules.empresas.Empresa;
import com.imsas.erp.shared.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a una persona de contacto perteneciente a una empresa cliente.
 *
 * <p>Una empresa puede tener múltiples contactos (RN-10). Un contacto puede tener
 * múltiples correos electrónicos gestionados a través de {@link ContactoEmail}.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contactos")
public class Contacto extends BaseEntity {

    /**
     * Empresa a la que pertenece este contacto. FK obligatoria.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_contacto_empresa")
    )
    private Empresa empresa;

    /**
     * Nombre completo del contacto.
     */
    @Column(nullable = false, length = 120)
    private String nombre;

    /**
     * Cargo o rol del contacto dentro de la empresa.
     * Ejemplo: {@code "Comprador"}, {@code "Coordinador de producción"}.
     */
    @Column(length = 100)
    private String cargo;

    /**
     * Número de teléfono del contacto. Opcional.
     */
    @Column(length = 30)
    private String telefono;

    /**
     * Indica si este contacto es el responsable de facturación de la empresa.
     * Flag informativo; una empresa puede tener varios contactos con este flag en {@code true}.
     */
    @Column(name = "es_facturacion", nullable = false)
    private boolean esFacturacion = false;

    /**
     * Correos electrónicos del contacto.
     *
     * <p>Cascade PERSIST y MERGE solamente: la eliminación de correos es lógica
     * (campo {@code activo} en {@link ContactoEmail}), conforme a RN-12.
     */
    @Builder.Default
    @OneToMany(
            mappedBy = "contacto",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    private List<ContactoEmail> emails = new ArrayList<>();

    /**
     * Agrega un correo y establece la referencia inversa para mantener coherencia
     * de la relación bidireccional en memoria.
     *
     * @param email correo a asociar
     */
    public void addEmail(ContactoEmail email) {
        emails.add(email);
        email.setContacto(this);
    }
}
