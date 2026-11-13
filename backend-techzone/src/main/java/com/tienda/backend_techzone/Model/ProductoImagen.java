package com.tienda.backend_techzone.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "producto_imagen")
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
    private Boolean esPrincipal = false;

    @Column(name = "orden")
    private Integer orden = 0;

    @Column(name = "etiqueta", length = 100)
    private String etiqueta = "Vista Principal";

    public ProductoImagen() {
    }

    public ProductoImagen(Long idProductoImagen, Producto producto, String urlImagen, Boolean esPrincipal, Integer orden, String etiqueta) {
        this.idProductoImagen = idProductoImagen;
        this.producto = producto;
        this.urlImagen = urlImagen;
        this.esPrincipal = esPrincipal != null ? esPrincipal : false;
        this.orden = orden != null ? orden : 0;
        this.etiqueta = etiqueta != null ? etiqueta : "Vista Principal";
    }

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

    public static ProductoImagenBuilder builder() {
        return new ProductoImagenBuilder();
    }

    public static class ProductoImagenBuilder {
        private Long idProductoImagen;
        private Producto producto;
        private String urlImagen;
        private Boolean esPrincipal = false;
        private Integer orden = 0;
        private String etiqueta = "Vista Principal";

        public ProductoImagenBuilder idProductoImagen(Long idProductoImagen) {
            this.idProductoImagen = idProductoImagen;
            return this;
        }

        public ProductoImagenBuilder producto(Producto producto) {
            this.producto = producto;
            return this;
        }

        public ProductoImagenBuilder urlImagen(String urlImagen) {
            this.urlImagen = urlImagen;
            return this;
        }

        public ProductoImagenBuilder esPrincipal(Boolean esPrincipal) {
            this.esPrincipal = esPrincipal;
            return this;
        }

        public ProductoImagenBuilder orden(Integer orden) {
            this.orden = orden;
            return this;
        }

        public ProductoImagenBuilder etiqueta(String etiqueta) {
            this.etiqueta = etiqueta;
            return this;
        }

        public ProductoImagen build() {
            return new ProductoImagen(idProductoImagen, producto, urlImagen, esPrincipal, orden, etiqueta);
        }
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
