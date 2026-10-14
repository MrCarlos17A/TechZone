package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Repository.ClienteRepository;
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
        return clienteRepository.findAll();
    }

    public Cliente ObtenerClientePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
    }
}
