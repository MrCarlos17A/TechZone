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
        if (rol.getVigente() == null) {
            rol.setVigente(true);
        }
        if (rol.getIdRol() == null) {
            rol.setIdRol(rolRepository.findAll().stream()
                    .map(Rol::getIdRol)
                    .max(Integer::compareTo)
                    .orElse(0) + 1);
        }
        return rolRepository.save(rol);
    }

    public Rol ActualizarRol(Integer id, Rol rol) {
        Rol existente = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + id));
        existente.setNombre(rol.getNombre().trim());
        if (rol.getVigente() != null) {
            existente.setVigente(rol.getVigente());
        }
        validarRol(existente);
        return rolRepository.save(existente);
    }

    public void EliminarRol(Integer id) {
        rolRepository.deleteById(id);
    }

    public Rol ObtenerRolPorId(Integer id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + id));
    }

    private void validarRol(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del rol es obligatorio");
        }
    }
}
