import { Injectable, inject, signal, computed, effect } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, catchError, of, Observable, forkJoin } from 'rxjs';
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
      // 1. Cargar datos locales iniciales segun la sesion actual
      const inicial = this.leerLocal();
      if (inicial) {
        this.carrito.set(inicial);
      }

      // 2. Sincronizacion reactiva con la sesion del usuario
      effect(() => {
        const user = this.authService.currentUser();
        if (!user) {
          // Si no hay usuario (invitado o tras cerrar sesion):
          // Cargar exclusivamente el carrito de visitante anonimo, nunca el del usuario que salio
          const guestCart = this.leerLocal('techzone_cart_guest');
          this.carrito.set(guestCart);
        } else {
          // Si el usuario inicio sesion:
          // Verificar si habia productos en el carrito temporal de invitado antes de loguearse
          const guestCart = this.leerLocal('techzone_cart_guest');
          if (guestCart && guestCart.items && guestCart.items.length > 0) {
            this.fusionarCarritoInvitado(guestCart);
          } else {
            // Cargar el carrito propio de este usuario desde la BD
            this.cargarCarrito();
          }
        }
      });
    }
  }

  limpiar(): void {
    this.carrito.set(null);
    this.persistirLocal(null);
  }

  /**
   * Genera una clave unica por usuario para aislar completamente los carritos
   * y evitar que usuarios en una misma computadora compartan productos.
   */
  private getStorageKey(): string {
    const user = this.authService.currentUser();
    if (user) {
      const id = user.idUsuario || user.correo;
      return `techzone_cart_user_${id}`;
    }
    return 'techzone_cart_guest';
  }

  private persistirLocal(res: CarritoResumen | null): void {
    if (typeof window === 'undefined') return;
    try {
      const key = this.getStorageKey();
      if (res && res.items && res.items.length > 0) {
        localStorage.setItem(key, JSON.stringify(res));
      } else {
        localStorage.removeItem(key);
      }
    } catch {}
  }

  private leerLocal(claveFija?: string): CarritoResumen | null {
    if (typeof window === 'undefined') return null;
    try {
      const key = claveFija || this.getStorageKey();
      const data = localStorage.getItem(key);
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  }

  private fusionarCarritoInvitado(guestCart: CarritoResumen): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('techzone_cart_guest');
    }

    if (!guestCart.items || guestCart.items.length === 0) {
      this.cargarCarrito();
      return;
    }

    // Subir cada producto del invitado a la cuenta del usuario en Supabase
    const peticiones = guestCart.items.map(it =>
      this.http.post<CarritoResumen>(
        `${this.apiUrl}/items`,
        { idProducto: it.idProducto, cantidad: it.cantidad },
        { headers: this.authService.getAuthHeaders() }
      ).pipe(catchError(() => of(null)))
    );

    forkJoin(peticiones).subscribe({
      next: () => this.cargarCarrito(),
      error: () => this.cargarCarrito()
    });
  }

  private enriquecerYGuardarCarrito(res: CarritoResumen | null): void {
    if (!res || !res.items || res.items.length === 0) {
      this.carrito.set(res);
      this.persistirLocal(res);
      return;
    }

    this.carrito.set(res);
    this.persistirLocal(res);

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

        const enriquecido: CarritoResumen = {
          ...res,
          items: itemsActualizados
        };

        this.carrito.set(enriquecido);
        this.persistirLocal(enriquecido);
      },
      error: () => {}
    });
  }

  cargarCarrito(): void {
    if (!this.authService.isAuthenticated()) {
      const guest = this.leerLocal('techzone_cart_guest');
      this.carrito.set(guest);
      return;
    }
    this.http.get<CarritoResumen>(this.apiUrl, { headers: this.authService.getAuthHeaders() }).pipe(
      catchError(() => of(this.leerLocal()))
    ).subscribe((res) => {
      this.enriquecerYGuardarCarrito(res);
    });
  }

  agregarProducto(idProducto: number, cantidad = 1, itemSnapshot?: Partial<CarritoItem>): Observable<CarritoResumen | null> {
    const estadoPrevio = this.carrito();
    const itemsCopia = estadoPrevio ? [...estadoPrevio.items] : [];
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

    const nuevoCarrito: CarritoResumen = {
      idCarrito: estadoPrevio?.idCarrito || Date.now(),
      cantidadItems: itemsCopia.length,
      subtotal: nuevoSubtotal,
      igv: nuevoIgv,
      total: nuevoTotal,
      items: itemsCopia
    };

    // Actualizacion optimista inmediata en memoria y persistencia local
    this.carrito.set(nuevoCarrito);
    this.persistirLocal(nuevoCarrito);

    // Si el usuario no esta autenticado, se mantiene en almacenamiento de invitado
    if (!this.authService.isAuthenticated()) {
      return of(nuevoCarrito);
    }

    // Si esta autenticado, sincronizar en tiempo real con Supabase
    return this.http.post<CarritoResumen>(
      `${this.apiUrl}/items`,
      { idProducto, cantidad },
      { headers: this.authService.getAuthHeaders() }
    ).pipe(
      tap((res) => this.enriquecerYGuardarCarrito(res)),
      catchError(() => {
        return of(nuevoCarrito);
      })
    );
  }

  actualizarCantidad(idDetalle: number, cantidad: number): Observable<CarritoResumen> {
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

      const actualizado: CarritoResumen = {
        ...estadoPrevio,
        items: itemsCopia,
        subtotal: nuevoSubtotal,
        igv: nuevoIgv,
        total: nuevoTotal
      };

      this.carrito.set(actualizado);
      this.persistirLocal(actualizado);

      if (!this.authService.isAuthenticated()) {
        return of(actualizado);
      }
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
    const estadoPrevio = this.carrito();
    if (estadoPrevio) {
      const itemsCopia = estadoPrevio.items.filter(it => it.idDetalle !== idDetalle);
      const nuevoSubtotal = itemsCopia.reduce((sum, it) => sum + it.subtotal, 0);
      const nuevoIgv = nuevoSubtotal * 0.18;
      const nuevoTotal = nuevoSubtotal + nuevoIgv;

      const actualizado: CarritoResumen = {
        ...estadoPrevio,
        items: itemsCopia,
        cantidadItems: itemsCopia.length,
        subtotal: nuevoSubtotal,
        igv: nuevoIgv,
        total: nuevoTotal
      };

      this.carrito.set(actualizado);
      this.persistirLocal(actualizado);

      if (!this.authService.isAuthenticated()) {
        return of(actualizado);
      }
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
    this.carrito.set(null);
    this.persistirLocal(null);

    if (!this.authService.isAuthenticated()) {
      return of(null);
    }

    return this.http.delete(this.apiUrl, { headers: this.authService.getAuthHeaders() }).pipe(
      catchError(() => of(null))
    );
  }
}
