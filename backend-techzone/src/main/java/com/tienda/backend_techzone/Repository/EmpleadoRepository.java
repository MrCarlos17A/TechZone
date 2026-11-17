package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Empleado;
import com.tienda.backend_techzone.Model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findFirstByPersona(Persona persona);

    @Query("SELECT e FROM Empleado e LEFT JOIN FETCH e.persona p LEFT JOIN FETCH p.tipoDocumento LEFT JOIN FETCH p.ubigeo ORDER BY e.idEmpleado ASC")
    List<Empleado> findAllConRelaciones();
}
