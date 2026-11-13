package com.tienda.backend_techzone.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "producto_imagen")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(name = "orden")
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "etiqueta", length = 100)
    @Builder.Default
    private String etiqueta = "Vista Principal";

    public Long getIdProductoImagen() {
        return idProductoImagen;
    }

    public void setIdProductoImagen(Long idProductoImagen) {
        this.idProductoImagen = idProductoImagen;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

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
    public BigDecimal getPrecioProducto() {
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
