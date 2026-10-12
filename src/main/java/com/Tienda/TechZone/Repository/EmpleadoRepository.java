package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
}
