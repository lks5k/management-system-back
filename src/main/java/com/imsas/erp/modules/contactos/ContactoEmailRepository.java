package com.imsas.erp.modules.contactos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactoEmailRepository extends JpaRepository<ContactoEmail, UUID> {

    
    List<ContactoEmail> findAllByContactoId(UUID contactoId);

    
    boolean existsByContactoIdAndEmail(UUID contactoId, String email);

    
    void deleteAllByContactoId(UUID contactoId);
}
