package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.MedioPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedioPagoRepository extends JpaRepository<MedioPago, Integer> {
    List<MedioPago> findAllByVigenteTrue();
}
