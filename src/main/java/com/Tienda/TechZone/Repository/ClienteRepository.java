package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByPersona(Persona persona);
}
