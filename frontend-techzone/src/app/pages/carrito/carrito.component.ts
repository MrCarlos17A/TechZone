import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CarritoService } from '../../services/carrito.service';
import { AuthService } from '../../services/auth.service';
import { ProductoService } from '../../services/producto.service';
import { Producto } from '../../models/producto.models';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './carrito.component.html'
})
export class CarritoComponent implements OnInit {
  readonly carritoService = inject(CarritoService);
  readonly authService = inject(AuthService);
  private readonly productoService = inject(ProductoService);
  private readonly router = inject(Router);

  productos = signal<Producto[]>([]);

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/carrito' } });
      return;
    }
    this.carritoService.cargarCarrito();
    this.productoService.getProductos().subscribe({
      next: (prods) => this.productos.set(prods),
      error: () => {}
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

  cambiarCantidad(idDetalle: number, delta: number, cantidadActual: number): void {
    const nueva = cantidadActual + delta;
    if (nueva <= 0) {
      this.eliminar(idDetalle);
    } else {
      this.carritoService.actualizarCantidad(idDetalle, nueva).subscribe();
    }
  }

  eliminar(idDetalle: number): void {
    this.carritoService.eliminarItem(idDetalle).subscribe();
  }

  vaciar(): void {
    if (confirm('¿Deseas vaciar todos los productos del carrito?')) {
      this.carritoService.vaciar().subscribe();
    }
  }
}
