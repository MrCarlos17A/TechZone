package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.RegistroAcceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Long> {
    List<RegistroAcceso> findTop100ByOrderByFechaHoraDesc();
}
