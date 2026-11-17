import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';
import { FooterComponent } from './components/footer/footer.component';
import { ToastContainerComponent } from './components/toast-container/toast-container.component';
import { TemaService } from './services/tema.service';
import { InactividadService } from './services/inactividad.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, ToastContainerComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  readonly router = inject(Router);
  readonly temaService = inject(TemaService);
  readonly inactividadService = inject(InactividadService);

  esRutaAdmin(): boolean {
    return this.router.url.startsWith('/admin');
  }
}
