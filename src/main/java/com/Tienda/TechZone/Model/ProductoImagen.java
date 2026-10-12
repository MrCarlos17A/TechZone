package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_imagen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_imagen", nullable = false)
    private Long idProductoImagen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @Column(name = "url_imagen", length = 255)
    private String urlImagen;

    @Column(name = "es_principal")
    @Builder.Default
    private Boolean esPrincipal = false;
}
