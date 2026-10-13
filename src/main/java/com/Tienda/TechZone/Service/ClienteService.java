package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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

    public Cliente CrearCliente(Cliente cliente) {
        validarCliente(cliente);
        return clienteRepository.save(cliente);
    }

    public Cliente ActualizarCliente(Long id, Cliente cliente) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
        validarCliente(cliente);
        existente.setPersona(cliente.getPersona());
        existente.setFechaUltimaCompra(cliente.getFechaUltimaCompra());
        existente.setTotalGastado(cliente.getTotalGastado());
        return clienteRepository.save(existente);
    }

    private void validarTotalGastado(Cliente cliente) {
        if (cliente.getTotalGastado() != null && cliente.getTotalGastado().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El total gastado no puede ser negativo");
        }
    }

    private void validarCliente(Cliente cliente) {
        if (cliente.getPersona() == null || cliente.getPersona().getIdPersona() == null) {
            throw new RuntimeException("Selecciona una persona para el cliente");
        }
        validarTotalGastado(cliente);
    }

    public void EliminarCliente(Long id) {
        clienteRepository.deleteById(id);
    }
}
