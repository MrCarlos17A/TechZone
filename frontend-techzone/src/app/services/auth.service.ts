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
    // Sincronizar en segundo plano de manera no destructiva con el servidor si hay sesion activa
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
    // Bloquear incondicionalmente a Cliente y Usuario sin rol administrador
    if (user.roles && Array.isArray(user.roles)) {
      const esClienteOUsuario = user.roles.some(r => {
        const u = (r || '').toUpperCase();
        return u === 'ROLE_CLIENTE' || u === 'CLIENTE' || u === 'ROLE_USUARIO' || u === 'USUARIO';
      });
      const esAdmin = user.roles.some(r => (r || '').toUpperCase().includes('ADMIN'));
      if (esClienteOUsuario && !esAdmin) {
        return false;
      }
    }
    if (user.esAdministrativo !== undefined) {
      return !!user.esAdministrativo;
    }
    if (user.esStaff !== undefined) {
      return !!user.esStaff;
    }
    return false;
  });

  tienePermiso(modulo: string, accion: 'ver' | 'crear' | 'editar' | 'eliminar' = 'ver'): boolean {
    const user = this.currentUser();
    if (!user) return false;
    if (this.esAdmin()) return true;

    // Obtener matriz de permisos
    let permisosObj: Record<string, string[]> | null = null;
    if (user.permisos) {
      if (typeof user.permisos === 'string') {
        try {
          permisosObj = JSON.parse(user.permisos);
        } catch {
          permisosObj = null;
        }
      } else if (typeof user.permisos === 'object') {
        permisosObj = user.permisos as Record<string, string[]>;
      }
    }

    if (!permisosObj) {
      // Si no tiene matriz explícita pero es staff, permitir solo lectura
      return accion === 'ver' && this.isStaff();
    }

    const accionesModulo = permisosObj[modulo.toLowerCase()];
    if (!accionesModulo || !Array.isArray(accionesModulo)) {
      return false;
    }

    return accionesModulo.includes(accion.toLowerCase());
  }

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
      try {
        localStorage.removeItem('techzone_user');
        sessionStorage.removeItem('techzone_user');
      } catch {}
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
    const t = this.token();
    if (!t) return;
    if (this.isTokenExpired(t)) {
      this.logout();
      return;
    }

    const headers = this.getAuthHeaders();
    this.http.get<any>(`${this.apiUrl}/me`, { headers }).subscribe({
      next: (info) => {
        if (info && this.currentUser()) {
          const actual = this.currentUser()!;
          const actualizado: AuthResponse = {
            ...actual,
            nombre: info.nombre || actual.nombre,
            roles: info.roles || actual.roles,
            esStaff: info.esStaff !== undefined ? info.esStaff : actual.esStaff,
            idUsuario: info.idUsuario || actual.idUsuario,
            correo: info.correo || actual.correo
          };
          this.guardarSesion(actualizado);
        }
      },
      error: (err) => {
        // Solo cerrar sesion si el backend confirma explicitamente 401 (token revocado o expirado)
        if (err?.status === 401) {
          this.logout();
        }
      }
    });
  }

  private isTokenExpired(token: string | null): boolean {
    if (!token) return true;
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return true;
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = (typeof window !== 'undefined' && window.atob)
        ? decodeURIComponent(window.atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''))
        : '';
      if (!jsonPayload) return false;
      const payload = JSON.parse(jsonPayload);
      if (!payload.exp) return false;
      return (Date.now() / 1000) >= (payload.exp - 5);
    } catch {
      return false;
    }
  }

  private guardarSesion(resp: AuthResponse): void {
    if (typeof window !== 'undefined') {
      try {
        const serializado = JSON.stringify(resp);
        localStorage.setItem('techzone_user', serializado);
        sessionStorage.setItem('techzone_user', serializado);
      } catch {}
    }
    this.currentUser.set(resp);
  }

  private obtenerSesionGuardada(): AuthResponse | null {
    if (typeof window === 'undefined') {
      return null;
    }
    try {
      const raw = localStorage.getItem('techzone_user') || sessionStorage.getItem('techzone_user');
      if (!raw) return null;
      const data: AuthResponse = JSON.parse(raw);
      if (!data || !data.token) return null;
      if (this.isTokenExpired(data.token)) {
        try {
          localStorage.removeItem('techzone_user');
          sessionStorage.removeItem('techzone_user');
        } catch {}
        return null;
      }
      return data;
    } catch {
      return null;
    }
  }
}
