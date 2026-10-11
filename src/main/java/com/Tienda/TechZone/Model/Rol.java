package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 150)
    private String descripcion;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estadoRol = true;
}
