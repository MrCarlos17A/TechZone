package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Empleado;
import com.Tienda.TechZone.Model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findFirstByPersona(Persona persona);
}
