package com.tienda.backend_techzone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_sitio")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSitio {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id_config", nullable = false)
    private Long idConfig;

    @Column(name = "nombre_sitio", length = 80)
    @Builder.Default
    private String nombreSitio = "TechZone";

    @Column(name = "logo_url", length = 500)
    @Builder.Default
    private String logoUrl = "https://polvfrjaecmugwyzyuzm.supabase.co/storage/v1/object/public/imagenes/configurtacion_sitio/TechZone.png";

    @Column(length = 160)
    @Builder.Default
    private String tagline = "Tu tienda de tecnologia de confianza.";

    @Column(length = 100)
    @Builder.Default
    private String email = "techzone@gmail.com";

    @Column(length = 20)
    @Builder.Default
    private String telefono = "+51 967 662 103";

    @Column(length = 100)
    @Builder.Default
    private String horario = "Lun - Dom, 09:00 a.m. - 21:00 p.m.";

    @Column(length = 20)
    @Builder.Default
    private String whatsapp = "+51 967 662 103";

    @Column(name = "whatsapp_texto", length = 255)
    @Builder.Default
    private String whatsappTexto = "Atencion 24/7";

    @Column(name = "mostrar_tiktok")
    @Builder.Default
    private Boolean mostrarTiktok = true;

    @Column(name = "tiktok_url", length = 500)
    @Builder.Default
    private String tiktokUrl = "https://www.tiktok.com";

    @Column(name = "mostrar_facebook")
    @Builder.Default
    private Boolean mostrarFacebook = true;

    @Column(name = "facebook_url", length = 500)
    @Builder.Default
    private String facebookUrl = "https://www.facebook.com";

    @Column(name = "mostrar_instagram")
    @Builder.Default
    private Boolean mostrarInstagram = true;

    @Column(name = "instagram_url", length = 500)
    @Builder.Default
    private String instagramUrl = "https://www.instagram.com";

    @Column(name = "mostrar_youtube")
    @Builder.Default
    private Boolean mostrarYoutube = true;

    @Column(name = "youtube_url", length = 500)
    @Builder.Default
    private String youtubeUrl = "https://www.youtube.com";

    @Column(length = 120)
    @Builder.Default
    private String copyright = "TECHZONE. Todos los derechos reservados.";

    @Column(name = "libro_reclamaciones_img", length = 500)
    @Builder.Default
    private String libroReclamacionesImg = "https://polvfrjaecmugwyzyuzm.supabase.co/storage/v1/object/public/imagenes/configurtacion_sitio/Libro_de_Reclamaciones.png";

    @Column(name = "libro_reclamaciones_url", length = 500)
    @Builder.Default
    private String libroReclamacionesUrl = "/";
}
