package com.Tienda.TechZone.Model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Marca marca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Categoria categoria;

    @Column(length = 50)
    private String codigoSku;

    @Column(length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "JSON")
    private String caracteristicas;

    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "vigente")
    @Builder.Default
    private Boolean vigente = true;

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<ProductoImagen> imagenes = new ArrayList<>();

    @Transient
    public String getImagenPrincipal() {
        if (imagenes == null || imagenes.isEmpty()) {
            return null;
        }
        return imagenes.stream()
                .filter(ProductoImagen::getEsPrincipal)
                .findFirst()
                .orElse(imagenes.get(0))
                .getUrlImagen();
    }
}
