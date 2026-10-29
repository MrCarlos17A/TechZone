package com.tienda.backend_techzone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medio_pago")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedioPago {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medio_pago")
    private Long idMedioPago;

    @Column(length = 50)
    private String nombre;

    @Column(name = "instrucciones", length = 300)
    private String instrucciones;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
