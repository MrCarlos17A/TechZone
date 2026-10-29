import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

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
      // Si el backend responde 401 y no es la ruta de login o registro, expiró la sesión
      if (error.status === 401 && !req.url.includes('/auth/login') && !req.url.includes('/auth/registro')) {
        authService.logout();
        router.navigate(['/login'], { queryParams: { sesionExpirada: 'true' } });
      }
      return throwError(() => error);
    })
  );
};
