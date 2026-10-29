package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.DetalleVenta;
import com.tienda.backend_techzone.Repository.DetalleVentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetalleVentaService {

    private final DetalleVentaRepository detalleVentaRepository;

    public DetalleVentaService(DetalleVentaRepository detalleVentaRepository) {
        this.detalleVentaRepository = detalleVentaRepository;
    }

    public List<DetalleVenta> ListarPorVenta(Long idVenta) {
        if (idVenta == null) {
            return detalleVentaRepository.findAllConRelaciones();
        }
        return detalleVentaRepository.findAllConRelaciones().stream()
                .filter(dv -> dv.getVenta() != null && idVenta.equals(dv.getVenta().getIdVenta()))
                .toList();
    }

    public List<DetalleVenta> listarPorVenta(Long idVenta) {
        return ListarPorVenta(idVenta);
    }

    public DetalleVenta ObtenerDetallePorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del detalle es obligatorio");
        }
        return detalleVentaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado con id: " + id));
    }

    public DetalleVenta obtenerDetallePorId(Long id) {
        return ObtenerDetallePorId(id);
    }
}
