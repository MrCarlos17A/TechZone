import { Injectable, inject, signal, computed, effect } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, catchError, of, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CarritoResumen, CarritoItem } from '../models/carrito.models';
import { ProductoService } from './producto.service';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly productoService = inject(ProductoService);
  private readonly apiUrl = `${environment.apiUrl}/carrito`;

  readonly carrito = signal<CarritoResumen | null>(null);
  readonly items = computed(() => this.carrito()?.items || []);
  readonly cantidadTotal = computed(() => {
    const c = this.carrito();
    if (!c || !c.items) return 0;
    return c.items.reduce((acc, curr) => acc + curr.cantidad, 0);
  });
  readonly subtotal = computed(() => this.carrito()?.subtotal || 0);
  readonly igv = computed(() => this.carrito()?.igv || 0);
  readonly total = computed(() => this.carrito()?.total || 0);

  constructor() {
    if (typeof window !== 'undefined') {
      // Sincronización reactiva con la sesión del usuario
      effect(() => {
        const user = this.authService.currentUser();
        if (!user) {
          // Si cierra sesión, limpiar inmediatamente el carrito en memoria
          this.carrito.set(null);
        } else {
          // Si inicia sesión o cambia de usuario, recargar el carrito de su propia cuenta
          this.cargarCarrito();
        }
      });
    }
  }

  limpiar(): void {
    this.carrito.set(null);
  }

  private enriquecerYGuardarCarrito(res: CarritoResumen | null): void {
    if (!res || !res.items || res.items.length === 0) {
      this.carrito.set(res);
      return;
    }

    // Establecer primero para reactividad inmediata
    this.carrito.set(res);

    // Enriquecer con ProductoService para obtener URLs de imágenes
    this.productoService.getProductos().subscribe({
      next: (productos) => {
        const prodMap = new Map<number, string>();
        for (const p of productos) {
          if (p.imagenes && p.imagenes.length > 0) {
            const principal = p.imagenes.find(img => img.esPrincipal) || p.imagenes[0];
            if (principal?.urlImagen) {
              prodMap.set(p.idProducto, principal.urlImagen);
            }
          }
        }

        const itemsActualizados = res.items.map(it => ({
          ...it,
          imagenUrl: it.imagenUrl || prodMap.get(it.idProducto) || ''
        }));

        this.carrito.set({
          ...res,
          items: itemsActualizados
        });
      },
      error: () => {}
    });
  }

  cargarCarrito(): void {
    if (!this.authService.isAuthenticated()) {
      this.carrito.set(null);
      return;
    }
    this.http.get<CarritoResumen>(this.apiUrl, { headers: this.authService.getAuthHeaders() }).pipe(
      catchError(() => of(null))
    ).subscribe((res) => {
      this.enriquecerYGuardarCarrito(res);
    });
  }

  agregarProducto(idProducto: number, cantidad = 1, itemSnapshot?: Partial<CarritoItem>): Observable<CarritoResumen | null> {
    if (!this.authService.isAuthenticated()) {
      return of(null);
    }

    // ⚡ ACTUALIZACIÓN OPTIMISTA INMEDIATA (0 ms)
    const estadoPrevio = this.carrito();
    if (estadoPrevio) {
      const itemsCopia = [...estadoPrevio.items];
      const existente = itemsCopia.find(it => it.idProducto === idProducto);
      if (existente) {
        existente.cantidad += cantidad;
        existente.subtotal = existente.precioVenta * existente.cantidad;
      } else if (itemSnapshot) {
        itemsCopia.push({
          idDetalle: Date.now(),
          idProducto,
          nombre: itemSnapshot.nombre || 'Producto',
          codigoSku: itemSnapshot.codigoSku || '',
          precioVenta: itemSnapshot.precioVenta || 0,
          cantidad,
          stock: itemSnapshot.stock || 10,
          subtotal: (itemSnapshot.precioVenta || 0) * cantidad,
          imagenUrl: itemSnapshot.imagenUrl || ''
        });
      }
      const nuevoSubtotal = itemsCopia.reduce((sum, it) => sum + it.subtotal, 0);
      const nuevoIgv = nuevoSubtotal * 0.18;
      const nuevoTotal = nuevoSubtotal + nuevoIgv;

      this.carrito.set({
        ...estadoPrevio,
        items: itemsCopia,
        cantidadItems: itemsCopia.length,
        subtotal: nuevoSubtotal,
        igv: nuevoIgv,
        total: nuevoTotal
      });
    }

    return this.http.post<CarritoResumen>(
      `${this.apiUrl}/items`,
      { idProducto, cantidad },
      { headers: this.authService.getAuthHeaders() }
    ).pipe(
      tap((res) => this.enriquecerYGuardarCarrito(res)),
      catchError((err) => {
        if (estadoPrevio) this.carrito.set(estadoPrevio);
        return of(null);
      })
    );
  }

  actualizarCantidad(idDetalle: number, cantidad: number): Observable<CarritoResumen> {
    // ⚡ ACTUALIZACIÓN OPTIMISTA INMEDIATA (0 ms)
    const estadoPrevio = this.carrito();
    if (estadoPrevio) {
      const itemsCopia = estadoPrevio.items.map(it => {
        if (it.idDetalle === idDetalle) {
          return {
            ...it,
            cantidad,
            subtotal: it.precioVenta * cantidad
          };
        }
        return it;
      });
      const nuevoSubtotal = itemsCopia.reduce((sum, it) => sum + it.subtotal, 0);
      const nuevoIgv = nuevoSubtotal * 0.18;
      const nuevoTotal = nuevoSubtotal + nuevoIgv;

      this.carrito.set({
        ...estadoPrevio,
        items: itemsCopia,
        subtotal: nuevoSubtotal,
        igv: nuevoIgv,
        total: nuevoTotal
      });
    }

    return this.http.put<CarritoResumen>(
      `${this.apiUrl}/items/${idDetalle}`,
      { cantidad },
      { headers: this.authService.getAuthHeaders() }
    ).pipe(
      tap((res) => this.enriquecerYGuardarCarrito(res)),
      catchError((err) => {
        if (estadoPrevio) this.carrito.set(estadoPrevio);
        throw err;
      })
    );
  }

  eliminarItem(idDetalle: number): Observable<CarritoResumen> {
    // ⚡ ACTUALIZACIÓN OPTIMISTA INMEDIATA (0 ms)
    const estadoPrevio = this.carrito();
    if (estadoPrevio) {
      const itemsCopia = estadoPrevio.items.filter(it => it.idDetalle !== idDetalle);
      const nuevoSubtotal = itemsCopia.reduce((sum, it) => sum + it.subtotal, 0);
      const nuevoIgv = nuevoSubtotal * 0.18;
      const nuevoTotal = nuevoSubtotal + nuevoIgv;

      this.carrito.set({
        ...estadoPrevio,
        items: itemsCopia,
        cantidadItems: itemsCopia.length,
        subtotal: nuevoSubtotal,
        igv: nuevoIgv,
        total: nuevoTotal
      });
    }

    return this.http.delete<CarritoResumen>(
      `${this.apiUrl}/items/${idDetalle}`,
      { headers: this.authService.getAuthHeaders() }
    ).pipe(
      tap((res) => this.enriquecerYGuardarCarrito(res)),
      catchError((err) => {
        if (estadoPrevio) this.carrito.set(estadoPrevio);
        throw err;
      })
    );
  }

  vaciar(): Observable<any> {
    return this.http.delete(this.apiUrl, { headers: this.authService.getAuthHeaders() }).pipe(
      tap(() => this.carrito.set(null))
    );
  }
}
