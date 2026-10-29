package com.tienda.backend_techzone.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_imagen")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoImagen {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_imagen", nullable = false)
    private Long idProductoImagen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto")
    @ToString.Exclude
    @JsonIgnoreProperties({"imagenes", "hibernateLazyInitializer", "handler"})
    private Producto producto;

    @Column(name = "url_imagen", length = 500)
    private String urlImagen;

    @Column(name = "es_principal")
    @Builder.Default
    private Boolean esPrincipal = false;

    @JsonProperty("idProducto")
    public Long getIdProducto() {
        return producto != null ? producto.getIdProducto() : null;
    }

    @JsonProperty("nombreProducto")
    public String getNombreProducto() {
        return producto != null ? producto.getNombre() : null;
    }

    @JsonProperty("skuProducto")
    public String getSkuProducto() {
        return producto != null ? producto.getCodigoSku() : null;
    }

    @JsonProperty("precioProducto")
    public java.math.BigDecimal getPrecioProducto() {
        return producto != null ? producto.getPrecioVenta() : null;
    }

    @JsonProperty("categoriaProducto")
    public String getCategoriaProducto() {
        return (producto != null && producto.getCategoria() != null) ? producto.getCategoria().getNombre() : null;
    }

    @JsonProperty("marcaProducto")
    public String getMarcaProducto() {
        return (producto != null && producto.getMarca() != null) ? producto.getMarca().getNombre() : null;
    }
}
