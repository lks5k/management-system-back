package com.imsas.erp.modules.geo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartamentoRepository extends JpaRepository<Departamento, Short> {

    List<Departamento> findByPaisIdOrderByNombreAsc(Short paisId);
}
