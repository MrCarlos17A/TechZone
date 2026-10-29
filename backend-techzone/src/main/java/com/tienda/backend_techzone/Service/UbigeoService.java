package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Ubigeo;
import com.tienda.backend_techzone.Repository.UbigeoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UbigeoService {

    private final UbigeoRepository ubigeoRepository;

    public UbigeoService(UbigeoRepository ubigeoRepository) {
        this.ubigeoRepository = ubigeoRepository;
    }

    public List<Ubigeo> ListarUbigeos() {
        return ubigeoRepository.findAll();
    }

    public List<Ubigeo> listarUbigeos() {
        return ListarUbigeos();
    }

    public Ubigeo CrearUbigeo(Ubigeo ubigeo) {
        if (ubigeo.getIdUbigeo() == null) {
            throw new IllegalArgumentException("El id del ubigeo es obligatorio");
        }
        if (ubigeoRepository.existsById(ubigeo.getIdUbigeo())) {
            throw new IllegalArgumentException("Ya existe un ubigeo con ese id");
        }
        validarUbigeo(ubigeo);
        return ubigeoRepository.save(ubigeo);
    }

    public Ubigeo crearUbigeo(Ubigeo ubigeo) {
        return CrearUbigeo(ubigeo);
    }

    public Ubigeo ActualizarUbigeo(Long id, Ubigeo ubigeo) {
        Ubigeo existente = ubigeoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ubigeo no encontrado con id: " + id));
        validarUbigeo(ubigeo);
        existente.setDepartamento(ubigeo.getDepartamento().trim());
        existente.setProvincia(ubigeo.getProvincia().trim());
        existente.setDistrito(ubigeo.getDistrito().trim());
        if (ubigeo.getVigente() != null) {
            existente.setVigente(ubigeo.getVigente());
        }
        return ubigeoRepository.save(existente);
    }

    public Ubigeo actualizarUbigeo(Long id, Ubigeo ubigeo) {
        return ActualizarUbigeo(id, ubigeo);
    }

    public void EliminarUbigeo(Long id) {
        Ubigeo ubigeo = ubigeoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ubigeo no encontrado con id: " + id));
        ubigeo.setVigente(false);
        ubigeoRepository.save(ubigeo);
    }

    public void eliminarUbigeo(Long id) {
        EliminarUbigeo(id);
    }

    public Ubigeo ObtenerUbigeoPorId(Long id) {
        return ubigeoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ubigeo no encontrado con id: " + id));
    }

    public Ubigeo obtenerUbigeoPorId(Long id) {
        return ObtenerUbigeoPorId(id);
    }

    private void validarUbigeo(Ubigeo ubigeo) {
        if (ubigeo.getDepartamento() == null || ubigeo.getDepartamento().isBlank()) {
            throw new IllegalArgumentException("El departamento es obligatorio");
        }
        if (ubigeo.getProvincia() == null || ubigeo.getProvincia().isBlank()) {
            throw new IllegalArgumentException("La provincia es obligatoria");
        }
        if (ubigeo.getDistrito() == null || ubigeo.getDistrito().isBlank()) {
            throw new IllegalArgumentException("El distrito es obligatorio");
        }
        if (ubigeo.getDepartamento().trim().length() > 50
                || ubigeo.getProvincia().trim().length() > 50
                || ubigeo.getDistrito().trim().length() > 50) {
            throw new IllegalArgumentException("Departamento, provincia y distrito no pueden superar 50 caracteres");
        }
    }
}
