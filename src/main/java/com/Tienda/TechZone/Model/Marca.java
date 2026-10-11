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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Long idMarca;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 255)
    private String imagen;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estadoMarca = true;
}
