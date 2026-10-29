import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastMessage } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed top-5 right-5 z-[100] flex flex-col gap-2.5 max-w-sm w-full pointer-events-none">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="pointer-events-auto flex items-start gap-3 p-4 rounded-2xl shadow-xl border backdrop-blur-xl transition-all duration-300 animate-slideDown bg-white/95 dark:bg-slate-900/95 shadow-slate-900/10 dark:shadow-black/50"
          [ngClass]="{
            'border-emerald-300 dark:border-emerald-500/40 text-slate-900 dark:text-slate-100': toast.tipo === 'exito',
            'border-red-300 dark:border-red-500/40 text-slate-900 dark:text-slate-100': toast.tipo === 'error',
            'border-amber-300 dark:border-amber-500/40 text-slate-900 dark:text-slate-100': toast.tipo === 'advertencia',
            'border-blue-300 dark:border-blue-500/40 text-slate-900 dark:text-slate-100': toast.tipo === 'info'
          }">
          
          <!-- Icon -->
          <div class="flex-shrink-0 mt-0.5">
            @switch (toast.tipo) {
              @case ('exito') {
                <div class="w-7 h-7 rounded-full bg-emerald-100 dark:bg-emerald-500/20 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-bold">
                  <span class="material-symbols-outlined text-base">check_circle</span>
                </div>
              }
              @case ('error') {
                <div class="w-7 h-7 rounded-full bg-red-100 dark:bg-red-500/20 text-red-600 dark:text-red-400 flex items-center justify-center font-bold">
                  <span class="material-symbols-outlined text-base">error</span>
                </div>
              }
              @case ('advertencia') {
                <div class="w-7 h-7 rounded-full bg-amber-100 dark:bg-amber-500/20 text-amber-700 dark:text-amber-400 flex items-center justify-center font-bold">
                  <span class="material-symbols-outlined text-base">warning</span>
                </div>
              }
              @default {
                <div class="w-7 h-7 rounded-full bg-blue-100 dark:bg-blue-500/20 text-blue-600 dark:text-blue-400 flex items-center justify-center font-bold">
                  <span class="material-symbols-outlined text-base">info</span>
                </div>
              }
            }
          </div>

          <!-- Content -->
          <div class="flex-1 min-w-0">
            @if (toast.titulo) {
              <h4 class="text-xs font-bold text-slate-900 dark:text-white tracking-wide mb-0.5 font-heading">{{ toast.titulo }}</h4>
            }
            <p class="text-xs text-slate-600 dark:text-slate-300 leading-relaxed font-medium">{{ toast.mensaje }}</p>
          </div>

          <!-- Close -->
          <button
            (click)="toastService.remover(toast.id)"
            class="text-slate-400 hover:text-slate-700 dark:hover:text-white p-1 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer">
            <span class="material-symbols-outlined text-base">close</span>
          </button>
        </div>
      }
    </div>
  `
})
export class ToastContainerComponent {
  readonly toastService = inject(ToastService);
}
