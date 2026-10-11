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
        return marcaRepository.save(marca);
    }

    public Marca ActualizarMarca(Long id, Marca marca) {
        Marca existente = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + id));
        existente.setNombre(marca.getNombre().trim());
        existente.setImagen(marca.getImagen());
        existente.setEstadoMarca(marca.getEstadoMarca());
        validarMarca(existente);
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
    }
}
