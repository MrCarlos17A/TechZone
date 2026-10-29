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
