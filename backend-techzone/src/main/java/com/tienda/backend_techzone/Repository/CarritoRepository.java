package com.tienda.backend_techzone.Repository;

import com.tienda.backend_techzone.Model.Carrito;
import com.tienda.backend_techzone.Model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByCliente(Cliente cliente);
    Optional<Carrito> findFirstByCliente(Cliente cliente);

    @Query("SELECT c FROM Carrito c WHERE c.cliente.idCliente = :idCliente ORDER BY c.idCarrito DESC")
    Optional<Carrito> findFirstByClienteIdCliente(@Param("idCliente") Long idCliente);

    @Query("SELECT c FROM Carrito c WHERE c.cliente.idCliente = :idCliente")
    Optional<Carrito> findByClienteIdCliente(@Param("idCliente") Long idCliente);

    @Query("SELECT c FROM Carrito c LEFT JOIN FETCH c.cliente cl LEFT JOIN FETCH cl.persona p ORDER BY c.idCarrito DESC")
    List<Carrito> findAllConCliente();
}
