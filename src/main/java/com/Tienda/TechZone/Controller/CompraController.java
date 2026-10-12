package com.Tienda.TechZone.Controller;

import com.Tienda.TechZone.Model.Cliente;
import com.Tienda.TechZone.Model.DetalleVenta;
import com.Tienda.TechZone.Model.MedioPago;
import com.Tienda.TechZone.Model.Pago;
import com.Tienda.TechZone.Model.Persona;
import com.Tienda.TechZone.Model.Producto;
import com.Tienda.TechZone.Model.Ubigeo;
import com.Tienda.TechZone.Model.Venta;
import com.Tienda.TechZone.Model.Usuario;
import com.Tienda.TechZone.Repository.ClienteRepository;
import com.Tienda.TechZone.Repository.MedioPagoRepository;
import com.Tienda.TechZone.Repository.PagoRepository;
import com.Tienda.TechZone.Repository.PersonaRepository;
import com.Tienda.TechZone.Repository.UbigeoRepository;
import com.Tienda.TechZone.Repository.UsuarioRepository;
import com.Tienda.TechZone.Repository.VentaRepository;
import com.Tienda.TechZone.Repository.DetalleVentaRepository;
import com.Tienda.TechZone.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CompraController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoService productoService;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final PagoRepository pagoRepository;
    private final PersonaRepository personaRepository;
    private final UbigeoRepository ubigeoRepository;

    public record ItemCompra(Long idProducto, Integer cantidad) {
    }

    public record DatosPersonales(String dni, String nombres, String apellidoPaterno,
            String apellidoMaterno, String telefono, String correo) {
    }

    public record DireccionRequest(Integer idUbigeo, String direccionExacta, String referencia) {
    }

    public record PagoRequest(Integer idMedioPago, String codigoOperacion, String numeroCelular,
            String numeroTarjeta, String fechaVence, String cvv,
            String titular, String bancoOrigen, String codigoPPI) {
    }

    public record SolicitudCompra(List<ItemCompra> items, DatosPersonales datosPersonales,
            DireccionRequest direccion, PagoRequest pago) {
    }

    @PostMapping("/checkout")
    @Transactional
    public Map<String, Object> Checkout(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SolicitudCompra solicitud) {
        Map<String, Object> respuesta = new LinkedHashMap<>();

        if (userDetails == null) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Debes iniciar sesion para comprar");
            return respuesta;
        }

        List<ItemCompra> items = solicitud != null ? solicitud.items() : null;
        if (items == null || items.isEmpty()) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "El carrito esta vacio");
            return respuesta;
        }

        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername()).orElse(null);
        if (usuario == null) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Usuario no encontrado");
            return respuesta;
        }

        DatosPersonales dp = solicitud.datosPersonales();
        PagoRequest pr = solicitud.pago();
        DireccionRequest dr = solicitud.direccion();

        try {
            BigDecimal total = BigDecimal.ZERO;

            for (ItemCompra item : items) {
                if (item.cantidad() == null || item.cantidad() <= 0) {
                    continue;
                }
                Producto producto = productoService.ObtenerProductoPorId(item.idProducto());
                if (producto.getStock() == null || producto.getStock() < item.cantidad()) {
                    respuesta.put("ok", false);
                    respuesta.put("mensaje", "Stock insuficiente para: " + producto.getNombre());
                    return respuesta;
                }
                if (producto.getPrecioVenta() == null
                        || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
                    respuesta.put("ok", false);
                    respuesta.put("mensaje", "Precio invalido para: " + producto.getNombre());
                    return respuesta;
                }
                BigDecimal subtotal = producto.getPrecioVenta().multiply(BigDecimal.valueOf(item.cantidad()));
                total = total.add(subtotal);
            }

            if (total.compareTo(BigDecimal.ZERO) <= 0) {
                respuesta.put("ok", false);
                respuesta.put("mensaje", "El total de la compra debe ser mayor a cero");
                return respuesta;
            }

            BigDecimal igv = total.multiply(new BigDecimal("0.18"));
            BigDecimal totalConIgv = total.add(igv);

            // Actualizar Persona con datos del checkout
            Persona persona = usuario.getPersona();
            if (persona != null && dp != null) {
                if (dp.dni() != null && !dp.dni().isBlank()) {
                    persona.setNumeroDocumento(dp.dni());
                }
                if (dp.nombres() != null && !dp.nombres().isBlank()) {
                    persona.setNombres(dp.nombres());
                }
                if (dp.apellidoPaterno() != null && !dp.apellidoPaterno().isBlank()) {
                    persona.setApellidoPaterno(dp.apellidoPaterno());
                }
                if (dp.apellidoMaterno() != null && !dp.apellidoMaterno().isBlank()) {
                    persona.setApellidoMaterno(dp.apellidoMaterno());
                }
                if (dp.telefono() != null && !dp.telefono().isBlank()) {
                    persona.setTelefono(dp.telefono());
                }
                if (dr != null) {
                    if (dr.direccionExacta() != null && !dr.direccionExacta().isBlank()) {
                        persona.setDireccion(dr.direccionExacta());
                    }
                    if (dr.referencia() != null && !dr.referencia().isBlank()) {
                        persona.setReferencia(dr.referencia());
                    }
                    if (dr.idUbigeo() != null) {
                        Ubigeo ubigeo = ubigeoRepository.findById(dr.idUbigeo()).orElse(null);
                        if (ubigeo != null) {
                            persona.setUbigeo(ubigeo);
                        }
                    }
                }
                personaRepository.save(persona);
            }

            Cliente cliente = usuario.getPersona() != null
                    ? clienteRepository.findByPersona(usuario.getPersona()).orElse(null)
                    : null;
            if (cliente == null && usuario.getPersona() != null) {
                cliente = clienteRepository.save(Cliente.builder()
                        .persona(usuario.getPersona())
                        .totalGastado(BigDecimal.ZERO)
                        .build());
            }

            // Construir direccionEnvio
            String direccionEnvio = null;
            if (dr != null && dr.direccionExacta() != null && !dr.direccionExacta().isBlank()) {
                StringBuilder sb = new StringBuilder(dr.direccionExacta());
                if (dr.idUbigeo() != null) {
                    Ubigeo u = ubigeoRepository.findById(dr.idUbigeo()).orElse(null);
                    if (u != null) {
                        sb.append(", ").append(u.getDistrito());
                        sb.append(", ").append(u.getProvincia());
                        sb.append(", ").append(u.getDepartamento());
                    }
                }
                if (dr.referencia() != null && !dr.referencia().isBlank()) {
                    sb.append(" (Ref: ").append(dr.referencia()).append(")");
                }
                direccionEnvio = sb.toString();
            }

            Venta venta = Venta.builder()
                    .cliente(cliente)
                    .estado("PENDIENTE")
                    .fechaVenta(LocalDateTime.now())
                    .subtotal(total)
                    .igv(igv)
                    .total(totalConIgv)
                    .direccionEnvio(direccionEnvio)
                    .build();
            venta = ventaRepository.save(venta);

            for (ItemCompra item : items) {
                if (item.cantidad() == null || item.cantidad() <= 0) {
                    continue;
                }
                Producto producto = productoService.ObtenerProductoPorId(item.idProducto());
                BigDecimal subtotal = producto.getPrecioVenta().multiply(BigDecimal.valueOf(item.cantidad()));

                DetalleVenta detalle = DetalleVenta.builder()
                        .venta(venta)
                        .producto(producto)
                        .cantidad(item.cantidad())
                        .precioUnitario(producto.getPrecioVenta())
                        .subTotalVenta(subtotal)
                        .build();
                detalleVentaRepository.save(detalle);

                producto.setStock(producto.getStock() - item.cantidad());
                productoService.ActualizarProducto(producto.getIdProducto(), producto);
            }

            // Crear Pago
            MedioPago medioPago = null;
            String codigoOperacion = null;
            if (pr != null && pr.idMedioPago() != null) {
                medioPago = medioPagoRepository.findById(pr.idMedioPago()).orElse(null);
            }
            if (medioPago == null) {
                medioPago = medioPagoRepository.findById(1).orElse(null);
            }

            if (pr != null) {
                String nombreMedio = medioPago != null && medioPago.getNombre() != null
                        ? medioPago.getNombre().toLowerCase() : "";
                if (nombreMedio.contains("yape") || nombreMedio.contains("plin")) {
                    codigoOperacion = pr.codigoOperacion();
                } else if (nombreMedio.contains("tarjeta") && pr.numeroTarjeta() != null) {
                    String limpio = pr.numeroTarjeta().replaceAll("\\D", "");
                    String ultimos4 = limpio.length() >= 4 ? limpio.substring(limpio.length() - 4) : limpio;
                    codigoOperacion = "****-****-****-" + ultimos4;
                } else if (nombreMedio.contains("transferencia")) {
                    codigoOperacion = pr.codigoOperacion();
                } else if (nombreMedio.contains("pagoefectivo") || nombreMedio.contains("pago efectivo")) {
                    codigoOperacion = pr.codigoPPI();
                } else {
                    codigoOperacion = pr.codigoOperacion();
                }
            }

            if (codigoOperacion != null && codigoOperacion.length() > 50) {
                codigoOperacion = codigoOperacion.substring(0, 50);
            }

            pagoRepository.save(Pago.builder()
                    .venta(venta)
                    .medioPago(medioPago)
                    .montoPago(totalConIgv)
                    .fechaPago(LocalDateTime.now())
                    .codigoOperacion(codigoOperacion)
                    .estadoPago("PENDIENTE")
                    .build());

            if (cliente != null) {
                BigDecimal gastadoPrevio = cliente.getTotalGastado() != null ? cliente.getTotalGastado() : BigDecimal.ZERO;
                cliente.setTotalGastado(gastadoPrevio.add(totalConIgv));
                cliente.setFechaUltimaCompra(LocalDateTime.now());
                clienteRepository.save(cliente);
            }

            respuesta.put("ok", true);
            respuesta.put("idPedido", venta.getIdVenta());
            respuesta.put("total", totalConIgv);
            return respuesta;
        } catch (RuntimeException e) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", e.getMessage());
            return respuesta;
        }
    }
}
