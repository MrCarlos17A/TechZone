import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { MisPedidosComponent } from './mis-pedidos.component';
import { VentaService } from '../../services/venta.service';
import { AuthService } from '../../services/auth.service';
import { ConfiguracionService } from '../../services/configuracion.service';
import { VentaCompleta, VentaResumen } from '../../models/venta.models';

describe('MisPedidosComponent - Comprobante de Pago', () => {
  let component: MisPedidosComponent;
  let mockVentaService: any;
  let mockAuthService: any;
  let mockConfiguracionService: any;

  const ventaResumenMock: VentaResumen = {
    idVenta: 1,
    fechaVenta: '2026-11-14T20:00:00',
    estado: 'ENTREGADO',
    subtotal: 1000,
    igv: 180,
    total: 1180,
    direccionEnvio: 'Av. Javier Prado 4200'
  };

  const ventaCompletaMock: VentaCompleta = {
    ...ventaResumenMock,
    clienteNombre: 'CARLOS ARROYO',
    clienteDocumento: '73234768',
    medioPago: 'Yape (BCP)',
    detalles: [
      {
        idDetalleVenta: 1,
        idProducto: 10,
        nombreProducto: 'Monitor Gamer 240Hz',
        precioUnitario: 1000,
        cantidad: 1,
        subTotalVenta: 1000
      }
    ],
    pagos: [
      {
        idPago: 1,
        medioPago: 'Yape (BCP)',
        monto: 1180,
        fecha: '2026-11-14T20:00:00',
        codigoOperacion: 'OP-000001',
        estadoPago: 'CONFIRMADO'
      }
    ]
  };

  beforeEach(async () => {
    mockVentaService = {
      getMisPedidos: vi.fn().mockReturnValue(of([ventaResumenMock])),
      getMiPedidoDetalle: vi.fn().mockReturnValue(of(ventaCompletaMock))
    };
    mockAuthService = {
      isAuthenticated: vi.fn().mockReturnValue(true)
    };
    mockConfiguracionService = {
      configuracion: vi.fn().mockReturnValue({ logoUrl: 'https://test/logo.png' })
    };

    await TestBed.configureTestingModule({
      imports: [MisPedidosComponent],
      providers: [
        provideRouter([]),
        { provide: VentaService, useValue: mockVentaService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ConfiguracionService, useValue: mockConfiguracionService }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(MisPedidosComponent);
    component = fixture.componentInstance;
  });

  it('debe clasificar como boleta cuando el documento tiene 8 digitos', () => {
    const esFactura = component.esFactura(ventaCompletaMock);
    expect(esFactura).toBe(false);
  });

  it('debe clasificar como factura cuando el documento es RUC de 11 digitos', () => {
    const ventaFactura: VentaCompleta = {
      ...ventaCompletaMock,
      clienteDocumento: '20608945123',
      clienteNombre: 'TECHZONE SAC'
    };
    const esFactura = component.esFactura(ventaFactura);
    expect(esFactura).toBe(true);
  });

  it('debe cargar los datos del comprobante al invocar verComprobante', () => {
    component.verComprobante(1);
    expect(mockVentaService.getMiPedidoDetalle).toHaveBeenCalledWith(1);
    expect(component.comprobanteSeleccionado()).toEqual(ventaCompletaMock);
  });

  it('debe generar hash digital para el comprobante', () => {
    const hash = component.generarHashSimulado(ventaCompletaMock);
    expect(hash).toBeDefined();
    expect(hash.length).toBeGreaterThan(10);
  });

  it('debe cerrar el comprobante seleccionado', () => {
    component.verComprobante(1);
    expect(component.comprobanteSeleccionado()).not.toBeNull();
    component.cerrarComprobante();
    expect(component.comprobanteSeleccionado()).toBeNull();
  });
});
