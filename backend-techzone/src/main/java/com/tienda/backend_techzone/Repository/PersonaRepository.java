package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByNumeroDocumento(String numeroDocumento);
    Optional<Persona> findFirstByNumeroDocumento(String numeroDocumento);
    Optional<Persona> findByTipoDocumentoAndNumeroDocumento(Long idTipoDocumento, String numeroDocumento);
    Optional<Persona> findFirstByTipoDocumentoAndNumeroDocumento(Long idTipoDocumento, String numeroDocumento);
    Optional<Persona> findByTelefono(String telefono);
    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByTelefono(String telefono);
    long countByNumeroDocumento(String numeroDocumento);
    long countByTipoDocumentoAndNumeroDocumento(Long idTipoDocumento, String numeroDocumento);
}