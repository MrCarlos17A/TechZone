package com.tienda.backend_techzone.Service;

import com.tienda.backend_techzone.Model.Producto;
import com.tienda.backend_techzone.Repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductoServiceTest {

    private ProductoRepository productoRepository;
    private ProductoService productoService;

    private List<Producto> listaActivos;
    private Producto productoPorId;

    @BeforeEach
    void setUp() {
        listaActivos = List.of();
        productoPorId = null;

        productoRepository = (ProductoRepository) Proxy.newProxyInstance(
                ProductoRepository.class.getClassLoader(),
                new Class<?>[]{ProductoRepository.class},
                (proxy, method, args) -> {
                    if ("findByVigenteTrue".equals(method.getName())) {
                        return listaActivos;
                    }
                    if ("findByIdConRelaciones".equals(method.getName())) {
                        Long id = (Long) args[0];
                        if (productoPorId != null && productoPorId.getIdProducto().equals(id)) {
                            return Optional.of(productoPorId);
                        }
                        return Optional.empty();
                    }
                    return null;
                }
        );

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
        this.listaActivos = List.of(p1);

        List<Producto> activos = productoService.listarProductosActivos();

        assertNotNull(activos);
        assertEquals(1, activos.size());
        assertEquals("GeForce RTX 5080", activos.get(0).getNombre());
    }

    @Test
    @DisplayName("Debe buscar producto por ID correctamente cuando existe")
    void debeObtenerProductoPorId() {
        Producto p = new Producto();
        p.setIdProducto(10L);
        p.setNombre("Monitor Gamer OLED 240Hz");
        p.setVigente(true);
        this.productoPorId = p;

        Producto resultado = productoService.obtenerProductoPorId(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getIdProducto());
        assertEquals("Monitor Gamer OLED 240Hz", resultado.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException cuando el producto no existe")
    void debeLanzarExcepcionCuandoNoExiste() {
        assertThrows(RuntimeException.class, () -> {
            productoService.obtenerProductoPorId(999L);
        });
    }
}
