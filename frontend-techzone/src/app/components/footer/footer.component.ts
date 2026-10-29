import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ConfiguracionService } from '../../services/configuracion.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './footer.component.html'
})
export class FooterComponent {
  readonly configuracionService = inject(ConfiguracionService);

  /**
   * Determina la URL de destino o el comportamiento seguro.
   * Si no hay URL configurada o está vacía, puede redirigir a la home genérica de la red
   * o permanecer en la página sin error.
   */
  obtenerHrefRed(url?: string, homeGenerica: string = 'javascript:void(0)'): string {
    const link = url?.trim();
    if (!link || link === '#' || link === 'javascript:void(0)') {
      return homeGenerica;
    }
    return link.startsWith('http') ? link : `https://${link}`;
  }

  esEnlaceExterno(url?: string): boolean {
    const link = url?.trim();
    return !!(link && link !== '#' && link !== 'javascript:void(0)');
  }

  onRedClick(event: MouseEvent, url?: string, homeGenerica?: string): void {
    const link = url?.trim();
    // Si la URL es '#' o vacía sin home específica, prevenimos navegación para permanecer en la misma página
    if (!link || link === '#' || link === 'javascript:void(0)') {
      if (!homeGenerica || homeGenerica === 'javascript:void(0)') {
        event.preventDefault();
      }
    }
  }
}
