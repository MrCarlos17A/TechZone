package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Carrito;
import com.tienda.backend_techzone.Model.Cliente;
import com.tienda.backend_techzone.Repository.CarritoRepository;
import com.tienda.backend_techzone.Repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ClienteRepository clienteRepository;

    public List<Carrito> ListarCarritos() {
        return carritoRepository.findAll();
    }

    public List<Carrito> ListarPorCliente(Long idCliente) {
        return carritoRepository.findAll().stream()
                .filter(c -> c.getCliente() != null && c.getCliente().getIdCliente().equals(idCliente))
                .toList();
    }

    public Carrito CrearCarrito(Carrito carrito) {
        if (carrito.getCliente() == null || carrito.getCliente().getIdCliente() == null) {
            throw new RuntimeException("El cliente es obligatorio");
        }
        Cliente cliente = clienteRepository.findById(carrito.getCliente().getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        carrito.setCliente(cliente);
        carrito.setFechaCarrito(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    public void EliminarCarrito(Long id) {
        carritoRepository.deleteById(id);
    }

    public Carrito ObtenerCarritoPorId(Long id) {
        return carritoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con id: " + id));
    }
}
