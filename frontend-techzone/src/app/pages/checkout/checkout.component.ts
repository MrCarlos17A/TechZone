import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CarritoService } from '../../services/carrito.service';
import { VentaService } from '../../services/venta.service';
import { ConfiguracionService } from '../../services/configuracion.service';
import { AuthService } from '../../services/auth.service';
import { ProductoService } from '../../services/producto.service';
import { Producto } from '../../models/producto.models';
import { MedioPago, Ubigeo, MisDatosUsuario } from '../../models/configuracion.models';
import { Validaciones } from '../../utils/validaciones';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './checkout.component.html'
})
export class CheckoutComponent implements OnInit {
  readonly carritoService = inject(CarritoService);
  private readonly ventaService = inject(VentaService);
  private readonly configuracionService = inject(ConfiguracionService);
  private readonly productoService = inject(ProductoService);
  productos = signal<Producto[]>([]);
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // 1. Datos Personales
  dni = signal('');
  nombres = signal('');
  apellidoPaterno = signal('');
  apellidoMaterno = signal('');
  telefono = signal('');
  correo = signal('');

  // 2. Ubigeo y Dirección
  departamentos = signal<string[]>([]);
  provincias = signal<string[]>([]);
  distritos = signal<Ubigeo[]>([]);
  departamentoSel = signal<string>('');
  provinciaSel = signal<string>('');
  idUbigeoSel = signal<number | undefined>(undefined);
  direccion = signal('');
  referencia = signal('');

  // 3. Comprobante de Pago (Boleta / Factura)
  tipoComprobante = signal<'boleta' | 'factura'>('boleta');
  rucFactura = signal('');
  razonSocialFactura = signal('');

  // 4. Método de Pago y Campos Específicos
  mediosPago = signal<MedioPago[]>([]);
  medioPagoSeleccionado = signal<number | null>(null);

  celularPago = signal('');
  codOpPago = signal('');
  numTarjeta = signal('');
  fechaVence = signal('');
  cvvTarjeta = signal('');
  titularTarjeta = signal('');
  codOpTransferencia = signal('');
  bancoOrigen = signal('BCP');

  // Estado general y Snapshot para Comprobante
  procesando = signal(false);
  errorMensaje = signal<string | null>(null);
  exito = signal(false);
  idVentaCreada = signal<number | null>(null);
  fechaEmision = signal(new Date());
  itemsComprados = signal<any[]>([]);
  totalComprado = signal(0);
  subtotalComprado = signal(0);
  igvComprado = signal(0);

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/checkout' } });
      return;
    }

    this.carritoService.cargarCarrito();

    // Cargar medios de pago disponibles (100% en línea)
    this.configuracionService.getMediosPago().subscribe({
      next: (medios) => {
        const onlineMedios = (medios || []).filter(m => !m.nombre.toLowerCase().includes('efectivo'));
        this.mediosPago.set(onlineMedios);
        if (onlineMedios.length > 0) {
          this.medioPagoSeleccionado.set(onlineMedios[0].idMedioPago);
        }
      }
    });

    // Cargar departamentos de Ubigeo
    this.configuracionService.getDepartamentos().subscribe({
      next: (deps) => this.departamentos.set(deps)
    });

    // Cargar datos del usuario logueado para autocompletar
    this.configuracionService.getMisDatos().subscribe({
      next: (datos: MisDatosUsuario) => {
        if (datos) {
          if (datos.dni) this.dni.set(datos.dni);
          if (datos.nombres) this.nombres.set(datos.nombres);
          if (datos.apellidoPaterno) this.apellidoPaterno.set(datos.apellidoPaterno);
          if (datos.apellidoMaterno) this.apellidoMaterno.set(datos.apellidoMaterno);
          if (datos.telefono) this.telefono.set(datos.telefono);
          if (datos.correo) this.correo.set(datos.correo);
          if (datos.direccionExacta) this.direccion.set(datos.direccionExacta);
          if (datos.referencia) this.referencia.set(datos.referencia);

          if (datos.departamento) {
            this.departamentoSel.set(datos.departamento);
            this.configuracionService.getProvincias(datos.departamento).subscribe({
              next: (provs) => {
                this.provincias.set(provs);
                if (datos.provincia) {
                  this.provinciaSel.set(datos.provincia);
                  this.configuracionService.getDistritos(datos.departamento!, datos.provincia).subscribe({
                    next: (dists) => {
                      this.distritos.set(dists);
                      if (datos.idUbigeo) {
                        this.idUbigeoSel.set(datos.idUbigeo);
                      }
                    }
                  });
                }
              }
            });
          }
        }
      }
    });
  }

  obtenerImagenItem(item: any): string {
    if (item?.imagenUrl) return item.imagenUrl;
    const p = this.productos().find(prod => prod.idProducto === item?.idProducto);
    if (p && p.imagenes && p.imagenes.length > 0) {
      const principal = p.imagenes.find(img => img.esPrincipal) || p.imagenes[0];
      return principal?.urlImagen || '';
    }
    return '';
  }

  // Métodos de formateo de inputs
  onDniChange(val: string): void {
    this.dni.set(val.replace(/\D/g, '').slice(0, 8));
  }

  onTelefonoChange(val: string): void {
    this.telefono.set(val.replace(/\D/g, '').slice(0, 9));
  }

  onCelularPagoChange(val: string): void {
    this.celularPago.set(val.replace(/\D/g, '').slice(0, 9));
  }

  onCodOpChange(val: string): void {
    this.codOpPago.set(val.replace(/\D/g, '').slice(0, 6));
  }

  onNumTarjetaChange(val: string): void {
    this.numTarjeta.set(val.replace(/\D/g, '').slice(0, 16));
  }

  onCvvChange(val: string): void {
    this.cvvTarjeta.set(val.replace(/\D/g, '').slice(0, 4));
  }

  onFechaVenceChange(val: string): void {
    const raw = val.replace(/\D/g, '').slice(0, 4);
    if (raw.length >= 3) {
      this.fechaVence.set(raw.slice(0, 2) + '/' + raw.slice(2));
    } else {
      this.fechaVence.set(raw);
    }
  }

  cambioDepartamento(dep: string): void {
    this.departamentoSel.set(dep);
    this.provinciaSel.set('');
    this.idUbigeoSel.set(undefined);
    this.distritos.set([]);
    if (dep) {
      this.configuracionService.getProvincias(dep).subscribe(provs => this.provincias.set(provs));
    } else {
      this.provincias.set([]);
    }
  }

  cambioProvincia(prov: string): void {
    this.provinciaSel.set(prov);
    this.idUbigeoSel.set(undefined);
    if (prov && this.departamentoSel()) {
      this.configuracionService.getDistritos(this.departamentoSel(), prov).subscribe(dists => this.distritos.set(dists));
    } else {
      this.distritos.set([]);
    }
  }

  getNombreMedioSeleccionado(): string {
    const id = this.medioPagoSeleccionado();
    const medio = this.mediosPago().find(m => m.idMedioPago === id);
    return medio ? medio.nombre.toLowerCase() : '';
  }

  esYapeOPlin(): boolean {
    const n = this.getNombreMedioSeleccionado();
    return n.includes('yape') || n.includes('plin');
  }

  esTarjeta(): boolean {
    const n = this.getNombreMedioSeleccionado();
    return n.includes('tarjeta') || n.includes('credito') || n.includes('debito');
  }

  esTransferencia(): boolean {
    const n = this.getNombreMedioSeleccionado();
    return n.includes('transferencia');
  }

  confirmarCompra(): void {
    this.errorMensaje.set(null);

    // 1. Validar DNI
    const vDni = Validaciones.validarDni(this.dni());
    if (!vDni.valido) {
      this.errorMensaje.set(vDni.mensaje || 'DNI inválido');
      return;
    }

    // 2. Validar Nombres y Apellidos
    const vNombres = Validaciones.validarTextoNombre(this.nombres(), 'Los nombres');
    if (!vNombres.valido) {
      this.errorMensaje.set(vNombres.mensaje || 'Nombres inválidos');
      return;
    }

    const vApPat = Validaciones.validarTextoNombre(this.apellidoPaterno(), 'El apellido paterno');
    if (!vApPat.valido) {
      this.errorMensaje.set(vApPat.mensaje || 'Apellido paterno inválido');
      return;
    }

    const vApMat = Validaciones.validarTextoNombre(this.apellidoMaterno(), 'El apellido materno');
    if (!vApMat.valido) {
      this.errorMensaje.set(vApMat.mensaje || 'Apellido materno inválido');
      return;
    }

    // 3. Validar Comprobante (Boleta o Factura)
    if (this.tipoComprobante() === 'factura') {
      const vRuc = Validaciones.validarRuc(this.rucFactura());
      if (!vRuc.valido) {
        this.errorMensaje.set(vRuc.mensaje || 'RUC inválido para la Factura Electrónica');
        return;
      }
      if (!this.razonSocialFactura().trim()) {
        this.errorMensaje.set('Ingresa la Razón Social de la empresa para emitir la Factura');
        return;
      }
    }

    // 4. Validar Teléfono Celular (9 dígitos empezando en 9)
    const vTel = Validaciones.validarTelefono(this.telefono());
    if (!vTel.valido) {
      this.errorMensaje.set(vTel.mensaje || 'Teléfono celular inválido');
      return;
    }

    // 5. Validar Ubigeo
    if (!this.departamentoSel() || !this.provinciaSel() || !this.idUbigeoSel()) {
      this.errorMensaje.set('Debes seleccionar el departamento, provincia y distrito de entrega');
      return;
    }

    // 6. Validar Dirección (mínimo 10 caracteres)
    const vDir = Validaciones.validarDireccion(this.direccion());
    if (!vDir.valido) {
      this.errorMensaje.set(vDir.mensaje || 'Dirección de entrega inválida');
      return;
    }

    // 7. Validar Método de Pago
    if (!this.medioPagoSeleccionado()) {
      this.errorMensaje.set('Por favor selecciona un método de pago');
      return;
    }

    let codigoOpFinal = '';

    if (this.esYapeOPlin()) {
      if (!/^9\d{8}$/.test(this.celularPago().trim())) {
        this.errorMensaje.set('El número de celular de Yape/Plin debe tener 9 dígitos y empezar con 9');
        return;
      }
      if (!/^\d{6}$/.test(this.codOpPago().trim())) {
        this.errorMensaje.set('El código de operación de Yape/Plin debe tener 6 dígitos numéricos');
        return;
      }
      codigoOpFinal = this.codOpPago().trim();
    } else if (this.esTarjeta()) {
      if (!/^\d{16}$/.test(this.numTarjeta().trim())) {
        this.errorMensaje.set('El número de tarjeta debe tener exactamente 16 dígitos');
        return;
      }
      if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(this.fechaVence().trim())) {
        this.errorMensaje.set('La fecha de vencimiento debe tener formato MM/AA (ej. 12/28)');
        return;
      }
      if (!/^\d{3,4}$/.test(this.cvvTarjeta().trim())) {
        this.errorMensaje.set('El código CVV debe tener 3 o 4 dígitos numéricos');
        return;
      }
      if (!this.titularTarjeta().trim()) {
        this.errorMensaje.set('Ingresa el nombre del titular como figura en la tarjeta');
        return;
      }
      const raw = this.numTarjeta().trim();
      codigoOpFinal = 'TARJETA-****-' + raw.slice(-4);
    } else if (this.esTransferencia()) {
      if (!this.codOpTransferencia().trim()) {
        this.errorMensaje.set('Ingresa el número de operación bancaria de la transferencia');
        return;
      }
      codigoOpFinal = `${this.bancoOrigen()}-${this.codOpTransferencia().trim()}`;
    }

    // Guardar snapshot de los productos comprados antes de vaciar carrito
    this.itemsComprados.set([...this.carritoService.items()]);
    this.totalComprado.set(this.carritoService.total());
    this.subtotalComprado.set(this.carritoService.subtotal());
    this.igvComprado.set(this.carritoService.igv());
    this.fechaEmision.set(new Date());

    this.procesando.set(true);

    const dirCompleta = this.referencia().trim()
      ? `${this.direccion().trim().toUpperCase()} (REF: ${this.referencia().trim().toUpperCase()})`
      : this.direccion().trim().toUpperCase();

    this.ventaService.checkout({
      direccionEnvio: dirCompleta,
      referencia: this.referencia().trim().toUpperCase() || undefined,
      idUbigeo: this.idUbigeoSel(),
      telefono: this.telefono().trim(),
      numeroDocumento: this.tipoComprobante() === 'factura' ? this.rucFactura().trim() : this.dni().trim(),
      idMedioPago: this.medioPagoSeleccionado() || undefined,
      codigoOperacion: codigoOpFinal || undefined,
      celularPago: this.celularPago().trim() || undefined
    }).subscribe({
      next: (res) => {
        this.procesando.set(false);
        this.exito.set(true);
        this.idVentaCreada.set(res.idVenta);
        this.carritoService.cargarCarrito();
      },
      error: (err) => {
        this.procesando.set(false);
        this.errorMensaje.set(err.error?.mensaje || 'Error al procesar el pedido. Intenta nuevamente.');
      }
    });
  }

  getDistritoNombre(): string {
    const id = this.idUbigeoSel();
    const d = this.distritos().find(x => x.idUbigeo === id);
    return d ? d.distrito : '';
  }

  getNombreMedioPago(): string {
    const id = this.medioPagoSeleccionado();
    const m = this.mediosPago().find(x => x.idMedioPago === id);
    return m ? m.nombre : 'Medio no seleccionado';
  }

  imprimirComprobante(): void {
    if (typeof window !== 'undefined') {
      window.print();
    }
  }
}
