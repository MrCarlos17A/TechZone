package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Rol;
import com.tienda.backend_techzone.Repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class RolService {

    private final RolRepository rolRepository;

    public List<Rol> ListarRoles() {
        return rolRepository.findAll();
    }

    public List<Rol> listarRoles() {
        return ListarRoles();
    }

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @org.springframework.transaction.annotation.Transactional
    public Rol CrearRol(Rol rol) {
        validarRol(rol);
        if (rol.getVigente() == null) {
            rol.setVigente(true);
        }
        Long nextId = ((Number) entityManager.createNativeQuery(
                "SELECT COALESCE(MAX(id_rol), 0) + 1 FROM rol").getSingleResult()).longValue();
        entityManager.createNativeQuery(
                "INSERT INTO rol (id_rol, nombre, vigente) VALUES (:id, :nombre, :vigente)")
                .setParameter("id", nextId)
                .setParameter("nombre", rol.getNombre().trim())
                .setParameter("vigente", rol.getVigente())
                .executeUpdate();
        rol.setIdRol(nextId);
        return rol;
    }

    public Rol ActualizarRol(Long id, Rol rol) {
        Rol existente = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + id));
        validarRol(rol);
        existente.setNombre(rol.getNombre().trim());
        if (rol.getVigente() != null) {
            existente.setVigente(rol.getVigente());
        }
        return rolRepository.save(existente);
    }

    public void EliminarRol(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + id));
        rol.setVigente(false);
        rolRepository.save(rol);
    }

    public Rol ObtenerRolPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + id));
    }

    private void validarRol(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del rol es obligatorio");
        }
        if (rol.getNombre().trim().length() > 20) {
            throw new RuntimeException("El nombre no puede superar 20 caracteres");
        }
    }
}
