package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.MedioPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedioPagoRepository extends JpaRepository<MedioPago, Long> {
    List<MedioPago> findAllByVigenteTrue();
}
