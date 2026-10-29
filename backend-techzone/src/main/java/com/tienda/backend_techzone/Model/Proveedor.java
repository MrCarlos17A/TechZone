package com.tienda.backend_techzone.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor", nullable = false)
    private Long idProveedor;

    @Column(length = 150)
    private String razonSocial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento")
    @ToString.Exclude
    private TipoDocumento tipoDocumento;

    @Column(length = 20)
    private String numeroDocumento;

    @Column(length = 20)
    private String telefonoContacto;

    @Column(length = 100)
    private String correo;

    @Column(length = 150)
    private String nombreEncargado;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;
}
