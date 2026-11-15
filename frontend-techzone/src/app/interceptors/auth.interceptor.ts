import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

function isTokenExpired(token: string | null): boolean {
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
    // Si la expiración en segundos ya se superó (con margen de 5 segundos)
    return (Date.now() / 1000) >= (payload.exp - 5);
  } catch {
    return false;
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  let request = req;
  const token = authService.token();

  // Adjuntar automáticamente token Bearer si la petición es hacia nuestra API de backend
  if (token && req.url.startsWith(environment.apiUrl)) {
    request = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si el backend responde 401 y no es la ruta de login o registro
      if (error.status === 401 && !req.url.includes('/auth/login') && !req.url.includes('/auth/registro')) {
        // Solo cerrar sesión si el token realmente expiró o no existe
        if (!token || isTokenExpired(token)) {
          authService.logout();
          router.navigate(['/login'], { queryParams: { sesionExpirada: 'true' } });
        }
      }
      return throwError(() => error);
    })
  );
};
