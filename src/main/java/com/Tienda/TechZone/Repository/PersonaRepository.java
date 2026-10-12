package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByNumeroDocumento(String numeroDocumento);
    Optional<Persona> findByTipoDocumentoAndNumeroDocumento(Integer idTipoDocumento, String numeroDocumento);
}