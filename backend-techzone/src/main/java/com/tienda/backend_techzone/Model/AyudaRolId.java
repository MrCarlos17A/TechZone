package com.tienda.backend_techzone.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AyudaRolId implements Serializable {

    @Column(name = "id_ayuda")
    private Long idAyuda;

    @Column(name = "id_rol")
    private Long idRol;
}
