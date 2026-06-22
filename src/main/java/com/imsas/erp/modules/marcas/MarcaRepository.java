package com.imsas.erp.modules.marcas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, UUID> {

    
    Page<Marca> findAllByEmpresaIdAndActivoTrue(UUID empresaId, Pageable pageable);

    
    boolean existsByNombreAndEmpresaIdAndActivoTrue(String nombre, UUID empresaId);

    
    boolean existsByNombreAndEmpresaIdAndActivoTrueAndIdNot(String nombre, UUID empresaId, UUID id);
}
