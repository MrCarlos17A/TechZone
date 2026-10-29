package com.tienda.backend_techzone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ayuda_rol")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AyudaRol {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private AyudaRolId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idAyuda")
    @JoinColumn(name = "id_ayuda")
    @ToString.Exclude
    private CentroAyuda ayuda;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRol")
    @JoinColumn(name = "id_rol")
    @ToString.Exclude
    private Rol rol;
}
