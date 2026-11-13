package com.tienda.backend_techzone.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_acceso")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acceso", nullable = false)
    @EqualsAndHashCode.Include
    private Long idAcceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @ToString.Exclude
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "agente", length = 255)
    private String agente;

    @Column(name = "exitoso", nullable = false)
    private Boolean exitoso;

    @Column(name = "motivo", length = 40)
    private String motivo;

    public RegistroAcceso(Usuario usuario, String correo, String ip, String agente, Boolean exitoso, String motivo) {
        this.usuario = usuario;
        this.correo = correo;
        this.ip = ip;
        this.agente = agente;
        this.exitoso = exitoso;
        this.motivo = motivo;
        this.fechaHora = LocalDateTime.now();
    }
}
