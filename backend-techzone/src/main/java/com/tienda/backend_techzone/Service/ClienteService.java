package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Cliente;
import com.tienda.backend_techzone.Repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lectura del módulo de clientes. El alta/edición/baja NO se expone:
 * la ficha la crea el sistema en el checkout y el estado lo mueven
 * VentaService / CompraController.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> ListarClientes() {
        return clienteRepository.findAllConPersona();
    }

    public List<Cliente> listarClientes() {
        return ListarClientes();
    }

    public Cliente ObtenerClientePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
    }

    public Cliente obtenerClientePorId(Long id) {
        return ObtenerClientePorId(id);
    }

    public Cliente actualizarCliente(Long id, Cliente cliente) {
        Cliente existente = ObtenerClientePorId(id);
        if (cliente.getTotalGastado() != null) {
            existente.setTotalGastado(cliente.getTotalGastado());
        }
        if (cliente.getFechaUltimaCompra() != null) {
            existente.setFechaUltimaCompra(cliente.getFechaUltimaCompra());
        }
        return clienteRepository.save(existente);
    }
}
