package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Ubigeo;
import com.Tienda.TechZone.Repository.UbigeoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbigeoService {

    private final UbigeoRepository ubigeoRepository;

    public List<Ubigeo> ListarUbigeos() {
        return ubigeoRepository.findAll();
    }

    public Ubigeo CrearUbigeo(Ubigeo ubigeo) {
        if (ubigeo.getIdUbigeo() == null) {
            throw new RuntimeException("El id del ubigeo es obligatorio");
        }
        if (ubigeoRepository.existsById(ubigeo.getIdUbigeo())) {
            throw new RuntimeException("Ya existe un ubigeo con ese id");
        }
        validarUbigeo(ubigeo);
        return ubigeoRepository.save(ubigeo);
    }

    public Ubigeo ActualizarUbigeo(Long id, Ubigeo ubigeo) {
        Ubigeo existente = ubigeoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ubigeo no encontrado con id: " + id));
        validarUbigeo(ubigeo);
        existente.setDepartamento(ubigeo.getDepartamento().trim());
        existente.setProvincia(ubigeo.getProvincia().trim());
        existente.setDistrito(ubigeo.getDistrito().trim());
        if (ubigeo.getVigente() != null) {
            existente.setVigente(ubigeo.getVigente());
        }
        return ubigeoRepository.save(existente);
    }

    public void EliminarUbigeo(Long id) {
        Ubigeo ubigeo = ubigeoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ubigeo no encontrado con id: " + id));
        ubigeo.setVigente(false);
        ubigeoRepository.save(ubigeo);
    }

    public Ubigeo ObtenerUbigeoPorId(Long id) {
        return ubigeoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ubigeo no encontrado con id: " + id));
    }

    private void validarUbigeo(Ubigeo ubigeo) {
        if (ubigeo.getDepartamento() == null || ubigeo.getDepartamento().isBlank()) {
            throw new RuntimeException("El departamento es obligatorio");
        }
        if (ubigeo.getProvincia() == null || ubigeo.getProvincia().isBlank()) {
            throw new RuntimeException("La provincia es obligatoria");
        }
        if (ubigeo.getDistrito() == null || ubigeo.getDistrito().isBlank()) {
            throw new RuntimeException("El distrito es obligatorio");
        }
        if (ubigeo.getDepartamento().trim().length() > 50
                || ubigeo.getProvincia().trim().length() > 50
                || ubigeo.getDistrito().trim().length() > 50) {
            throw new RuntimeException("Departamento, provincia y distrito no pueden superar 50 caracteres");
        }
    }
}
