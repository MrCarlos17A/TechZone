package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Persona persona;

    @Column(name = "fecha_ultima_compra")
    private LocalDateTime fechaUltimaCompra;

    @Column(name = "total_gastado", precision = 10, scale = 2)
    private BigDecimal totalGastado;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
