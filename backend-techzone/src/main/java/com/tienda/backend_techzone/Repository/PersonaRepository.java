package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByNumeroDocumento(String numeroDocumento);
    Optional<Persona> findFirstByNumeroDocumento(String numeroDocumento);

    @Query("SELECT p FROM Persona p WHERE p.tipoDocumento.idTipoDocumento = :idTipoDocumento AND p.numeroDocumento = :numeroDocumento")
    Optional<Persona> findByTipoDocumentoAndNumeroDocumento(@Param("idTipoDocumento") Long idTipoDocumento, @Param("numeroDocumento") String numeroDocumento);

    @Query("SELECT p FROM Persona p WHERE p.tipoDocumento.idTipoDocumento = :idTipoDocumento AND p.numeroDocumento = :numeroDocumento")
    Optional<Persona> findFirstByTipoDocumentoAndNumeroDocumento(@Param("idTipoDocumento") Long idTipoDocumento, @Param("numeroDocumento") String numeroDocumento);

    Optional<Persona> findByTelefono(String telefono);
    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByTelefono(String telefono);
    long countByNumeroDocumento(String numeroDocumento);

    @Query("SELECT count(p) FROM Persona p WHERE p.tipoDocumento.idTipoDocumento = :idTipoDocumento AND p.numeroDocumento = :numeroDocumento")
    long countByTipoDocumentoAndNumeroDocumento(@Param("idTipoDocumento") Long idTipoDocumento, @Param("numeroDocumento") String numeroDocumento);
}