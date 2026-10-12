package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ubigeo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ubigeo {

    @Id
    @Column(name = "id_ubigeo", nullable = false)
    private Integer idUbigeo;

    @Column(length = 50)
    private String departamento;

    @Column(length = 50)
    private String provincia;

    @Column(length = 50)
    private String distrito;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
