package com.imsas.erp.modules.geo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {

    List<Ciudad> findByDepartamentoIdOrderByNombreAsc(Short departamentoId);
}
