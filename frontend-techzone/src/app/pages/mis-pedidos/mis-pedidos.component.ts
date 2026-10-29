import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { VentaService } from '../../services/venta.service';
import { AuthService } from '../../services/auth.service';
import { ConfiguracionService } from '../../services/configuracion.service';
import { VentaCompleta, VentaResumen } from '../../models/venta.models';

@Component({
  selector: 'app-mis-pedidos',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mis-pedidos.component.html'
})
export class MisPedidosComponent implements OnInit {
  private readonly ventaService = inject(VentaService);
  readonly authService = inject(AuthService);
  readonly configuracionService = inject(ConfiguracionService);
  private readonly router = inject(Router);

  pedidos = signal<VentaResumen[]>([]);
  pedidoSeleccionado = signal<VentaCompleta | null>(null);
  comprobanteSeleccionado = signal<VentaCompleta | null>(null);
  cargando = signal(true);
  cargandoDetalle = signal(false);
  cargandoComprobante = signal(false);

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/mis-pedidos' } });
      return;
    }
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.cargando.set(true);
    this.ventaService.getMisPedidos().subscribe({
      next: (data) => {
        this.pedidos.set(data);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  verDetalle(id: number): void {
    this.cargandoDetalle.set(true);
    this.ventaService.getMiPedidoDetalle(id).subscribe({
      next: (data) => {
        this.pedidoSeleccionado.set(data);
        this.cargandoDetalle.set(false);
      },
      error: () => this.cargandoDetalle.set(false)
    });
  }

  cerrarModal(): void {
    this.pedidoSeleccionado.set(null);
  }

  verComprobante(id: number): void {
    if (this.pedidoSeleccionado()?.idVenta === id) {
      this.comprobanteSeleccionado.set(this.pedidoSeleccionado());
      return;
    }
    this.cargandoComprobante.set(true);
    this.ventaService.getMiPedidoDetalle(id).subscribe({
      next: (data) => {
        this.comprobanteSeleccionado.set(data);
        this.cargandoComprobante.set(false);
      },
      error: () => this.cargandoComprobante.set(false)
    });
  }

  cerrarComprobante(): void {
    this.comprobanteSeleccionado.set(null);
  }

  imprimirComprobante(): void {
    if (typeof window !== 'undefined') {
      window.print();
    }
  }

  esFactura(c: VentaCompleta): boolean {
    const doc = c.clienteDocumento || '';
    return doc.length === 11 || (c.clienteNombre || '').toUpperCase().includes('S.A.C') || (c.clienteNombre || '').toUpperCase().includes('S.R.L');
  }

  generarHashSimulado(c: VentaCompleta): string {
    const str = `${c.idVenta}|${c.total}|${c.fechaVenta}`;
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) - hash) + str.charCodeAt(i);
      hash |= 0;
    }
    const hex = Math.abs(hash).toString(16).padStart(8, '0');
    return `${hex}e8a49c71b02f5a6b8c9d0e1f2a3b4c5d`.slice(0, 28);
  }
}
