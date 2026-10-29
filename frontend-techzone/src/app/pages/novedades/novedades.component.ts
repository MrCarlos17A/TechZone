import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductoService } from '../../services/producto.service';
import { Producto } from '../../models/producto.models';
import { ProductCardComponent } from '../../components/product-card/product-card.component';

@Component({
  selector: 'app-novedades',
  standalone: true,
  imports: [CommonModule, ProductCardComponent],
  template: `
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 transition-colors duration-300">
      <div class="border-b border-slate-200 dark:border-slate-800 pb-4">
        <span class="text-xs font-bold text-blue-600 dark:text-blue-400 uppercase tracking-widest">Lanzamientos</span>
        <h1 class="text-3xl font-extrabold text-slate-900 dark:text-white font-heading mt-1">Novedades & Ofertas Especiales</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">Los últimos componentes ingresados a nuestro almacén con precios de lanzamiento</p>
      </div>

      @if (cargando()) {
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          @for (i of [1,2,3,4]; track i) {
            <div class="glass-card rounded-2xl h-80 animate-pulse bg-slate-200 dark:bg-slate-900/50"></div>
          }
        </div>
      } @else if (productos().length > 0) {
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          @for (p of productos(); track p.idProducto) {
            <app-product-card [producto]="p" />
          }
        </div>
      } @else {
        <div class="text-center py-20 glass-panel rounded-3xl">
          <p class="text-slate-500 dark:text-slate-400">No hay novedades registradas en este momento.</p>
        </div>
      }
    </div>
  `
})
export class NovedadesComponent implements OnInit {
  private readonly productoService = inject(ProductoService);
  productos = signal<Producto[]>([]);
  cargando = signal(true);

  ngOnInit(): void {
    this.productoService.getDestacados().subscribe({
      next: (data) => {
        this.productos.set(data);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }
}
