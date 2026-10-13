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
                    .max(Long::compareTo)
                    .orElse(0L) + 1);
        }
        return marcaRepository.save(marca);
    }

    public Marca ActualizarMarca(Long id, Marca marca) {
        Marca existente = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + id));
        validarMarca(marca);
        existente.setNombre(marca.getNombre().trim());
        if (marca.getVigente() != null) {
            existente.setVigente(marca.getVigente());
        }
        return marcaRepository.save(existente);
    }

    public void EliminarMarca(Long id) {
        marcaRepository.deleteById(id);
    }

    public Marca ObtenerMarcaPorId(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + id));
    }

    private void validarMarca(Marca marca) {
        if (marca.getNombre() == null || marca.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la marca es obligatorio");
        }
        if (marca.getNombre().trim().length() > 20) {
            throw new RuntimeException("El nombre de la marca no puede superar 20 caracteres");
        }
    }
}
