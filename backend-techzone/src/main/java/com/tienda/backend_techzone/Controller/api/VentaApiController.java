package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.*;
import com.tienda.backend_techzone.Repository.*;
import com.tienda.backend_techzone.Service.VentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class VentaApiController {

    private final VentaService ventaService;
    private final DetalleVentaRepository detalleVentaRepository;
    private final PagoRepository pagoRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final CarritoRepository carritoRepository;
    private final DetalleCarritoRepository detalleCarritoRepository;
    private final ProductoRepository productoRepository;
    private final PersonaRepository personaRepository;
    private final UbigeoRepository ubigeoRepository;

    public VentaApiController(VentaService ventaService,
                              DetalleVentaRepository detalleVentaRepository,
                              PagoRepository pagoRepository,
                              MedioPagoRepository medioPagoRepository,
                              UsuarioRepository usuarioRepository,
                              ClienteRepository clienteRepository,
                              CarritoRepository carritoRepository,
                              DetalleCarritoRepository detalleCarritoRepository,
                              ProductoRepository productoRepository,
                              PersonaRepository personaRepository,
                              UbigeoRepository ubigeoRepository) {
        this.ventaService = ventaService;
        this.detalleVentaRepository = detalleVentaRepository;
        this.pagoRepository = pagoRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.carritoRepository = carritoRepository;
        this.detalleCarritoRepository = detalleCarritoRepository;
        this.productoRepository = productoRepository;
        this.personaRepository = personaRepository;
        this.ubigeoRepository = ubigeoRepository;
    }

    public record ItemCheckout(Long idProducto, Integer cantidad) {
    }

    public record SolicitudCheckout(
            String direccionEnvio,
            String referencia,
            Long idUbigeo,
            String telefono,
            String numeroDocumento,
            Long idMedioPago,
            String codigoOperacion,
            String celularPago,
            List<ItemCheckout> items
    ) {}

    // --- CHECKOUT ---
    @PostMapping("/api/checkout")
    @Transactional
    public ResponseEntity<?> checkout(
            @AuthenticationPrincipal UserDetails detalles,
            @RequestBody SolicitudCheckout req) {

        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesión para comprar"));
        }

        Usuario usuario = usuarioRepository.findByCorreo(detalles.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Persona persona = usuario.getPersona();
        if (persona == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El usuario no tiene una persona asociada"));
        }

        // 1. Validar y actualizar número de documento (DNI)
        if (req != null && req.numeroDocumento() != null && !req.numeroDocumento().isBlank()) {
            String dni = req.numeroDocumento().trim();
            if (!dni.matches("^\\d{8}$")) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "El DNI debe tener exactamente 8 dígitos numéricos"));
            }
            java.util.Optional<Persona> persDoc = personaRepository.findFirstByNumeroDocumento(dni);
            if (persDoc.isPresent() && !persDoc.get().getIdPersona().equals(persona.getIdPersona())) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "El número de documento ya está registrado por otra persona"));
            }
            persona.setNumeroDocumento(dni);
        }

        // 2. Validar y actualizar teléfono celular (9 dígitos empezando en 9)
        if (req != null && req.telefono() != null && !req.telefono().isBlank()) {
            String tel = req.telefono().trim();
            if (!tel.matches("^9\\d{8}$")) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "El teléfono celular debe tener 9 dígitos y empezar con 9"));
            }
            java.util.Optional<Persona> persTel = personaRepository.findByTelefono(tel);
            if (persTel.isPresent() && !persTel.get().getIdPersona().equals(persona.getIdPersona())) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "El teléfono ya se encuentra registrado por otra persona"));
            }
            persona.setTelefono(tel);
        } else if (persona.getTelefono() == null || !persona.getTelefono().matches("^9\\d{8}$")) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debes ingresar un número de celular válido de 9 dígitos"));
        }

        // 3. Validar dirección de entrega (mínimo 10 caracteres)
        String direccion = (req != null && req.direccionEnvio() != null && !req.direccionEnvio().isBlank())
                ? req.direccionEnvio().trim().toUpperCase()
                : (persona.getDireccion() != null ? persona.getDireccion().trim().toUpperCase() : "");

        if (direccion.length() < 10) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La dirección de entrega debe tener al menos 10 caracteres (ej. Av./Calle y número)"));
        }

        // 4. Ubigeo y referencia
        if (req != null && req.idUbigeo() != null) {
            ubigeoRepository.findById(req.idUbigeo()).ifPresent(persona::setUbigeo);
        }
        if (req != null && req.referencia() != null && !req.referencia().isBlank()) {
            persona.setReferencia(req.referencia().trim().toUpperCase());
        }
        persona.setDireccion(direccion);
        personaRepository.save(persona);

        Cliente cliente = clienteRepository.findFirstByPersona(persona)
                .orElseGet(() -> clienteRepository.save(Cliente.builder()
                        .persona(persona)
                        .totalGastado(BigDecimal.ZERO)
                        .vigente(true)
                        .build()));

        List<DetalleVenta> listaDetalles = new ArrayList<>();

        if (req != null && req.items() != null && !req.items().isEmpty()) {
            for (ItemCheckout it : req.items()) {
                if (it.idProducto() != null && it.cantidad() != null && it.cantidad() > 0) {
                    Producto p = productoRepository.findById(it.idProducto()).orElse(null);
                    if (p != null) {
                        DetalleVenta dv = new DetalleVenta();
                        dv.setProducto(p);
                        dv.setCantidad(it.cantidad());
                        listaDetalles.add(dv);
                    }
                }
            }
        } else {
            // Tomar items del carrito
            Carrito carrito = carritoRepository.findFirstByClienteIdCliente(cliente.getIdCliente()).orElse(null);
            if (carrito != null) {
                List<DetalleCarrito> itemsCarrito = detalleCarritoRepository.findByCarritoIdCarrito(carrito.getIdCarrito());
                for (DetalleCarrito dc : itemsCarrito) {
                    DetalleVenta dv = new DetalleVenta();
                    dv.setProducto(dc.getProducto());
                    dv.setCantidad(dc.getCantidad());
                    listaDetalles.add(dv);
                }
                // Limpiar carrito luego de crear venta
                detalleCarritoRepository.deleteByCarritoIdCarrito(carrito.getIdCarrito());
            }
        }

        if (listaDetalles.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No hay productos en la orden de compra"));
        }

        try {
            Venta venta = ventaService.registrarVenta(cliente.getIdCliente(), listaDetalles, direccion);

            // Registrar pago (garantizado para toda orden de compra)
            Long idMedio = (req != null && req.idMedioPago() != null) ? req.idMedioPago() : null;
            MedioPago medio = null;
            if (idMedio != null) {
                medio = medioPagoRepository.findById(idMedio).orElse(null);
            }
            if (medio == null) {
                medio = medioPagoRepository.findAll().stream().findFirst().orElse(null);
            }
            if (medio != null) {
                String codOp = (req != null && req.codigoOperacion() != null && !req.codigoOperacion().isBlank())
                        ? req.codigoOperacion().trim()
                        : "OP-" + String.format("%06d", venta.getIdVenta());

                Pago pago = Pago.builder()
                        .venta(venta)
                        .medioPago(medio)
                        .montoPago(venta.getTotal())
                        .fechaPago(LocalDateTime.now())
                        .codigoOperacion(codOp)
                        .estadoPago("PENDIENTE")
                        .build();
                pagoRepository.save(pago);
            }

            return ResponseEntity.status(201).body(Map.of(
                    "mensaje", "Venta registrada exitosamente",
                    "idVenta", venta.getIdVenta(),
                    "total", venta.getTotal(),
                    "estado", venta.getEstado()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // --- CLIENTE: MIS PEDIDOS ---
    @GetMapping("/api/mis-pedidos")
    public ResponseEntity<?> misPedidos(@AuthenticationPrincipal UserDetails detalles) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesion"));
        }
        Usuario usuario = usuarioRepository.findByCorreo(detalles.getUsername()).orElse(null);
        if (usuario == null || usuario.getPersona() == null) {
            return ResponseEntity.ok(List.of());
        }
        Cliente cliente = clienteRepository.findFirstByPersona(usuario.getPersona()).orElse(null);
        if (cliente == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Venta> ventas = ventaService.listarPorCliente(cliente.getIdCliente());
        List<Map<String, Object>> resp = new ArrayList<>();
        for (Venta v : ventas) {
            resp.add(mapearVentaResumen(v));
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/mis-pedidos/{id}")
    public ResponseEntity<?> miPedidoDetalle(
            @AuthenticationPrincipal UserDetails detalles,
            @PathVariable Long id) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesion"));
        }
        try {
            Venta venta = ventaService.obtenerVentaPorId(id);
            Usuario usuario = usuarioRepository.findByCorreo(detalles.getUsername()).orElse(null);
            if (usuario == null || usuario.getPersona() == null || venta.getCliente() == null
                    || !Objects.equals(venta.getCliente().getPersona().getIdPersona(), usuario.getPersona().getIdPersona())) {
                return ResponseEntity.status(403).body(Map.of("mensaje", "No tienes acceso a este pedido"));
            }
            return ResponseEntity.ok(mapearVentaCompleta(venta));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensaje", e.getMessage()));
        }
    }

    // --- ADMIN: VENTAS ---
    @GetMapping("/api/admin/ventas")
    public ResponseEntity<?> listarAdmin() {
        List<Venta> ventas = ventaService.listarVentas();
        List<Map<String, Object>> resp = new ArrayList<>();
        for (Venta v : ventas) {
            resp.add(mapearVentaResumen(v));
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/admin/ventas/{id}")
    public ResponseEntity<?> obtenerAdmin(@PathVariable Long id) {
        try {
            Venta venta = ventaService.obtenerVentaPorId(id);
            return ResponseEntity.ok(mapearVentaCompleta(venta));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PatchMapping("/api/admin/ventas/{id}/estado")
    public ResponseEntity<?> cambiarEstadoAdmin(@PathVariable Long id,
                                                @RequestBody Map<String, String> body,
                                                @AuthenticationPrincipal UserDetails detalles) {
        try {
            String nuevoEstado = body != null ? body.get("estado") : null;
            String correoOperador = detalles != null ? detalles.getUsername() : null;
            Venta actualizada = ventaService.actualizarEstadoPedido(id, nuevoEstado, correoOperador);
            return ResponseEntity.ok(mapearVentaCompleta(actualizada));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PatchMapping("/api/admin/ventas/{id}/confirmar")
    public ResponseEntity<?> confirmarAdmin(@PathVariable Long id, @AuthenticationPrincipal UserDetails detalles) {
        try {
            String correoOperador = detalles != null ? detalles.getUsername() : null;
            Venta confirmada = ventaService.actualizarEstadoPedido(id, "ENTREGADO", correoOperador);
            return ResponseEntity.ok(mapearVentaCompleta(confirmada));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PatchMapping("/api/admin/ventas/{id}/cancelar")
    public ResponseEntity<?> cancelarAdmin(@PathVariable Long id) {
        try {
            Venta cancelada = ventaService.cancelarVenta(id);
            return ResponseEntity.ok(mapearVentaCompleta(cancelada));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    private Map<String, Object> mapearVentaResumen(Venta v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idVenta", v.getIdVenta());
        m.put("fechaVenta", v.getFechaVenta());
        m.put("estado", v.getEstado());
        m.put("subtotal", v.getSubtotal());
        m.put("igv", v.getIgv());
        m.put("total", v.getTotal());
        m.put("direccionEnvio", v.getDireccionEnvio());
        if (v.getCliente() != null && v.getCliente().getPersona() != null) {
            Persona p = v.getCliente().getPersona();
            m.put("clienteNombre", p.getNombreCompleto());
            m.put("clienteDocumento", p.getNumeroDocumento());
            m.put("clienteTelefono", p.getTelefono());
        }
        if (v.getEmpleado() != null && v.getEmpleado().getPersona() != null) {
            Persona pe = v.getEmpleado().getPersona();
            m.put("despachadoPor", pe.getNombreCompleto());
        }
        return m;
    }

    private Map<String, Object> mapearVentaCompleta(Venta v) {
        Map<String, Object> m = mapearVentaResumen(v);
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaIdVenta(v.getIdVenta());
        List<Map<String, Object>> items = new ArrayList<>();
        for (DetalleVenta dv : detalles) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idDetalleVenta", dv.getIdDetalleVenta());
            item.put("idProducto", dv.getProducto() != null ? dv.getProducto().getIdProducto() : null);
            item.put("nombreProducto", dv.getProducto() != null ? dv.getProducto().getNombre() : "");
            item.put("precioUnitario", dv.getPrecioUnitario());
            item.put("cantidad", dv.getCantidad());
            item.put("subTotalVenta", dv.getSubTotalVenta());
            items.add(item);
        }
        m.put("detalles", items);

        List<Pago> pagos = pagoRepository.findByVentaIdVenta(v.getIdVenta());
        List<Map<String, Object>> pagosInfo = new ArrayList<>();
        for (Pago p : pagos) {
            Map<String, Object> pi = new LinkedHashMap<>();
            pi.put("idPago", p.getIdPago());
            pi.put("medioPago", p.getMedioPago() != null ? p.getMedioPago().getNombre() : "");
            pi.put("monto", p.getMontoPago());
            pi.put("fecha", p.getFechaPago());
            pi.put("codigoOperacion", p.getCodigoOperacion());
            pi.put("estadoPago", p.getEstadoPago());
            pagosInfo.add(pi);
        }
        m.put("pagos", pagosInfo);

        return m;
    }
}
