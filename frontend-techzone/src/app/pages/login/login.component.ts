import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ConfiguracionService } from '../../services/configuracion.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  readonly authService = inject(AuthService);
  readonly configuracionService = inject(ConfiguracionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  username = signal('');
  password = signal('');
  mostrarPassword = signal(false);
  cargando = signal(false);
  error = signal<string | null>(null);

  cerrarSesionActual(): void {
    this.authService.logout();
    this.username.set('');
    this.password.set('');
    this.error.set(null);
  }

  toggleMostrarPassword(): void {
    this.mostrarPassword.update(v => !v);
  }

  ngOnInit(): void {
    if (this.route.snapshot.queryParams['sesionExpirada'] === 'true') {
      this.error.set('Tu sesión ha expirado por inactividad. Por favor inicia sesión nuevamente.');
    }
  }

  onSubmit(): void {
    if (!this.username() || !this.password()) {
      this.error.set('Por favor completa todos los campos');
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.authService.login({
      username: this.username().trim(),
      password: this.password()
    }).subscribe({
      next: (resp) => {
        this.cargando.set(false);
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || (resp.esStaff ? '/admin' : '/');
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err.error?.mensaje || 'Credenciales inválidas. Verifica tu correo y contraseña.');
      }
    });
  }
}
