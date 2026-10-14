package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Persona;
import com.Tienda.TechZone.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findFirstByPersona(Persona persona);
    Optional<Usuario> findByPersona_IdPersona(Long idPersona);
    boolean existsByPersona(Persona persona);
}
