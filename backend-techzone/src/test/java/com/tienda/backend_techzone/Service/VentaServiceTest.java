package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Cliente;
import com.tienda.backend_techzone.Model.DetalleVenta;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private VentaService ventaService;

    @Test
    @DisplayName("Debe registrar venta y calcular subtotal, igv y total")
    void debeRegistrarVentaYCalcularTotales() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);

        Producto producto = new Producto();
        producto.setIdProducto(10L);
        producto.setNombre("Monitor Gamer OLED 240Hz");
        producto.setPrecioVenta(new BigDecimal("1000.00"));
        producto.setStock(10);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdConBloqueo(10L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(i -> {
            Venta v = i.getArgument(0);
            v.setIdVenta(100L);
            return v;
        });

        Venta venta = ventaService.registrarVenta(1L, List.of(detalle), "Av. Central 123");

        assertNotNull(venta);
        assertEquals(100L, venta.getIdVenta());
        assertEquals("PENDIENTE", venta.getEstado());
        assertEquals(new BigDecimal("2000.00"), venta.getSubtotal());
        assertEquals(new BigDecimal("360.0000"), venta.getIgv());
        assertEquals(new BigDecimal("2360.0000"), venta.getTotal());
        assertEquals(8, producto.getStock());

        verify(ventaRepository, times(1)).save(any(Venta.class));
        verify(detalleVentaRepository, times(1)).save(any(DetalleVenta.class));
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Debe obtener venta por id cuando existe")
    void debeObtenerVentaPorId() {
        Venta v = new Venta();
        v.setIdVenta(50L);
        v.setEstado("PENDIENTE");

        when(ventaRepository.findById(50L)).thenReturn(Optional.of(v));

        Venta resultado = ventaService.obtenerVentaPorId(50L);

        assertNotNull(resultado);
        assertEquals(50L, resultado.getIdVenta());
        verify(ventaRepository, times(1)).findById(50L);
    }

    @Test
    @DisplayName("Debe actualizar el estado de una orden de venta")
    void debeActualizarEstadoDePedido() {
        Venta v = new Venta();
        v.setIdVenta(25L);
        v.setEstado("PENDIENTE");

        when(ventaRepository.findById(25L)).thenReturn(Optional.of(v));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(i -> i.getArgument(0));

        Venta actualizada = ventaService.actualizarEstadoPedido(25L, "EN_PREPARACION", "admin@techzone.com");

        assertNotNull(actualizada);
        assertEquals("EN_PREPARACION", actualizada.getEstado());
        verify(ventaRepository, times(1)).save(v);
    }

    @Test
    @DisplayName("Debe lanzar excepcion al recibir un estado invalido")
    void debeLanzarExcepcionConEstadoInvalido() {
        assertThrows(RuntimeException.class, () -> {
            ventaService.actualizarEstadoPedido(1L, "ESTADO_INEXISTENTE", "admin@techzone.com");
        });
    }
}
