package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Cliente;
import com.tienda.backend_techzone.Model.DetalleVenta;
import com.tienda.backend_techzone.Model.Pago;
import com.tienda.backend_techzone.Model.Producto;
import com.tienda.backend_techzone.Model.Venta;
import com.tienda.backend_techzone.Repository.ClienteRepository;
import com.tienda.backend_techzone.Repository.DetalleVentaRepository;
import com.tienda.backend_techzone.Repository.EmpleadoRepository;
import com.tienda.backend_techzone.Repository.PagoRepository;
import com.tienda.backend_techzone.Repository.ProductoRepository;
import com.tienda.backend_techzone.Repository.RolRepository;
import com.tienda.backend_techzone.Repository.UsuarioRepository;
import com.tienda.backend_techzone.Repository.VentaRepository;
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
    private final EmpleadoRepository empleadoRepository;

    public List<Venta> ListarVentas() {
        return ventaRepository.findAll();
    }

    public List<Venta> listarVentas() {
        return ListarVentas();
    }

    public List<Venta> ListarPorCliente(Long idCliente) {
        return ventaRepository.findByClienteIdCliente(idCliente);
    }

    public List<Venta> listarPorCliente(Long idCliente) {
        return ListarPorCliente(idCliente);
    }

    public Venta ObtenerVentaPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con id: " + id));
    }

    public Venta obtenerVentaPorId(Long id) {
        return ObtenerVentaPorId(id);
    }

    public Venta registrarVenta(Long idCliente, List<DetalleVenta> detalles, String direccionEnvio) {
        return RegistrarVenta(idCliente, detalles, direccionEnvio);
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
            Producto producto = productoRepository.findByIdConBloqueo(dv.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            if (producto.getStock() == null || producto.getStock() < dv.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() + " (Disponible: " + (producto.getStock() != null ? producto.getStock() : 0) + ")");
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
     * Ciclo de entrega del pedido:
     * PENDIENTE -> EN_PREPARACION -> EN_CAMINO -> ENTREGADO (o COMPLETADA)
     * También permite CANCELADA con devolución automática de inventario.
     */
    @Transactional
    public Venta actualizarEstadoPedido(Long idVenta, String nuevoEstado, String correoOperador) {
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new RuntimeException("El nuevo estado no puede estar vacío");
        }
        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        List<String> estadosValidos = List.of("PENDIENTE", "EN_PREPARACION", "EN_CAMINO", "ENTREGADO", "COMPLETADA", "CANCELADA");
        if (!estadosValidos.contains(estadoNormalizado)) {
            throw new RuntimeException("Estado inválido. Valores permitidos: PENDIENTE, EN_PREPARACION, EN_CAMINO, ENTREGADO, CANCELADA");
        }

        Venta venta = ObtenerVentaPorId(idVenta);
        String estadoAnterior = venta.getEstado() != null ? venta.getEstado().toUpperCase() : "PENDIENTE";

        if ("CANCELADA".equals(estadoAnterior)) {
            throw new RuntimeException("La orden de venta ya fue cancelada y no se puede modificar su estado");
        }

        if (estadoNormalizado.equals(estadoAnterior)) {
            return venta; // Sin cambios
        }

        // Si se cancela la orden, delegamos a CancelarVenta para restaurar stock y rechazar pago
        if ("CANCELADA".equals(estadoNormalizado)) {
            return CancelarVenta(idVenta);
        }

        // Si pasa a preparación, camino, entregado o completada, confirmamos pagos pendientes
        if (List.of("EN_PREPARACION", "EN_CAMINO", "ENTREGADO", "COMPLETADA").contains(estadoNormalizado)) {
            for (Pago pago : pagoRepository.findByVentaIdVenta(idVenta)) {
                if (!"CONFIRMADO".equals(pago.getEstadoPago())) {
                    pago.setEstadoPago("CONFIRMADO");
                    pagoRepository.save(pago);
                }
            }

            // Si es la primera vez que avanza más allá de PENDIENTE, registramos la compra en el cliente
            if ("PENDIENTE".equals(estadoAnterior)) {
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
            }
        }

        // Si se provee el correo del trabajador/administrador que despachó, lo asignamos como empleado
        if (correoOperador != null && !correoOperador.isBlank()) {
            usuarioRepository.findByCorreo(correoOperador).ifPresent(u -> {
                if (u.getPersona() != null) {
                    empleadoRepository.findFirstByPersona(u.getPersona()).ifPresent(venta::setEmpleado);
                }
            });
        }

        venta.setEstado(estadoNormalizado);
        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta ConfirmarVenta(Long idVenta) {
        return actualizarEstadoPedido(idVenta, "ENTREGADO", null);
    }

    public Venta confirmarVenta(Long idVenta) {
        return ConfirmarVenta(idVenta);
    }

    @Transactional
    public Venta CancelarVenta(Long idVenta) {
        Venta venta = ObtenerVentaPorId(idVenta);
        if ("ENTREGADO".equals(venta.getEstado()) || "COMPLETADA".equals(venta.getEstado())) {
            throw new RuntimeException("La venta ya fue completada/entregada y no se puede cancelar");
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

    public Venta cancelarVenta(Long idVenta) {
        return CancelarVenta(idVenta);
    }
}
