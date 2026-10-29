import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Producto } from '../../models/producto.models';
import { CarritoService } from '../../services/carrito.service';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-card.component.html'
})
export class ProductCardComponent {
  @Input({ required: true }) producto!: Producto;

  private readonly carritoService = inject(CarritoService);
  readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);

  agregando = false;
  agregadoExitoso = false;

  get urlImagen(): string {
    if (this.producto.imagenes && this.producto.imagenes.length > 0) {
      const principal = this.producto.imagenes.find(img => img.esPrincipal);
      return principal ? principal.urlImagen : this.producto.imagenes[0].urlImagen;
    }
    return '';
  }

  agregarAlCarrito(event: Event): void {
    event.stopPropagation();
    event.preventDefault();

    if (!this.authService.isAuthenticated()) {
      this.toastService.advertencia('Inicia sesión para agregar productos a tu carrito');
      this.router.navigate(['/login']);
      return;
    }

    this.agregando = true;
    this.agregadoExitoso = true; // Feedback inmediato

    this.carritoService.agregarProducto(this.producto.idProducto, 1, {
      nombre: this.producto.nombre,
      codigoSku: this.producto.codigoSku,
      precioVenta: this.producto.precioVenta,
      stock: this.producto.stock,
      imagenUrl: this.urlImagen
    }).subscribe({
      next: () => {
        this.agregando = false;
        this.toastService.exito(`Se agregó "${this.producto.nombre}" al carrito`);
        setTimeout(() => this.agregadoExitoso = false, 2000);
      },
      error: (err) => {
        this.agregando = false;
        this.agregadoExitoso = false;
        this.toastService.error(err.error?.mensaje || 'No se pudo agregar el producto');
      }
    });
  }
}
