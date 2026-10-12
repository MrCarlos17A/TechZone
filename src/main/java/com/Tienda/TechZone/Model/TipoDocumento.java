package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoDocumento {

    @Id
    @Column(name = "id_tipo_documento", nullable = false)
    private Integer idTipoDocumento;

    @Column(length = 50)
    private String nombre;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
