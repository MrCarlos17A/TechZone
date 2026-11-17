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
    private final DatabaseBootstrapService databaseBootstrapService;

    public List<Rol> ListarRoles() {
        databaseBootstrapService.sincronizarTodo();
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
        if (rol.getEsAdministrativo() == null) {
            rol.setEsAdministrativo(false);
        }
        Long nextId = ((Number) entityManager.createNativeQuery(
                "SELECT COALESCE(MAX(id_rol), 0) + 1 FROM rol").getSingleResult()).longValue();
        entityManager.createNativeQuery(
                "INSERT INTO rol (id_rol, nombre, vigente, es_administrativo, permisos) VALUES (:id, :nombre, :vigente, :esAdmin, :permisos)")
                .setParameter("id", nextId)
                .setParameter("nombre", rol.getNombre().trim())
                .setParameter("vigente", rol.getVigente())
                .setParameter("esAdmin", rol.getEsAdministrativo())
                .setParameter("permisos", rol.getPermisos())
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
        if (rol.getEsAdministrativo() != null) {
            existente.setEsAdministrativo(rol.getEsAdministrativo());
        }
        existente.setPermisos(rol.getPermisos());
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
        if (rol.getNombre().trim().length() > 50) {
            throw new RuntimeException("El nombre no puede superar 50 caracteres");
        }
    }
}
