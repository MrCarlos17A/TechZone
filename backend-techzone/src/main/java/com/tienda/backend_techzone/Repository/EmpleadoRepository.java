package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Empleado;
import com.tienda.backend_techzone.Model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findFirstByPersona(Persona persona);
}
