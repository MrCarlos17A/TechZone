package com.tienda.backend_techzone.Model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "producto_proveedor")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductoProveedor {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private Id id;

    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "tiempo_pedido_dias")
    private Integer tiempoPedidoDias;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto")
    @JoinColumn(name = "id_producto")
    @ToString.Exclude
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProveedor")
    @JoinColumn(name = "id_proveedor")
    @ToString.Exclude
    private Proveedor proveedor;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Integer getTiempoPedidoDias() {
        return tiempoPedidoDias;
    }

    public void setTiempoPedidoDias(Integer tiempoPedidoDias) {
        this.tiempoPedidoDias = tiempoPedidoDias;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {

        @Column(name = "id_producto")
        private Long idProducto;

        @Column(name = "id_proveedor")
        private Long idProveedor;

        public Long getIdProducto() {
            return idProducto;
        }

        public void setIdProducto(Long idProducto) {
            this.idProducto = idProducto;
        }

        public Long getIdProveedor() {
            return idProveedor;
        }

        public void setIdProveedor(Long idProveedor) {
            this.idProveedor = idProveedor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id)) return false;
            Id id = (Id) o;
            return Objects.equals(idProducto, id.idProducto) &&
                   Objects.equals(idProveedor, id.idProveedor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idProducto, idProveedor);
        }
    }
}
