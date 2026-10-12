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
        return ubigeoRepository.save(ubigeo);
    }

    public Ubigeo ActualizarUbigeo(Integer id, Ubigeo ubigeo) {
        Ubigeo existente = ubigeoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ubigeo no encontrado con id: " + id));
        existente.setDepartamento(ubigeo.getDepartamento());
        existente.setProvincia(ubigeo.getProvincia());
        existente.setDistrito(ubigeo.getDistrito());
        if (ubigeo.getVigente() != null) {
            existente.setVigente(ubigeo.getVigente());
        }
        return ubigeoRepository.save(existente);
    }

    public void EliminarUbigeo(Integer id) {
        ubigeoRepository.deleteById(id);
    }

    public Ubigeo ObtenerUbigeoPorId(Integer id) {
        return ubigeoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ubigeo no encontrado con id: " + id));
    }
}
