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
      sessionStorage.clear();
    }
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getAuthHeaders(): HttpHeaders {
    const t = this.token();
    if (t) {
      return new HttpHeaders({ Authorization: `Bearer ${t}` });
    }
    return new HttpHeaders();
  }

  private guardarSesion(resp: AuthResponse): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('techzone_user', JSON.stringify(resp));
    }
    this.currentUser.set(resp);
  }

  private obtenerSesionGuardada(): AuthResponse | null {
    if (typeof window === 'undefined') {
      return null;
    }
    try {
      const data = localStorage.getItem('techzone_user');
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  }
}
