package com.tienda.backend_techzone.Model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVenta {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_venta", nullable = false)
    private Long idDetalleVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta")
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"detalles", "pagos", "hibernateLazyInitializer", "handler"})
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"imagenes", "hibernateLazyInitializer", "handler"})
    private Producto producto;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "sub_total_venta", precision = 10, scale = 2)
    private BigDecimal subTotalVenta;

    // Getters y Setters explícitos para compatibilidad con cualquier IDE sin plugin Lombok
    public Long getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(Long idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubTotalVenta() { return subTotalVenta; }
    public void setSubTotalVenta(BigDecimal subTotalVenta) { this.subTotalVenta = subTotalVenta; }
}
