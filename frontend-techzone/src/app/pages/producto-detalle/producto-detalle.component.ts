import { Component, OnInit, inject, signal, computed } from '@angular/core';
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
  indiceImagenActual = signal<number>(0);
  cantidad = signal<number>(1);
  cargando = signal<boolean>(true);
  agregando = signal<boolean>(false);
  agregadoExitoso = signal<boolean>(false);

  // Computado de la imagen seleccionada activa
  imagenActual = computed<ProductoImagen | null>(() => {
    const list = this.imagenes();
    const idx = this.indiceImagenActual();
    if (list.length > 0 && idx >= 0 && idx < list.length) {
      return list[idx];
    }
    return null;
  });

  // URL activa para visualizacion en el visor principal
  imagenSeleccionadaUrl = computed<string>(() => {
    const actual = this.imagenActual();
    if (actual?.urlImagen) return actual.urlImagen;
    const prod = this.producto();
    if (prod?.imagenes && prod.imagenes.length > 0) {
      return prod.imagenes[0].urlImagen;
    }
    return '';
  });

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
        const ordenadas = [...imgs].sort((a, b) => (a.orden ?? 0) - (b.orden ?? 0));
        this.imagenes.set(ordenadas);
        if (ordenadas.length > 0) {
          const idxPrincipal = ordenadas.findIndex(i => i.esPrincipal);
          this.indiceImagenActual.set(idxPrincipal >= 0 ? idxPrincipal : 0);
        } else if (prod.imagenes && prod.imagenes.length > 0) {
          const ordenadasProd = [...prod.imagenes].sort((a, b) => (a.orden ?? 0) - (b.orden ?? 0));
          this.imagenes.set(ordenadasProd);
          this.indiceImagenActual.set(0);
        }
      },
      error: () => {
        if (prod.imagenes && prod.imagenes.length > 0) {
          const ordenadasProd = [...prod.imagenes].sort((a, b) => (a.orden ?? 0) - (b.orden ?? 0));
          this.imagenes.set(ordenadasProd);
          this.indiceImagenActual.set(0);
        }
      }
    });
  }

  seleccionarImagen(index: number): void {
    if (index >= 0 && index < this.imagenes().length) {
      this.indiceImagenActual.set(index);
    }
  }

  siguienteImagen(): void {
    const total = this.imagenes().length;
    if (total <= 1) return;
    this.indiceImagenActual.update(idx => (idx + 1) % total);
  }

  anteriorImagen(): void {
    const total = this.imagenes().length;
    if (total <= 1) return;
    this.indiceImagenActual.update(idx => (idx - 1 + total) % total);
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
      alert('Debes iniciar sesion para agregar productos al carrito.');
      this.router.navigate(['/login']);
      return;
    }

    this.agregando.set(true);
    const imagenUrlParaCarrito = this.imagenSeleccionadaUrl() ||
      ((prod.imagenes && prod.imagenes.length > 0) ? (prod.imagenes.find(i => i.esPrincipal)?.urlImagen || prod.imagenes[0].urlImagen) : '');

    this.carritoService.agregarProducto(prod.idProducto, this.cantidad(), {
      nombre: prod.nombre,
      codigoSku: prod.codigoSku,
      precioVenta: prod.precioVenta,
      stock: prod.stock,
      imagenUrl: imagenUrlParaCarrito
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
      alert('Debes iniciar sesion para realizar compras.');
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
