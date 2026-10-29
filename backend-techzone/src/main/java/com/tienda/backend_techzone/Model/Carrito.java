package com.tienda.backend_techzone.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "carrito")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Carrito {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito", nullable = false)
    private Long idCarrito;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente")
    @ToString.Exclude
    private Cliente cliente;

    @Column(name = "fecha_carrito")
    private LocalDateTime fechaCarrito;

    // Getters y Setters explícitos para compatibilidad con cualquier IDE sin plugin Lombok
    public Long getIdCarrito() { return idCarrito; }
    public void setIdCarrito(Long idCarrito) { this.idCarrito = idCarrito; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDateTime getFechaCarrito() { return fechaCarrito; }
    public void setFechaCarrito(LocalDateTime fechaCarrito) { this.fechaCarrito = fechaCarrito; }
}
