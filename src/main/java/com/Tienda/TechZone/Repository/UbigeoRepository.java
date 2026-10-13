package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Ubigeo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UbigeoRepository extends JpaRepository<Ubigeo, Long> {
    List<Ubigeo> findAllByVigenteTrue();
}
