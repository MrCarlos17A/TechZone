package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Producto;
import com.tienda.backend_techzone.Repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository);
    }

    @Test
    @DisplayName("Debe listar unicamente los productos activos/vigentes")
    void debeListarProductosActivos() {
        Producto p1 = new Producto();
        p1.setIdProducto(1L);
        p1.setNombre("GeForce RTX 5080");
        p1.setPrecioVenta(new BigDecimal("4599.00"));
        p1.setVigente(true);

        when(productoRepository.findByVigenteTrue()).thenReturn(List.of(p1));

        List<Producto> activos = productoService.listarProductosActivos();

        assertNotNull(activos);
        assertEquals(1, activos.size());
        assertEquals("GeForce RTX 5080", activos.get(0).getNombre());
        verify(productoRepository, times(1)).findByVigenteTrue();
    }

    @Test
    @DisplayName("Debe buscar producto por ID correctamente cuando existe")
    void debeObtenerProductoPorId() {
        Producto p = new Producto();
        p.setIdProducto(10L);
        p.setNombre("Monitor Gamer OLED 240Hz");
        p.setVigente(true);

        when(productoRepository.findByIdConRelaciones(10L)).thenReturn(Optional.of(p));

        Producto resultado = productoService.obtenerProductoPorId(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getIdProducto());
        assertEquals("Monitor Gamer OLED 240Hz", resultado.getNombre());
        verify(productoRepository, times(1)).findByIdConRelaciones(10L);
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException cuando el producto no existe")
    void debeLanzarExcepcionCuandoNoExiste() {
        when(productoRepository.findByIdConRelaciones(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productoService.obtenerProductoPorId(999L);
        });
        verify(productoRepository, times(1)).findByIdConRelaciones(999L);
    }
}
