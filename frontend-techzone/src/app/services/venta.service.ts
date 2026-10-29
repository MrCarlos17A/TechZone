import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SolicitudCheckout, VentaCompleta, VentaResumen } from '../models/venta.models';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class VentaService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly baseUrl = environment.apiUrl;

  checkout(datos: SolicitudCheckout): Observable<any> {
    return this.http.post(`${this.baseUrl}/checkout`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getMisPedidos(): Observable<VentaResumen[]> {
    return this.http.get<VentaResumen[]>(`${this.baseUrl}/mis-pedidos`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getMiPedidoDetalle(id: number): Observable<VentaCompleta> {
    return this.http.get<VentaCompleta>(`${this.baseUrl}/mis-pedidos/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Admin ---
  adminGetVentas(): Observable<VentaResumen[]> {
    return this.http.get<VentaResumen[]>(`${this.baseUrl}/admin/ventas`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  adminGetVentaDetalle(id: number): Observable<VentaCompleta> {
    return this.http.get<VentaCompleta>(`${this.baseUrl}/admin/ventas/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  adminConfirmarVenta(id: number): Observable<VentaCompleta> {
    return this.http.patch<VentaCompleta>(`${this.baseUrl}/admin/ventas/${id}/confirmar`, {}, {
      headers: this.authService.getAuthHeaders()
    });
  }

  adminCancelarVenta(id: number): Observable<VentaCompleta> {
    return this.http.patch<VentaCompleta>(`${this.baseUrl}/admin/ventas/${id}/cancelar`, {}, {
      headers: this.authService.getAuthHeaders()
    });
  }

  adminActualizarEstadoVenta(id: number, estado: string): Observable<VentaCompleta> {
    return this.http.patch<VentaCompleta>(`${this.baseUrl}/admin/ventas/${id}/estado`, { estado }, {
      headers: this.authService.getAuthHeaders()
    });
  }
}
