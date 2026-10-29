import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductoService } from '../../services/producto.service';
import { CarritoService } from '../../services/carrito.service';
import { AuthService } from '../../services/auth.service';
import { Producto, ProductoImagen } from '../../models/producto.models';

@Component({
  selector: 'app-producto-detalle',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './producto-detalle.component.html'
})
export class ProductoDetalleComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productoService = inject(ProductoService);
  private readonly carritoService = inject(CarritoService);
  readonly authService = inject(AuthService);

  producto = signal<Producto | null>(null);
  imagenes = signal<ProductoImagen[]>([]);
  imagenSeleccionada = signal<string>('');
  cantidad = signal<number>(1);
  cargando = signal<boolean>(true);
  agregando = signal<boolean>(false);
  agregadoExitoso = signal<boolean>(false);

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.cargarProducto(+id);
      }
    });
  }

  cargarProducto(id: number): void {
    this.cargando.set(true);
    this.productoService.getProductoById(id).subscribe({
      next: (prod) => {
        this.producto.set(prod);
        this.cargarImagenes(id, prod);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
      }
    });
  }

  cargarImagenes(id: number, prod: Producto): void {
    this.productoService.getImagenesProducto(id).subscribe({
      next: (imgs) => {
        this.imagenes.set(imgs);
        if (imgs.length > 0) {
          const principal = imgs.find(i => i.esPrincipal);
          this.imagenSeleccionada.set(principal ? principal.urlImagen : imgs[0].urlImagen);
        } else if (prod.imagenes && prod.imagenes.length > 0) {
          this.imagenSeleccionada.set(prod.imagenes[0].urlImagen);
        }
      }
    });
  }

  incrementar(): void {
    const prod = this.producto();
    if (prod && this.cantidad() < prod.stock) {
      this.cantidad.update(c => c + 1);
    }
  }

  decrementar(): void {
    if (this.cantidad() > 1) {
      this.cantidad.update(c => c - 1);
    }
  }

  agregarAlCarrito(): void {
    const prod = this.producto();
    if (!prod) return;

    if (!this.authService.isAuthenticated()) {
      alert('Debes iniciar sesión para agregar productos al carrito.');
      this.router.navigate(['/login']);
      return;
    }

    this.agregando.set(true);
    this.carritoService.agregarProducto(prod.idProducto, this.cantidad(), {
      nombre: prod.nombre,
      codigoSku: prod.codigoSku,
      precioVenta: prod.precioVenta,
      stock: prod.stock,
      imagenUrl: (prod.imagenes && prod.imagenes.length > 0) ? (prod.imagenes.find(i => i.esPrincipal)?.urlImagen || prod.imagenes[0].urlImagen) : ''
    }).subscribe({
      next: () => {
        this.agregando.set(false);
        this.agregadoExitoso.set(true);
        setTimeout(() => this.agregadoExitoso.set(false), 2500);
      },
      error: (err) => {
        this.agregando.set(false);
        alert(err.error?.mensaje || 'No se pudo agregar al carrito');
      }
    });
  }

  comprarAhora(): void {
    const prod = this.producto();
    if (!prod) return;

    if (!this.authService.isAuthenticated()) {
      alert('Debes iniciar sesión para realizar compras.');
      this.router.navigate(['/login']);
      return;
    }

    this.carritoService.agregarProducto(prod.idProducto, this.cantidad()).subscribe({
      next: () => {
        this.router.navigate(['/checkout']);
      },
      error: (err) => {
        alert(err.error?.mensaje || 'No se pudo procesar la compra');
      }
    });
  }
}
