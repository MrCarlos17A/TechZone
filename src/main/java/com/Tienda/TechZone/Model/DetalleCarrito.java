package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalle_carrito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_carrito", nullable = false)
    private Long idDetalleCarrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrito")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @Column(name = "cantidad")
    private Integer cantidad;
}
