import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito.service';
import { ConfiguracionService } from '../../services/configuracion.service';
import { TemaService } from '../../services/tema.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent {
  readonly authService = inject(AuthService);
  readonly carritoService = inject(CarritoService);
  readonly configuracionService = inject(ConfiguracionService);
  readonly temaService = inject(TemaService);
  private readonly router = inject(Router);

  busqueda = signal('');
  menuAbierto = signal(false);
  usuarioDropdownAbierto = signal(false);

  buscar(): void {
    const q = this.busqueda().trim();
    if (q) {
      this.router.navigate(['/productos'], { queryParams: { q } });
    }
  }

  toggleMenu(): void {
    this.menuAbierto.update(v => !v);
  }

  toggleUsuarioDropdown(): void {
    this.usuarioDropdownAbierto.update(v => !v);
  }

  cerrarSesion(): void {
    this.usuarioDropdownAbierto.set(false);
    this.authService.logout();
  }
}
