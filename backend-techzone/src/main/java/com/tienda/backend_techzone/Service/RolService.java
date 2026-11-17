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
        asegurarRolesBase();
        return rolRepository.findAll();
    }

    public List<Rol> listarRoles() {
        return ListarRoles();
    }

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private void asegurarRolesBase() {
        try {
            insertarRolSiNoExiste("Supervisor de Ventas",
                    "{\"dashboard\":[\"ver\"],\"ventas\":[\"ver\",\"crear\",\"editar\"],\"pagos\":[\"ver\"],\"clientes\":[\"ver\"]}");
            insertarRolSiNoExiste("Logística y Almacén",
                    "{\"productos\":[\"ver\",\"crear\",\"editar\"],\"categorias\":[\"ver\",\"crear\",\"editar\"],\"marcas\":[\"ver\",\"crear\",\"editar\"],\"imagenes\":[\"ver\",\"crear\",\"editar\"],\"ventas\":[\"ver\"]}");
            insertarRolSiNoExiste("Soporte Técnico",
                    "{\"productos\":[\"ver\"],\"ventas\":[\"ver\"],\"clientes\":[\"ver\"]}");
        } catch (Exception e) {
            System.err.println("Aviso al asegurar roles base: " + e.getMessage());
        }
    }

    private void insertarRolSiNoExiste(String nombre, String permisos) {
        boolean existe = rolRepository.findByNombre(nombre).isPresent();
        if (!existe) {
            try {
                Long nextId = ((Number) entityManager.createNativeQuery(
                        "SELECT COALESCE(MAX(id_rol), 0) + 1 FROM rol").getSingleResult()).longValue();
                entityManager.createNativeQuery(
                        "INSERT INTO rol (id_rol, nombre, vigente, permisos) VALUES (:id, :nombre, true, :permisos) ON CONFLICT (id_rol) DO NOTHING")
                        .setParameter("id", nextId)
                        .setParameter("nombre", nombre)
                        .setParameter("permisos", permisos)
                        .executeUpdate();
            } catch (Exception ex) {
                // Alternativa sin ID explicito en caso de secuencia activa
                try {
                    entityManager.createNativeQuery(
                            "INSERT INTO rol (nombre, vigente, permisos) VALUES (:nombre, true, :permisos)")
                            .setParameter("nombre", nombre)
                            .setParameter("permisos", permisos)
                            .executeUpdate();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public Rol CrearRol(Rol rol) {
        validarRol(rol);
        if (rol.getVigente() == null) {
            rol.setVigente(true);
        }
        Long nextId = ((Number) entityManager.createNativeQuery(
                "SELECT COALESCE(MAX(id_rol), 0) + 1 FROM rol").getSingleResult()).longValue();
        entityManager.createNativeQuery(
                "INSERT INTO rol (id_rol, nombre, vigente, permisos) VALUES (:id, :nombre, :vigente, :permisos)")
                .setParameter("id", nextId)
                .setParameter("nombre", rol.getNombre().trim())
                .setParameter("vigente", rol.getVigente())
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
