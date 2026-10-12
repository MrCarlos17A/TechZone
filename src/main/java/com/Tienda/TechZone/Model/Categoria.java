package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @Column(name = "id_categoria", nullable = false)
    private Integer idCategoria;

    @Column(length = 20)
    private String nombre;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
