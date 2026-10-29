package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Cliente;
import com.tienda.backend_techzone.Model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByPersona(Persona persona);
    Optional<Cliente> findFirstByPersona(Persona persona);

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.persona p ORDER BY c.idCliente ASC")
    List<Cliente> findAllConPersona();
}
