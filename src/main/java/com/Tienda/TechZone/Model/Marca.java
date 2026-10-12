package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marca")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marca {

    @Id
    @Column(name = "id_marca", nullable = false)
    private Integer idMarca;

    @Column(length = 20)
    private String nombre;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
