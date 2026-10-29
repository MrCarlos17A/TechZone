package com.tienda.backend_techzone.Model;

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
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acceso", nullable = false)
    @EqualsAndHashCode.Include
    private Long idAcceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @ToString.Exclude
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

    public Long getIdAcceso() { return idAcceso; }
    public void setIdAcceso(Long idAcceso) { this.idAcceso = idAcceso; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getAgente() { return agente; }
    public void setAgente(String agente) { this.agente = agente; }

    public Boolean getExitoso() { return exitoso; }
    public void setExitoso(Boolean exitoso) { this.exitoso = exitoso; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
