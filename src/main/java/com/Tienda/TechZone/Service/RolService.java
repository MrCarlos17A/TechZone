package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Rol;
import com.Tienda.TechZone.Repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public List<Rol> ListarRoles() {
        return rolRepository.findAll();
    }

    public Rol CrearRol(Rol rol) {
        validarRol(rol);
        if (rol.getEstadoRol() == null) {
            rol.setEstadoRol(true);
        }
        return rolRepository.save(rol);
    }

    public Rol ActualizarRol(Long id, Rol rol) {
        Rol existente = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + id));
        existente.setNombre(rol.getNombre().trim());
        existente.setDescripcion(rol.getDescripcion());
        if (rol.getEstadoRol() != null) {
            existente.setEstadoRol(rol.getEstadoRol());
        }
        validarRol(existente);
        return rolRepository.save(existente);
    }

    public void EliminarRol(Long id) {
        rolRepository.deleteById(id);
    }

    public Rol ObtenerRolPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + id));
    }

    private void validarRol(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del rol es obligatorio");
        }
    }
}
