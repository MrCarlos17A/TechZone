package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Marca;
import com.Tienda.TechZone.Repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public List<Marca> ListarMarcas() {
        return marcaRepository.findAll();
    }

    public Marca CrearMarca(Marca marca) {
        validarMarca(marca);
        if (marca.getVigente() == null) {
            marca.setVigente(true);
        }
        if (marca.getIdMarca() == null) {
            marca.setIdMarca(marcaRepository.findAll().stream()
                    .map(Marca::getIdMarca)
                    .max(Integer::compareTo)
                    .orElse(0) + 1);
        }
        return marcaRepository.save(marca);
    }

    public Marca ActualizarMarca(Integer id, Marca marca) {
        Marca existente = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + id));
        existente.setNombre(marca.getNombre().trim());
        if (marca.getVigente() != null) {
            existente.setVigente(marca.getVigente());
        }
        validarMarca(existente);
        return marcaRepository.save(existente);
    }

    public void EliminarMarca(Integer id) {
        marcaRepository.deleteById(id);
    }

    public Marca ObtenerMarcaPorId(Integer id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + id));
    }

    private void validarMarca(Marca marca) {
        if (marca.getNombre() == null || marca.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la marca es obligatorio");
        }
    }
}
