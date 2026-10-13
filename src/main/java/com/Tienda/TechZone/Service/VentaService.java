package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Model.DetalleVenta;
import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Repository.ClienteRepository;
import com.Tienda.TechZone.Repository.DetalleVentaRepository;
import com.Tienda.TechZone.Repository.ProductoRepository;
import com.Tienda.TechZone.Repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public List<Venta> ListarVentas() {
        return ventaRepository.findAll();
    }

    public List<Venta> ListarPorCliente(Long idCliente) {
        return ventaRepository.findByClienteIdCliente(idCliente);
    }

    public Venta ObtenerVentaPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con id: " + id));
    }

    @Transactional
    public Venta RegistrarVenta(Long idCliente, List<DetalleVenta> detalles, String direccionEnvio) {
        if (detalles == null || detalles.isEmpty()) {
            throw new RuntimeException("La venta debe tener al menos un producto");
        }

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleVenta dv : detalles) {
            if (dv.getCantidad() == null || dv.getCantidad() <= 0) {
                throw new RuntimeException("Cantidad invalida en el detalle de venta");
            }
            if (dv.getProducto() == null || dv.getProducto().getIdProducto() == null) {
                throw new RuntimeException("Producto no valido en el detalle de venta");
            }
            Producto producto = productoRepository.findById(dv.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            if (producto.getStock() == null || producto.getStock() < dv.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
            dv.setProducto(producto);
            dv.setPrecioUnitario(producto.getPrecioVenta());
            dv.setSubTotalVenta(producto.getPrecioVenta().multiply(BigDecimal.valueOf(dv.getCantidad())));
            subtotal = subtotal.add(dv.getSubTotalVenta());
        }

        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18"));
        BigDecimal total = subtotal.add(igv);

        Venta venta = Venta.builder()
                .cliente(cliente)
                .estado("PENDIENTE")
                .fechaVenta(LocalDateTime.now())
                .subtotal(subtotal)
                .igv(igv)
                .total(total)
                .direccionEnvio(direccionEnvio)
                .build();
        venta = ventaRepository.save(venta);

        for (DetalleVenta dv : detalles) {
            dv.setVenta(venta);
            detalleVentaRepository.save(dv);
            Producto producto = dv.getProducto();
            producto.setStock(producto.getStock() - dv.getCantidad());
            productoRepository.save(producto);
        }

        return venta;
    }

    public Venta CambiarEstado(Long idVenta, String estado) {
        Venta venta = ObtenerVentaPorId(idVenta);
        venta.setEstado(estado);
        return ventaRepository.save(venta);
    }
}
