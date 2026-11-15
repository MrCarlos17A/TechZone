import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../services/producto.service';
import { Producto } from '../../models/producto.models';
import { ProductCardComponent } from '../../components/product-card/product-card.component';

@Component({
  selector: 'app-novedades',
  standalone: true,
  imports: [CommonModule, RouterLink, ProductCardComponent],
  template: `
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 transition-colors duration-300">
      <!-- Encabezado con estadísticas visuales -->
      <div class="border-b border-slate-200 dark:border-slate-800 pb-5 flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <span class="text-xs font-black text-blue-600 dark:text-blue-400 uppercase tracking-widest flex items-center gap-1.5">
            <span class="material-symbols-outlined text-sm">local_fire_department</span>
            Lanzamientos & Últimos Ingresos
          </span>
          <h1 class="text-3xl font-extrabold text-slate-900 dark:text-white font-heading mt-1">Novedades & Ofertas Especiales</h1>
          <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Los últimos componentes ingresados a nuestro catálogo y lanzamientos oficiales seleccionados
          </p>
        </div>

        @if (!cargando() && productos().length > 0) {
          <div class="flex items-center gap-2">
            <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-blue-50 dark:bg-blue-950/60 border border-blue-200/60 dark:border-blue-900/40 text-xs font-bold text-blue-700 dark:text-blue-300 shadow-2xs">
              <span class="material-symbols-outlined text-sm text-blue-500">inventory_2</span>
              {{ productos().length }} Novedades en Catálogo
            </span>
            @if (totalNovedadesManuales() > 0) {
              <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-purple-50 dark:bg-purple-950/60 border border-purple-200/60 dark:border-purple-900/40 text-xs font-bold text-purple-700 dark:text-purple-300 shadow-2xs">
                <span class="material-symbols-outlined text-sm text-purple-500">star</span>
                {{ totalNovedadesManuales() }} Lanzamiento Oficial
              </span>
            }
          </div>
        }
      </div>

      <!-- Cuadrícula de Productos -->
      @if (cargando()) {
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          @for (i of [1,2,3,4,5,6,7,8]; track i) {
            <div class="glass-card rounded-2xl h-80 animate-pulse bg-slate-200 dark:bg-slate-900/50"></div>
          }
        </div>
      } @else if (productos().length > 0) {
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          @for (p of productosPaginados(); track p.idProducto) {
            <div class="relative group">
              @if (p.novedad) {
                <div class="absolute -top-2.5 -left-2 z-20 pointer-events-none">
                  <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-500/20 border border-white/20">
                    <span class="material-symbols-outlined text-[11px]">star</span> Lanzamiento
                  </span>
                </div>
              }
              <app-product-card [producto]="p" />
            </div>
          }
        </div>

        <!-- Paginación si hay más de 12 productos -->
        @if (totalPaginas() > 1) {
          <div class="flex justify-center items-center gap-2 pt-6 border-t border-slate-200 dark:border-slate-800">
            <button
              (click)="cambiarPagina(paginaActual() - 1)"
              [disabled]="paginaActual() === 1"
              class="px-3.5 py-2 rounded-xl text-xs font-bold border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">chevron_left</span> Anterior
            </button>
            <span class="text-xs font-semibold text-slate-600 dark:text-slate-400 px-3">
              Página <strong class="text-blue-600 dark:text-blue-400 font-black">{{ paginaActual() }}</strong> de {{ totalPaginas() }}
            </span>
            <button
              (click)="cambiarPagina(paginaActual() + 1)"
              [disabled]="paginaActual() === totalPaginas()"
              class="px-3.5 py-2 rounded-xl text-xs font-bold border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer flex items-center gap-1">
              Siguiente <span class="material-symbols-outlined text-sm">chevron_right</span>
            </button>
          </div>
        }
      } @else {
        <div class="text-center py-20 glass-panel rounded-3xl border border-slate-200 dark:border-slate-800">
          <span class="material-symbols-outlined text-5xl text-slate-400">inventory_2</span>
          <h2 class="text-lg font-bold text-slate-800 dark:text-slate-200 mt-2">Próximamente nuevos lanzamientos</h2>
          <p class="text-slate-500 dark:text-slate-400 text-xs mt-1">Estamos preparando los próximos ingresos y ofertas especiales para ti.</p>
          <a routerLink="/productos" class="inline-flex items-center gap-1.5 mt-4 px-4 py-2 rounded-xl text-xs font-bold bg-blue-600 text-white hover:bg-blue-700 transition-colors">
            Explorar Catálogo Completo
          </a>
        </div>
      }
    </div>
  `
})
export class NovedadesComponent implements OnInit {
  private readonly productoService = inject(ProductoService);
  productos = signal<Producto[]>([]);
  cargando = signal(true);

  readonly porPagina = 12;
  paginaActual = signal(1);

  totalPaginas = computed(() => Math.max(1, Math.ceil(this.productos().length / this.porPagina)));

  productosPaginados = computed(() => {
    const start = (this.paginaActual() - 1) * this.porPagina;
    return this.productos().slice(start, start + this.porPagina);
  });

  totalNovedadesManuales = computed(() => this.productos().filter(p => !!p.novedad).length);

  ngOnInit(): void {
    this.productoService.getNovedades(true).subscribe({
      next: (data) => {
        this.productos.set(data || []);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  cambiarPagina(nuevaPagina: number): void {
    if (nuevaPagina >= 1 && nuevaPagina <= this.totalPaginas()) {
      this.paginaActual.set(nuevaPagina);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
}
