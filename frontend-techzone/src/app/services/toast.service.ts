import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: string;
  tipo: 'exito' | 'error' | 'advertencia' | 'info';
  titulo?: string;
  mensaje: string;
  duracionMs?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  readonly toasts = signal<ToastMessage[]>([]);

  mostrar(tipo: 'exito' | 'error' | 'advertencia' | 'info', mensaje: string, titulo?: string, duracionMs = 3500): void {
    const id = Math.random().toString(36).substring(2, 9);
    const nuevoToast: ToastMessage = { id, tipo, mensaje, titulo, duracionMs };

    this.toasts.update(list => [...list, nuevoToast]);

    if (duracionMs > 0) {
      setTimeout(() => {
        this.remover(id);
      }, duracionMs);
    }
  }

  exito(mensaje: string, titulo = 'Operación Exitosa'): void {
    this.mostrar('exito', mensaje, titulo);
  }

  error(mensaje: string, titulo = 'Error'): void {
    this.mostrar('error', mensaje, titulo, 4500);
  }

  advertencia(mensaje: string, titulo = 'Atención'): void {
    this.mostrar('advertencia', mensaje, titulo);
  }

  info(mensaje: string, titulo = 'Información'): void {
    this.mostrar('info', mensaje, titulo);
  }

  remover(id: string): void {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }
}
