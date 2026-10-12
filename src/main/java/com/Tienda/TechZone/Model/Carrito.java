package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "carrito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito", nullable = false)
    private Long idCarrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cliente cliente;

    @Column(name = "fecha_carrito")
    private LocalDateTime fechaCarrito;
}
