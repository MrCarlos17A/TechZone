package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Ubigeo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UbigeoRepository extends JpaRepository<Ubigeo, Long> {
    List<Ubigeo> findAllByVigenteTrue();
}
