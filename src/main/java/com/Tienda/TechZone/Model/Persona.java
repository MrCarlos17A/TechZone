package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "persona")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(length = 100)
    private String nombres;

    @Column(length = 50)
    private String apellidoPaterno;

    @Column(length = 50)
    private String apellidoMaterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TipoDocumento tipoDocumento;

    @Column(length = 20)
    private String numeroDocumento;

    @Column(length = 15)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(length = 255)
    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubigeo")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Ubigeo ubigeo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
