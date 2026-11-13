import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ProductoDetalleComponent } from './producto-detalle.component';
import { ProductoService } from '../../services/producto.service';
import { CarritoService } from '../../services/carrito.service';
import { AuthService } from '../../services/auth.service';
import { Producto, ProductoImagen } from '../../models/producto.models';

describe('ProductoDetalleComponent - Galeria Estilo Promart', () => {
  let component: ProductoDetalleComponent;
  let mockProductoService: any;
  let mockCarritoService: any;
  let mockAuthService: any;

  const productoMock: Producto = {
    idProducto: 1,
    nombre: 'Monitor Gaming ASUS ROG 27"',
    precioVenta: 1200.00,
    stock: 5,
    codigoSku: 'ASMO01',
    vigente: true
  };

  const imagenesMock: ProductoImagen[] = [
    { idProductoImagen: 1, idProducto: 1, urlImagen: 'https://test/front.png', esPrincipal: true, orden: 1, etiqueta: 'Vista Frontal' },
    { idProductoImagen: 2, idProducto: 1, urlImagen: 'https://test/side.png', esPrincipal: false, orden: 2, etiqueta: 'Vista Lateral' },
    { idProductoImagen: 3, idProducto: 1, urlImagen: 'https://test/ports.png', esPrincipal: false, orden: 3, etiqueta: 'Puertos y Conexiones' }
  ];

  beforeEach(async () => {
    mockProductoService = {
      getProductoById: vi.fn().mockReturnValue(of(productoMock)),
      getImagenesProducto: vi.fn().mockReturnValue(of(imagenesMock))
    };
    mockCarritoService = {
      agregarProducto: vi.fn().mockReturnValue(of({}))
    };
    mockAuthService = {
      isAuthenticated: vi.fn().mockReturnValue(true)
    };

    await TestBed.configureTestingModule({
      imports: [ProductoDetalleComponent],
      providers: [
        provideRouter([]),
        { provide: ProductoService, useValue: mockProductoService },
        { provide: CarritoService, useValue: mockCarritoService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ProductoDetalleComponent);
    component = fixture.componentInstance;
  });

  it('debe inicializarse y cargar imagenes ordenadas por orden', () => {
    component.cargarProducto(1);
    expect(component.producto()).toEqual(productoMock);
    expect(component.imagenes().length).toBe(3);
    expect(component.indiceImagenActual()).toBe(0);
    expect(component.imagenActual()?.etiqueta).toBe('Vista Frontal');
    expect(component.imagenSeleccionadaUrl()).toBe('https://test/front.png');
  });

  it('debe cambiar de imagen al seleccionar miniatura por indice', () => {
    component.cargarProducto(1);
    component.seleccionarImagen(1);
    expect(component.indiceImagenActual()).toBe(1);
    expect(component.imagenActual()?.etiqueta).toBe('Vista Lateral');
    expect(component.imagenSeleccionadaUrl()).toBe('https://test/side.png');
  });

  it('debe avanzar a la siguiente imagen con siguienteImagen() y ciclar al inicio', () => {
    component.cargarProducto(1);
    expect(component.indiceImagenActual()).toBe(0);

    component.siguienteImagen();
    expect(component.indiceImagenActual()).toBe(1);

    component.siguienteImagen();
    expect(component.indiceImagenActual()).toBe(2);

    // Ciclo al primer elemento
    component.siguienteImagen();
    expect(component.indiceImagenActual()).toBe(0);
  });

  it('debe retroceder con anteriorImagen() y ciclar al final', () => {
    component.cargarProducto(1);
    expect(component.indiceImagenActual()).toBe(0);

    // Ciclo al ultimo elemento desde el inicio
    component.anteriorImagen();
    expect(component.indiceImagenActual()).toBe(2);

    component.anteriorImagen();
    expect(component.indiceImagenActual()).toBe(1);
  });

  it('debe incrementar y decrementar la cantidad respetando el stock', () => {
    component.cargarProducto(1);
    expect(component.cantidad()).toBe(1);

    component.incrementar();
    expect(component.cantidad()).toBe(2);

    component.decrementar();
    expect(component.cantidad()).toBe(1);

    // No debe decrementar por debajo de 1
    component.decrementar();
    expect(component.cantidad()).toBe(1);
  });
});

