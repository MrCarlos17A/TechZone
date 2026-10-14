package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rol rol;

    @Column(length = 100, unique = true)
    private String correo;

    @Column(name = "contrasena", length = 60)
    private String password;

    @Column(name = "fecha_ultimo_acceso")
    private LocalDateTime fechaUltimoAcceso;

    @Column(name = "estado", length = 20)
    private String estado;
}
