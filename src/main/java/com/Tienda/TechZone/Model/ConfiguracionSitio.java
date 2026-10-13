package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_sitio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSitio {

    @Id
    @Column(name = "id_config", nullable = false)
    private Long idConfig;

    @Column(name = "nombre_sitio", length = 80)
    @Builder.Default
    private String nombreSitio = "TechZone";

    @Column(name = "logo_url", length = 255)
    @Builder.Default
    private String logoUrl = "/Assets/IMG/Logo/TechZone.png";

    @Column(length = 160)
    @Builder.Default
    private String tagline = "Tu tienda de tecnologia de confianza.";

    @Column(length = 100)
    @Builder.Default
    private String email = "techzone@outlook.com";

    @Column(length = 20)
    @Builder.Default
    private String telefono = "+51 967 662 103";

    @Column(length = 60)
    @Builder.Default
    private String horario = "Lun - Dom, 09:00 a.m. - 21:00 p.m.";

    @Column(length = 20)
    @Builder.Default
    private String whatsapp = "+51 967 662 103";

    @Column(name = "whatsapp_texto", length = 60)
    @Builder.Default
    private String whatsappTexto = "Atencion 24/7";

    @Column(name = "facebook_url", length = 255)
    @Builder.Default
    private String facebookUrl = "https://www.facebook.com";

    @Column(name = "instagram_url", length = 255)
    @Builder.Default
    private String instagramUrl = "https://www.instagram.com";

    @Column(length = 120)
    @Builder.Default
    private String copyright = "TECHZONE. Todos los derechos reservados.";

    @Column(name = "libro_reclamaciones_url", length = 255)
    @Builder.Default
    private String libroReclamacionesUrl = "#";
}
