package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
