import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../services/producto.service';
import { Marca } from '../../models/producto.models';

@Component({
  selector: 'app-marcas',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 transition-colors duration-300">
      <div class="border-b border-slate-200 dark:border-slate-800 pb-4">
        <span class="text-xs font-bold text-blue-600 dark:text-blue-400 uppercase tracking-widest">Alianzas Oficiales</span>
        <h1 class="text-3xl font-extrabold text-slate-900 dark:text-white font-heading mt-1">Nuestras Marcas</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">Distribuidores autorizados de las firmas más prestigiosas del ecosistema gamer</p>
      </div>

      <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
        @for (m of marcas(); track m.idMarca) {
          <a
            [routerLink]="['/productos']"
            [queryParams]="{ marca: m.idMarca }"
            class="glass-panel p-6 rounded-2xl flex flex-col items-center justify-center hover:border-blue-500/50 hover:bg-slate-100/70 dark:hover:bg-slate-900 transition-all group h-36 shadow-sm">
            <div class="w-24 h-14 flex items-center justify-center mb-2">
              @if (m.logoUrl) {
                <img [src]="m.logoUrl" [alt]="m.nombre" class="max-h-12 max-w-full object-contain group-hover:scale-110 transition-all filter brightness-95 group-hover:brightness-110" />
              } @else {
                <span class="material-symbols-outlined text-3xl text-blue-500/70 group-hover:scale-110 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-all">verified</span>
              }
            </div>
            <span class="text-sm font-bold text-slate-800 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors text-center">{{ m.nombre }}</span>
          </a>
        }
      </div>
    </div>
  `
})
export class MarcasComponent implements OnInit {
  private readonly productoService = inject(ProductoService);
  marcas = signal<Marca[]>([]);

  ngOnInit(): void {
    this.productoService.getMarcas().subscribe({
      next: (m) => this.marcas.set(m)
    });
  }
}
