package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medio_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedioPago {

    @Id
    @Column(name = "id_medio_pago", nullable = false)
    private Integer idMedioPago;

    @Column(length = 20)
    private String nombre;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
