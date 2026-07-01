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

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contactos")
public class Contacto extends BaseEntity {

    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_contacto_empresa")
    )
    private Empresa empresa;

    
    @Column(nullable = false, length = 120)
    private String nombre;

    
    @Column(length = 100)
    private String cargo;

    
    @Column(length = 30)
    private String telefono;

    
    @Builder.Default
    @Column(name = "es_facturacion", nullable = false)
    private boolean esFacturacion = false;

    
    @Builder.Default
    @OneToMany(
            mappedBy = "contacto",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    private List<ContactoEmail> emails = new ArrayList<>();

    
    public void addEmail(ContactoEmail email) {
        emails.add(email);
        email.setContacto(this);
    }
}
