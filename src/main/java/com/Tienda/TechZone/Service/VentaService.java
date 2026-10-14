package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Model.DetalleVenta;
import com.Tienda.TechZone.Model.Pago;
import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Repository.ClienteRepository;
import com.Tienda.TechZone.Repository.DetalleVentaRepository;
import com.Tienda.TechZone.Repository.PagoRepository;
import com.Tienda.TechZone.Repository.ProductoRepository;
import com.Tienda.TechZone.Repository.RolRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
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
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

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

    /**
     * Pasa la venta de PENDIENTE a COMPLETADA y aplica todos sus efectos:
     * confirma el pago, acumula el gasto del cliente y, si era su primera
     * compra, lo promueve del rol "Usuario" al rol "Cliente".
     */
    @Transactional
    public Venta ConfirmarVenta(Long idVenta) {
        Venta venta = ObtenerVentaPorId(idVenta);
        if ("COMPLETADA".equals(venta.getEstado())) {
            throw new RuntimeException("La venta ya esta confirmada");
        }
        if ("CANCELADA".equals(venta.getEstado())) {
            throw new RuntimeException("La venta fue cancelada y no se puede confirmar");
        }

        venta.setEstado("COMPLETADA");
        venta = ventaRepository.save(venta);

        for (Pago pago : pagoRepository.findByVentaIdVenta(idVenta)) {
            pago.setEstadoPago("CONFIRMADO");
            pagoRepository.save(pago);
        }

        Cliente cliente = venta.getCliente();
        if (cliente != null) {
            BigDecimal total = venta.getTotal() != null ? venta.getTotal() : BigDecimal.ZERO;
            BigDecimal gastado = cliente.getTotalGastado() != null ? cliente.getTotalGastado() : BigDecimal.ZERO;
            cliente.setTotalGastado(gastado.add(total));
            cliente.setFechaUltimaCompra(LocalDateTime.now());
            clienteRepository.save(cliente);

            if (cliente.getPersona() != null) {
                usuarioRepository.findFirstByPersona(cliente.getPersona()).ifPresent(usuario -> {
                    String rolActual = usuario.getRol() != null ? usuario.getRol().getNombre() : null;
                    if ("Usuario".equals(rolActual)) {
                        rolRepository.findByNombre("Cliente").ifPresent(rolCliente -> {
                            usuario.setRol(rolCliente);
                            usuarioRepository.save(usuario);
                        });
                    }
                });
            }
        }
        return venta;
    }

    /**
     * Pasa la venta de PENDIENTE a CANCELADA, marca el pago como RECHAZADO
     * y devuelve al stock la cantidad que se habia reservado al crear la venta.
     */
    @Transactional
    public Venta CancelarVenta(Long idVenta) {
        Venta venta = ObtenerVentaPorId(idVenta);
        if ("COMPLETADA".equals(venta.getEstado())) {
            throw new RuntimeException("La venta ya fue confirmada y no se puede cancelar");
        }
        if ("CANCELADA".equals(venta.getEstado())) {
            throw new RuntimeException("La venta ya esta cancelada");
        }

        venta.setEstado("CANCELADA");
        venta = ventaRepository.save(venta);

        for (Pago pago : pagoRepository.findByVentaIdVenta(idVenta)) {
            pago.setEstadoPago("RECHAZADO");
            pagoRepository.save(pago);
        }

        for (DetalleVenta dv : detalleVentaRepository.findByVentaIdVenta(idVenta)) {
            if (dv.getProducto() == null || dv.getProducto().getIdProducto() == null || dv.getCantidad() == null) {
                continue;
            }
            Producto producto = productoRepository.findById(dv.getProducto().getIdProducto()).orElse(null);
            if (producto != null) {
                int stockActual = producto.getStock() != null ? producto.getStock() : 0;
                producto.setStock(stockActual + dv.getCantidad());
                productoRepository.save(producto);
            }
        }
        return venta;
    }
}
