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
    @Column(name = "id_rol", nullable = false)
    private Integer idRol;

    @Column(length = 20)
    private String nombre;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
