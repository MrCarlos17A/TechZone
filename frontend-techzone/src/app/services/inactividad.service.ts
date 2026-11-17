import { Injectable, inject, signal, effect, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class InactividadService {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly ngZone = inject(NgZone);

  // 25 minutos de inactividad total para el personal administrativo
  private readonly TIEMPO_TOTAL_MS = 25 * 60 * 1000;
  // Alerta preventiva a los 23 minutos (2 minutos de gracia)
  private readonly TIEMPO_AVISO_MS = 23 * 60 * 1000;
  // Intervalo de verificacion en segundo plano (cada 5 segundos)
  private readonly INTERVALO_CHEQUEO_MS = 5000;

  private ultimoMovimientoTimestamp = Date.now();
  private intervaloId: any = null;
  private listenersRegistrados = false;

  readonly advertenciaActiva = signal(false);
  readonly segundosRestantes = signal(120);

  constructor() {
    if (typeof window !== 'undefined') {
      // Monitorear reactivamente si el usuario activo es Staff / Administrador
      effect(() => {
        const esStaff = this.authService.isStaff();
        if (esStaff) {
          this.iniciarMonitoreo();
        } else {
          this.detenerMonitoreo();
        }
      });
    }
  }

  iniciarMonitoreo(): void {
    if (typeof window === 'undefined') return;
    this.ultimoMovimientoTimestamp = Date.now();
    this.advertenciaActiva.set(false);
    this.segundosRestantes.set(120);

    this.registrarEventosUsuario();

    if (!this.intervaloId) {
      // Ejecutar fuera de Angular Zone para evitar disparar ciclos de Change Detection innecesarios
      this.ngZone.runOutsideAngular(() => {
        this.intervaloId = setInterval(() => {
          this.verificarInactividad();
        }, this.INTERVALO_CHEQUEO_MS);
      });
    }
  }

  detenerMonitoreo(): void {
    if (this.intervaloId) {
      clearInterval(this.intervaloId);
      this.intervaloId = null;
    }
    this.advertenciaActiva.set(false);
    this.segundosRestantes.set(120);
  }

  registrarActividad(): void {
    this.ultimoMovimientoTimestamp = Date.now();
    if (this.advertenciaActiva()) {
      this.ngZone.run(() => {
        this.advertenciaActiva.set(false);
        this.segundosRestantes.set(120);
      });
    }
  }

  continuarSesion(): void {
    this.registrarActividad();
  }

  private verificarInactividad(): void {
    if (!this.authService.isStaff()) {
      this.detenerMonitoreo();
      return;
    }

    const transcurrido = Date.now() - this.ultimoMovimientoTimestamp;

    if (transcurrido >= this.TIEMPO_TOTAL_MS) {
      // Cumplidos los 25 minutos sin interaccion: cerrar sesion inmediatamente
      this.detenerMonitoreo();
      this.ngZone.run(() => {
        this.authService.logout();
        this.router.navigate(['/login'], { queryParams: { inactividad: 'true' } });
      });
    } else if (transcurrido >= this.TIEMPO_AVISO_MS) {
      // Ventana de aviso (ultimos 120 segundos)
      const restantes = Math.max(0, Math.ceil((this.TIEMPO_TOTAL_MS - transcurrido) / 1000));
      this.ngZone.run(() => {
        this.advertenciaActiva.set(true);
        this.segundosRestantes.set(restantes);
      });
    } else if (this.advertenciaActiva()) {
      this.ngZone.run(() => {
        this.advertenciaActiva.set(false);
        this.segundosRestantes.set(120);
      });
    }
  }

  private registrarEventosUsuario(): void {
    if (this.listenersRegistrados || typeof window === 'undefined') return;

    const eventos = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart'];
    const handler = () => {
      // Throttle: registrar como maximo una vez cada 2 segundos para ahorrar recursos
      const ahora = Date.now();
      if (ahora - this.ultimoMovimientoTimestamp > 2000) {
        this.registrarActividad();
      }
    };

    eventos.forEach(evt => {
      window.addEventListener(evt, handler, { passive: true });
    });

    this.listenersRegistrados = true;
  }
}
