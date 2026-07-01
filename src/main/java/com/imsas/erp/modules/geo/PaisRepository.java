package com.imsas.erp.modules.geo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaisRepository extends JpaRepository<Pais, Short> {

    List<Pais> findAllByOrderByNombreAsc();
}
