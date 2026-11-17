package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.ConfiguracionSitio;
import com.tienda.backend_techzone.Repository.ConfiguracionSitioRepository;
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
        ConfiguracionSitio cfg = repository.findById(ID_UNICO).orElseGet(() -> {
            ConfiguracionSitio defecto = ConfiguracionSitio.builder().idConfig(ID_UNICO).build();
            return repository.save(defecto);
        });
        completarValoresPorDefecto(cfg);
        return cfg;
    }

    private void completarValoresPorDefecto(ConfiguracionSitio cfg) {
        if (cfg.getNombreSitio() == null || cfg.getNombreSitio().isBlank()) {
            cfg.setNombreSitio("TechZone Gaming Store");
        }
        if (cfg.getRazonSocial() == null || cfg.getRazonSocial().isBlank()) {
            cfg.setRazonSocial("TECHZONE GAMING STORE S.A.C.");
        }
        if (cfg.getRuc() == null || cfg.getRuc().isBlank()) {
            cfg.setRuc("20608945123");
        }
        if (cfg.getDireccionFiscal() == null || cfg.getDireccionFiscal().isBlank()) {
            cfg.setDireccionFiscal("Av. Javier Prado Este 4200, Santiago de Surco - Lima");
        }
        if (cfg.getTelefono() == null || cfg.getTelefono().isBlank()) {
            cfg.setTelefono("967662103");
        }
        if (cfg.getEmail() == null || cfg.getEmail().isBlank()) {
            cfg.setEmail("ventas@techzone.com");
        }
        if (cfg.getTagline() == null || cfg.getTagline().isBlank()) {
            cfg.setTagline("Componentes Gamer y Tecnología de Última Generación");
        }
        if (cfg.getHorario() == null || cfg.getHorario().isBlank()) {
            cfg.setHorario("Lun - Dom: 09:00 a.m. - 21:00 p.m.");
        }
        if (cfg.getCopyright() == null || cfg.getCopyright().isBlank()) {
            cfg.setCopyright("© 2026 TechZone Gaming Store S.A.C. Todos los derechos reservados.");
        }
        if (cfg.getWeb() == null || cfg.getWeb().isBlank()) {
            cfg.setWeb("www.techzone.com");
        }
        if (cfg.getLeyendaFiscal() == null || cfg.getLeyendaFiscal().isBlank()) {
            cfg.setLeyendaFiscal("Autorizado mediante resolución SUNAT N° 034-005");
        }
    }

    public ConfiguracionSitio obtenerConfiguracion() {
        return ObtenerConfiguracion();
    }

    public ConfiguracionSitio guardarConfiguracion(ConfiguracionSitio config) {
        return GuardarConfiguracion(config);
    }

    @Transactional
    public ConfiguracionSitio GuardarConfiguracion(ConfiguracionSitio config) {
        if (config.getNombreSitio() != null) {
            config.setNombreSitio(config.getNombreSitio().trim());
            if (config.getNombreSitio().length() < 3) {
                throw new RuntimeException("El nombre del sitio debe tener al menos 3 caracteres");
            }
            if (config.getNombreSitio().length() > 80) {
                throw new RuntimeException("El nombre del sitio no puede superar 80 caracteres");
            }
        }
        if (config.getEmail() != null && !config.getEmail().isBlank()) {
            config.setEmail(config.getEmail().trim().toLowerCase());
            if (!config.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                throw new RuntimeException("Ingresa un formato de correo electrónico válido (ej. usuario@dominio.com)");
            }
            if (config.getEmail().length() > 100) {
                throw new RuntimeException("El correo no puede superar 100 caracteres");
            }
        }
        if (config.getTelefono() != null && !config.getTelefono().isBlank()) {
            String telLimpio = config.getTelefono().replaceAll("[^0-9]", "");
            if (telLimpio.startsWith("51") && telLimpio.length() == 11) {
                telLimpio = telLimpio.substring(2);
            }
            if (!telLimpio.matches("^9\\d{8}$")) {
                throw new RuntimeException("El teléfono debe tener 9 dígitos y empezar con 9 (ej. 987654321)");
            }
            config.setTelefono(telLimpio);
        }
        if (config.getRuc() != null && !config.getRuc().isBlank()) {
            String rucLimpio = config.getRuc().replaceAll("[^0-9]", "");
            if (!rucLimpio.matches("^(10|20)\\d{9}$")) {
                throw new RuntimeException("El RUC debe tener 11 dígitos y empezar con 10 o 20");
            }
            config.setRuc(rucLimpio);
        }
        if (config.getRazonSocial() != null && !config.getRazonSocial().isBlank()) {
            config.setRazonSocial(config.getRazonSocial().trim());
            if (config.getRazonSocial().length() < 3) {
                throw new RuntimeException("La razón social oficial debe tener al menos 3 caracteres");
            }
        }
        if (config.getDireccionFiscal() != null && !config.getDireccionFiscal().isBlank()) {
            config.setDireccionFiscal(config.getDireccionFiscal().trim());
            if (config.getDireccionFiscal().length() < 10) {
                throw new RuntimeException("La dirección fiscal debe tener al menos 10 caracteres");
            }
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
        if (config.getMostrarTiktok() == null) {
            config.setMostrarTiktok(true);
        }
        if (config.getMostrarFacebook() == null) {
            config.setMostrarFacebook(true);
        }
        if (config.getMostrarInstagram() == null) {
            config.setMostrarInstagram(true);
        }
        if (config.getMostrarYoutube() == null) {
            config.setMostrarYoutube(true);
        }
        config.setIdConfig(ID_UNICO);
        return repository.save(config);
    }
}
