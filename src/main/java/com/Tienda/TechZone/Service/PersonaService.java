package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Persona;
import com.Tienda.TechZone.Repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;

    public List<Persona> ListarPersonas() {
        return personaRepository.findAll();
    }

    public Persona CrearPersona(Persona persona) {
        validarPersona(persona);
        if (persona.getVigente() == null) {
            persona.setVigente(true);
        }
        persona.setFechaRegistro(LocalDateTime.now());
        return personaRepository.save(persona);
    }

    public Persona ActualizarPersona(Long id, Persona persona) {
        Persona existente = personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
        existente.setNombres(persona.getNombres());
        existente.setApellidoPaterno(persona.getApellidoPaterno());
        existente.setApellidoMaterno(persona.getApellidoMaterno());
        existente.setTipoDocumento(persona.getTipoDocumento());
        existente.setNumeroDocumento(persona.getNumeroDocumento());
        existente.setTelefono(persona.getTelefono());
        existente.setDireccion(persona.getDireccion());
        existente.setReferencia(persona.getReferencia());
        existente.setUbigeo(persona.getUbigeo());
        existente.setFechaNacimiento(persona.getFechaNacimiento());
        if (persona.getVigente() != null) {
            existente.setVigente(persona.getVigente());
        }
        validarPersona(existente);
        return personaRepository.save(existente);
    }

    public void EliminarPersona(Long id) {
        personaRepository.deleteById(id);
    }

    public Persona ObtenerPersonaPorId(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
    }

    public Persona ObtenerPorNumeroDocumento(String numeroDocumento) {
        return personaRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con documento: " + numeroDocumento));
    }

    private void validarPersona(Persona persona) {
        if (persona.getNombres() == null || persona.getNombres().isBlank()) {
            throw new RuntimeException("Los nombres son obligatorios");
        }
    }
}
