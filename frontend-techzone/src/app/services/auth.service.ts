import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, UsuarioLoginReq, UsuarioRegistroReq } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  readonly currentUser = signal<AuthResponse | null>(this.obtenerSesionGuardada());
  readonly token = computed(() => this.currentUser()?.token || null);
  readonly isAuthenticated = computed(() => !!this.currentUser());

  constructor() {
    // Si hay sesion en el navegador al arrancar la aplicacion, verificar activamente con el servidor
    if (typeof window !== 'undefined' && this.isAuthenticated()) {
      this.validarSesionConServidor();
    }
  }

  readonly esAdmin = computed(() => {
    const user = this.currentUser();
    if (!user || !user.roles) return false;
    return user.roles.some(r => {
      const lower = (r || '').toLowerCase();
      return lower.includes('admin') || lower.includes('gerente');
    });
  });

  readonly esColaborador = computed(() => {
    const user = this.currentUser();
    if (!user || !user.roles) return false;
    return user.roles.some(r => {
      const lower = (r || '').toLowerCase();
      return lower.includes('empleado') || lower.includes('colaborador') || lower.includes('trabajador');
    });
  });

  readonly isStaff = computed(() => {
    const user = this.currentUser();
    if (!user) return false;
    if (user.esStaff) return true;
    if (user.roles && Array.isArray(user.roles)) {
      return user.roles.some(r => {
        const lower = (r || '').toLowerCase();
        return lower.includes('admin') || lower.includes('staff') || lower.includes('gerente') || lower.includes('empleado') || lower.includes('colaborador') || lower.includes('trabajador');
      });
    }
    return false;
  });

  hasRole(nombreRol: string): boolean {
    const user = this.currentUser();
    if (!user || !user.roles) return false;
    const target = nombreRol.toLowerCase();
    return user.roles.some(r => {
      const lower = r.toLowerCase();
      return lower === target || lower === `role_${target}`;
    });
  }

  login(credenciales: UsuarioLoginReq): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credenciales).pipe(
      tap((resp) => this.guardarSesion(resp))
    );
  }

  registro(datos: UsuarioRegistroReq): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/registro`, datos).pipe(
      tap((resp) => this.guardarSesion(resp))
    );
  }

  logout(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('techzone_user');
      sessionStorage.removeItem('techzone_user');
      sessionStorage.clear();
    }
    this.currentUser.set(null);
    if (this.router.url !== '/login') {
      this.router.navigate(['/login']);
    }
  }

  getAuthHeaders(): HttpHeaders {
    const t = this.token();
    if (t) {
      return new HttpHeaders({ Authorization: `Bearer ${t}` });
    }
    return new HttpHeaders();
  }

  validarSesionConServidor(): void {
    const headers = this.getAuthHeaders();
    this.http.get(`${this.apiUrl}/me`, { headers }).subscribe({
      next: () => {
        // Token valido en la instancia actual del servidor
      },
      error: () => {
        // Servidor apagado, reiniciado o token de instancia previa: cerrar sesion inmediatamente
        this.logout();
      }
    });
  }

  private guardarSesion(resp: AuthResponse): void {
    if (typeof window !== 'undefined') {
      // Guardar en sessionStorage para que la sesion expire de forma segura al cerrar la pestana o navegador
      sessionStorage.setItem('techzone_user', JSON.stringify(resp));
      // Purgar residuos previos en localStorage
      localStorage.removeItem('techzone_user');
    }
    this.currentUser.set(resp);
  }

  private obtenerSesionGuardada(): AuthResponse | null {
    if (typeof window === 'undefined') {
      return null;
    }
    try {
      // Priorizar almacenamiento de sesion volatil
      const sessionData = sessionStorage.getItem('techzone_user');
      if (sessionData) {
        return JSON.parse(sessionData);
      }
      // Si habia datos remanentes en localStorage, migrarlos a sessionStorage y limpiar localStorage
      const localData = localStorage.getItem('techzone_user');
      if (localData) {
        localStorage.removeItem('techzone_user');
        sessionStorage.setItem('techzone_user', localData);
        return JSON.parse(localData);
      }
      return null;
    } catch {
      return null;
    }
  }
}
