package com.imsas.erp.modules.productos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    
    Page<Producto> findAllByActivoTrue(Pageable pageable);

    
    boolean existsByNombre(String nombre);

    
    boolean existsByNombreAndIdNot(String nombre, UUID id);
}
