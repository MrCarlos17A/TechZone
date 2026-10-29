package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.CentroAyuda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CentroAyudaRepository extends JpaRepository<CentroAyuda, Long> {
    List<CentroAyuda> findByActivoTrueOrderByOrdenAsc();

    List<CentroAyuda> findBySeccionAndActivoTrueOrderByOrdenAsc(String seccion);
}
