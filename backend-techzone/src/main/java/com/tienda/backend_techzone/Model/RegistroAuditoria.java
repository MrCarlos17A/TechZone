package com.tienda.backend_techzone.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_auditoria")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria", nullable = false)
    @EqualsAndHashCode.Include
    private Long idAuditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @ToString.Exclude
    private Usuario usuario;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "modulo", length = 50, nullable = false)
    private String modulo;

    @Column(name = "accion", length = 30, nullable = false)
    private String accion;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "datos_previos", columnDefinition = "TEXT")
    private String datosPrevios;

    @Column(name = "datos_nuevos", columnDefinition = "TEXT")
    private String datosNuevos;

    @Column(name = "ip", length = 45)
    @Builder.Default
    private String ip = "127.0.0.1";

    public String getIp() {
        return (ip != null && !ip.isBlank()) ? ip : "127.0.0.1";
    }
}
