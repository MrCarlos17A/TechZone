package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Persona;
import com.tienda.backend_techzone.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.rol LEFT JOIN FETCH u.persona WHERE LOWER(u.correo) = LOWER(:correo)")
    Optional<Usuario> findByCorreo(@Param("correo") String correo);

    boolean existsByCorreo(String correo);
    Optional<Usuario> findFirstByPersona(Persona persona);
    Optional<Usuario> findByPersona_IdPersona(Long idPersona);
    boolean existsByPersona(Persona persona);
}
