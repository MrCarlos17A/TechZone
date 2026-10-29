package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Persona;
import com.tienda.backend_techzone.Repository.PersonaRepository;
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
        validarDocumentoNoDuplicado(persona);
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
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
        persona.setVigente(false);
        personaRepository.save(persona);
    }

    public Persona ObtenerPersonaPorId(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
    }

    public Persona ObtenerPorNumeroDocumento(String numeroDocumento) {
        return personaRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con documento: " + numeroDocumento));
    }

    private void validarDocumentoNoDuplicado(Persona persona) {
        String doc = persona.getNumeroDocumento();
        if (doc == null || doc.isBlank()) {
            return;
        }
        Long idTipo = persona.getTipoDocumento() != null
                ? persona.getTipoDocumento().getIdTipoDocumento() : null;
        long existe = (idTipo != null)
                ? personaRepository.countByTipoDocumentoAndNumeroDocumento(idTipo, doc.trim())
                : personaRepository.countByNumeroDocumento(doc.trim());
        if (existe > 0) {
            throw new RuntimeException("Ya existe una persona con ese tipo y numero de documento");
        }
    }

    private void validarPersona(Persona persona) {
        if (persona.getNombres() == null || persona.getNombres().isBlank()) {
            throw new RuntimeException("Los nombres son obligatorios");
        }
        if (persona.getNombres().trim().length() > 100) {
            throw new RuntimeException("Los nombres no pueden superar 100 caracteres");
        }
        persona.setNombres(persona.getNombres().trim().toUpperCase());

        if (persona.getApellidoPaterno() == null || persona.getApellidoPaterno().isBlank()) {
            throw new RuntimeException("El apellido paterno es obligatorio");
        }
        if (persona.getApellidoPaterno().trim().length() > 50) {
            throw new RuntimeException("El apellido paterno no puede superar 50 caracteres");
        }
        persona.setApellidoPaterno(persona.getApellidoPaterno().trim().toUpperCase());

        if (persona.getApellidoMaterno() != null && !persona.getApellidoMaterno().isBlank()) {
            if (persona.getApellidoMaterno().trim().length() > 50) {
                throw new RuntimeException("El apellido materno no puede superar 50 caracteres");
            }
            persona.setApellidoMaterno(persona.getApellidoMaterno().trim().toUpperCase());
        }
        if (persona.getDireccion() != null && !persona.getDireccion().isBlank()) {
            persona.setDireccion(persona.getDireccion().trim().toUpperCase());
        }
        if (persona.getReferencia() != null && !persona.getReferencia().isBlank()) {
            persona.setReferencia(persona.getReferencia().trim().toUpperCase());
        }
        if (persona.getNumeroDocumento() != null && !persona.getNumeroDocumento().isBlank()) {
            String doc = persona.getNumeroDocumento().trim();
            if (!doc.matches("\\d{8,15}")) {
                throw new RuntimeException("El numero de documento debe contener solo digitos (8 a 15)");
            }
            persona.setNumeroDocumento(doc);
        }
        if (persona.getTelefono() != null && !persona.getTelefono().isBlank()) {
            String tel = persona.getTelefono().trim();
            if (!tel.matches("[0-9]{7,15}")) {
                throw new RuntimeException("El telefono debe contener solo digitos (7 a 15)");
            }
            persona.setTelefono(tel);
        }
        if (persona.getFechaNacimiento() != null
                && persona.getFechaNacimiento().isAfter(java.time.LocalDate.now())) {
            throw new RuntimeException("La fecha de nacimiento no puede ser futura");
        }
        if (persona.getNombres() != null) {
            persona.setNombres(persona.getNombres().trim());
        }
        if (persona.getApellidoPaterno() != null) {
            persona.setApellidoPaterno(persona.getApellidoPaterno().trim());
        }
        if (persona.getApellidoMaterno() != null) {
            persona.setApellidoMaterno(persona.getApellidoMaterno().trim());
        }
    }
}
