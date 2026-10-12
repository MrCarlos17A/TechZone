package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado", nullable = false)
    private Long idEmpleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Persona persona;

    @Column(name = "fecha_contrato")
    private LocalDate fechaContrato;

    @Column(name = "salario", precision = 10, scale = 2)
    private BigDecimal salario;
}
