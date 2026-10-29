package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.*;
import com.tienda.backend_techzone.Repository.*;
import com.tienda.backend_techzone.Service.DetalleCarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carrito")
public class CarritoApiController {

    private final CarritoRepository carritoRepository;
    private final DetalleCarritoRepository detalleCarritoRepository;
    private final DetalleCarritoService detalleCarritoService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final ProductoImagenRepository productoImagenRepository;

    public CarritoApiController(CarritoRepository carritoRepository,
                                DetalleCarritoRepository detalleCarritoRepository,
                                DetalleCarritoService detalleCarritoService,
                                UsuarioRepository usuarioRepository,
                                ClienteRepository clienteRepository,
                                ProductoRepository productoRepository,
                                ProductoImagenRepository productoImagenRepository) {
        this.carritoRepository = carritoRepository;
        this.detalleCarritoRepository = detalleCarritoRepository;
        this.detalleCarritoService = detalleCarritoService;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.productoImagenRepository = productoImagenRepository;
    }

    public record SolicitudAgregarItem(Long idProducto, Integer cantidad) {
    }

    public record SolicitudActualizarCantidad(Integer cantidad) {
    }

    @GetMapping
    @Transactional
    public ResponseEntity<?> obtenerCarrito(@AuthenticationPrincipal UserDetails detalles) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesion"));
        }
        Carrito carrito = obtenerOCrearCarrito(detalles.getUsername());
        return ResponseEntity.ok(construirRespuestaCarrito(carrito));
    }

    @PostMapping("/items")
    @Transactional
    public ResponseEntity<?> agregarItem(
            @AuthenticationPrincipal UserDetails detalles,
            @RequestBody SolicitudAgregarItem req) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesion"));
        }
        if (req == null || req.idProducto() == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El producto es obligatorio"));
        }
        int cantidad = (req.cantidad() != null && req.cantidad() > 0) ? req.cantidad() : 1;

        Producto producto = productoRepository.findById(req.idProducto()).orElse(null);
        if (producto == null) {
            return ResponseEntity.status(404).body(Map.of("mensaje", "Producto no encontrado"));
        }
        if (producto.getStock() == null || producto.getStock() < cantidad) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Stock insuficiente"));
        }

        Carrito carrito = obtenerOCrearCarrito(detalles.getUsername());
        List<DetalleCarrito> detallesExistentes = detalleCarritoRepository.findByCarritoIdCarrito(carrito.getIdCarrito());

        Optional<DetalleCarrito> yaEnCarrito = detallesExistentes.stream()
                .filter(d -> d.getProducto() != null && d.getProducto().getIdProducto().equals(req.idProducto()))
                .findFirst();

        if (yaEnCarrito.isPresent()) {
            DetalleCarrito d = yaEnCarrito.get();
            int nuevaCantidad = d.getCantidad() + cantidad;
            if (producto.getStock() < nuevaCantidad) {
                return ResponseEntity.badRequest().body(Map.of("mensaje", "No hay stock suficiente para aumentar la cantidad"));
            }
            d.setCantidad(nuevaCantidad);
            detalleCarritoRepository.save(d);
        } else {
            DetalleCarrito nuevo = DetalleCarrito.builder()
                    .carrito(carrito)
                    .producto(producto)
                    .cantidad(cantidad)
                    .build();
            detalleCarritoRepository.save(nuevo);
        }

        return ResponseEntity.ok(construirRespuestaCarrito(carrito));
    }

    @PutMapping("/items/{idDetalle}")
    @Transactional
    public ResponseEntity<?> actualizarCantidad(
            @AuthenticationPrincipal UserDetails detalles,
            @PathVariable Long idDetalle,
            @RequestBody SolicitudActualizarCantidad req) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesion"));
        }
        if (req == null || req.cantidad() == null || req.cantidad() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La cantidad debe ser mayor a 0"));
        }
        try {
            detalleCarritoService.actualizarCantidad(idDetalle, req.cantidad());
            Carrito carrito = obtenerOCrearCarrito(detalles.getUsername());
            return ResponseEntity.ok(construirRespuestaCarrito(carrito));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{idDetalle}")
    @Transactional
    public ResponseEntity<?> eliminarItem(
            @AuthenticationPrincipal UserDetails detalles,
            @PathVariable Long idDetalle) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesion"));
        }
        try {
            detalleCarritoService.eliminarDetalle(idDetalle);
            Carrito carrito = obtenerOCrearCarrito(detalles.getUsername());
            return ResponseEntity.ok(construirRespuestaCarrito(carrito));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<?> vaciarCarrito(@AuthenticationPrincipal UserDetails detalles) {
        if (detalles == null) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Debes iniciar sesion"));
        }
        Carrito carrito = obtenerOCrearCarrito(detalles.getUsername());
        detalleCarritoRepository.deleteByCarritoIdCarrito(carrito.getIdCarrito());
        return ResponseEntity.ok(Map.of("mensaje", "Carrito vaciado"));
    }

    private Carrito obtenerOCrearCarrito(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Persona persona = usuario.getPersona();
        if (persona == null) {
            throw new RuntimeException("El usuario no tiene persona asociada");
        }

        Cliente cliente = clienteRepository.findFirstByPersona(persona)
                .orElseGet(() -> {
                    Cliente nuevo = Cliente.builder()
                            .persona(persona)
                            .totalGastado(BigDecimal.ZERO)
                            .vigente(true)
                            .build();
                    return clienteRepository.save(nuevo);
                });

        return carritoRepository.findFirstByClienteIdCliente(cliente.getIdCliente())
                .orElseGet(() -> {
                    Carrito nuevo = Carrito.builder()
                            .cliente(cliente)
                            .fechaCarrito(LocalDateTime.now())
                            .build();
                    return carritoRepository.save(nuevo);
                });
    }

    private Map<String, Object> construirRespuestaCarrito(Carrito carrito) {
        List<DetalleCarrito> lista = detalleCarritoRepository.findByCarritoIdCarrito(carrito.getIdCarrito());
        BigDecimal subtotal = BigDecimal.ZERO;

        Set<Long> prodIds = lista.stream()
                .map(d -> d.getProducto() != null ? d.getProducto().getIdProducto() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> imagenMap = new HashMap<>();
        if (!prodIds.isEmpty()) {
            List<ProductoImagen> todasImagenes = productoImagenRepository.findByProductoIdProductoIn(prodIds);
            for (ProductoImagen img : todasImagenes) {
                Long idProd = img.getIdProducto();
                if (idProd != null) {
                    if (!imagenMap.containsKey(idProd) || Boolean.TRUE.equals(img.getEsPrincipal())) {
                        imagenMap.put(idProd, img.getUrlImagen());
                    }
                }
            }
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (DetalleCarrito d : lista) {
            Producto p = d.getProducto();
            BigDecimal precio = p != null && p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO;
            int cant = d.getCantidad() != null ? d.getCantidad() : 0;
            BigDecimal subItem = precio.multiply(BigDecimal.valueOf(cant));
            subtotal = subtotal.add(subItem);

            String imagenUrl = (p != null && p.getIdProducto() != null) ? imagenMap.get(p.getIdProducto()) : null;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idDetalle", d.getIdDetalleCarrito());
            item.put("idProducto", p != null ? p.getIdProducto() : null);
            item.put("nombre", p != null ? p.getNombre() : "");
            item.put("codigoSku", p != null ? p.getCodigoSku() : "");
            item.put("precioVenta", precio);
            item.put("cantidad", cant);
            item.put("stock", p != null ? p.getStock() : 0);
            item.put("subtotal", subItem);
            item.put("imagenUrl", imagenUrl);
            items.add(item);
        }

        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18"));
        BigDecimal total = subtotal.add(igv);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("idCarrito", carrito.getIdCarrito());
        resp.put("items", items);
        resp.put("cantidadItems", items.size());
        resp.put("subtotal", subtotal);
        resp.put("igv", igv);
        resp.put("total", total);
        return resp;
    }
}
