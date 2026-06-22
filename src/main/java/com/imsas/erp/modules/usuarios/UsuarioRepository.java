package com.imsas.erp.modules.usuarios;

import com.imsas.erp.shared.enums.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    
    Optional<Usuario> findByEmailAndActivoTrue(String email);

    
    boolean existsByEmail(String email);

    
    boolean existsByEmailAndIdNot(String email, UUID id);

    
    Page<Usuario> findAllByActivoTrue(Pageable pageable);

    
    Page<Usuario> findAllByActivoTrueAndRolNot(Rol rol, Pageable pageable);
}
