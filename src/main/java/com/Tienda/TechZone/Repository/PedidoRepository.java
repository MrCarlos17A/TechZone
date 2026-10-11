package com.Tienda.TechZone.Repository;

import com.Tienda.TechZone.Model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioIdUsuario(Long idUsuario);
}
