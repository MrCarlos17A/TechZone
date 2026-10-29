import { Injectable, signal, effect } from '@angular/core';

export type TemaColor = 'dark' | 'light';

@Injectable({
  providedIn: 'root'
})
export class TemaService {
  private readonly STORAGE_KEY = 'techzone_theme';

  tema = signal<TemaColor>(this.obtenerTemaInicial());

  constructor() {
    this.aplicarTema(this.tema());

    effect(() => {
      const nuevoTema = this.tema();
      this.aplicarTema(nuevoTema);
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.setItem(this.STORAGE_KEY, nuevoTema);
      }
    });
  }

  toggleTema(): void {
    this.tema.update(actual => actual === 'dark' ? 'light' : 'dark');
  }

  esModoOscuro(): boolean {
    return this.tema() === 'dark';
  }

  private obtenerTemaInicial(): TemaColor {
    if (typeof window === 'undefined') return 'dark';

    const guardado = localStorage.getItem(this.STORAGE_KEY) as TemaColor | null;
    if (guardado === 'dark' || guardado === 'light') {
      return guardado;
    }

    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
      return 'light';
    }

    return 'dark';
  }

  private aplicarTema(tema: TemaColor): void {
    if (typeof document === 'undefined') return;

    const root = document.documentElement;
    if (tema === 'dark') {
      root.classList.add('dark');
      root.classList.remove('light');
    } else {
      root.classList.add('light');
      root.classList.remove('dark');
    }
  }
}

