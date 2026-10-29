package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
}
