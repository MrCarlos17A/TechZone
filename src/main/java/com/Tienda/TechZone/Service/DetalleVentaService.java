package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.DetalleVenta;
import com.Tienda.TechZone.Repository.DetalleVentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetalleVentaService {

    private final DetalleVentaRepository detalleVentaRepository;

    public List<DetalleVenta> ListarPorVenta(Integer idVenta) {
        return detalleVentaRepository.findAll().stream()
                .filter(dv -> dv.getVenta() != null && dv.getVenta().getIdVenta().equals(idVenta))
                .toList();
    }

    public DetalleVenta ObtenerDetallePorId(Integer id) {
        return detalleVentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado con id: " + id));
    }
}
