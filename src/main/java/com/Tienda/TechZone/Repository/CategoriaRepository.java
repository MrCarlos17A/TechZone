package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
