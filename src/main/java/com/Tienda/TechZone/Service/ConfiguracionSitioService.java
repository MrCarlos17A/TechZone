package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.ConfiguracionSitio;
import com.Tienda.TechZone.Repository.ConfiguracionSitioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracionSitioService {

    private static final Long ID_UNICO = 1L;
    private final ConfiguracionSitioRepository repository;

    @Transactional(readOnly = true)
    public ConfiguracionSitio ObtenerConfiguracion() {
        return repository.findById(ID_UNICO).orElseGet(() -> {
            ConfiguracionSitio defecto = ConfiguracionSitio.builder().idConfig(ID_UNICO).build();
            return repository.save(defecto);
        });
    }

    @Transactional
    public ConfiguracionSitio GuardarConfiguracion(ConfiguracionSitio config) {
        if (config.getNombreSitio() != null) {
            config.setNombreSitio(config.getNombreSitio().trim());
            if (config.getNombreSitio().length() > 80) {
                throw new RuntimeException("El nombre del sitio no puede superar 80 caracteres");
            }
        }
        if (config.getEmail() != null && !config.getEmail().isBlank()) {
            if (!config.getEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                throw new RuntimeException("El correo de contacto no es valido");
            }
            if (config.getEmail().length() > 100) {
                throw new RuntimeException("El correo no puede superar 100 caracteres");
            }
        }
        if (config.getTelefono() != null && config.getTelefono().length() > 20) {
            throw new RuntimeException("El telefono no puede superar 20 caracteres");
        }
        if (config.getWhatsapp() != null && config.getWhatsapp().length() > 20) {
            throw new RuntimeException("El WhatsApp no puede superar 20 caracteres");
        }
        if (config.getTagline() != null && config.getTagline().length() > 160) {
            throw new RuntimeException("El tagline no puede superar 160 caracteres");
        }
        if (config.getCopyright() != null && config.getCopyright().length() > 120) {
            throw new RuntimeException("El copyright no puede superar 120 caracteres");
        }
        config.setIdConfig(ID_UNICO);
        return repository.save(config);
    }
}
