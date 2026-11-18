import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ConfiguracionService } from '../services/configuracion.service';
import { LoginComponent } from './login/login.component';
import { RegistroComponent } from './registro/registro.component';

for (const component of [LoginComponent, RegistroComponent]) {
  describe(`${component.name}: accesibilidad del formulario`, () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [component],
        providers: [
          provideRouter([]),
          {
            provide: AuthService,
            useValue: { isAuthenticated: () => false }
          },
          {
            provide: ConfiguracionService,
            useValue: {
              configuracion: signal(null),
              getTiposDocumento: () => of([]),
              getDepartamentos: () => of([])
            }
          }
        ]
      }).compileComponents();
    });

    function createFixture() {
      const fixture = TestBed.createComponent<LoginComponent | RegistroComponent>(component);
      fixture.detectChanges();
      return fixture;
    }

    it('asocia cada campo con una etiqueta accesible', () => {
      const fixture = createFixture();
      const root: HTMLElement = fixture.nativeElement;
      const controls = root.querySelectorAll<HTMLInputElement | HTMLSelectElement>('input, select');

      expect(controls.length).toBeGreaterThan(0);
      for (const control of controls) {
        const hasLabel = !!control.labels?.length || !!control.getAttribute('aria-label');
        expect(hasLabel, `Campo sin etiqueta: ${control.name}`).toBe(true);
      }
      const form = root.querySelector('form')!;
      expect(root.querySelector(`#${form.getAttribute('aria-labelledby')}`)?.tagName).toBe('H1');
    });

    it('permite mostrar y ocultar contraseñas sin perder el valor', async () => {
      const fixture = createFixture();
      await fixture.whenStable();
      const root: HTMLElement = fixture.nativeElement;
      const buttons = root.querySelectorAll<HTMLButtonElement>('button[aria-controls]');

      expect(buttons.length).toBe(component === LoginComponent ? 1 : 2);
      for (const button of buttons) {
        const input = root.querySelector<HTMLInputElement>(`#${button.getAttribute('aria-controls')}`)!;
        input.value = 'ClaveDePrueba2026!';
        input.dispatchEvent(new Event('input'));
        fixture.detectChanges();
        await fixture.whenStable();

        expect(button.tabIndex).toBe(0);
        expect(button.getAttribute('aria-label')).toBeTruthy();
        expect(button.getAttribute('aria-pressed')).toBe('false');
        button.click();
        fixture.detectChanges();
        expect(input.type).toBe('text');
        expect(input.value).toBe('ClaveDePrueba2026!');
        expect(button.getAttribute('aria-pressed')).toBe('true');
        button.click();
        fixture.detectChanges();
        expect(input.type).toBe('password');
        expect(input.value).toBe('ClaveDePrueba2026!');
      }
    });

    it('expone los errores y el estado de carga a tecnologias de asistencia', () => {
      const fixture = createFixture();
      fixture.componentInstance.error.set('Mensaje de prueba');
      fixture.componentInstance.cargando.set(true);
      fixture.detectChanges();
      const root: HTMLElement = fixture.nativeElement;

      expect(root.querySelector('[role="alert"]')?.textContent).toContain('Mensaje de prueba');
      expect(root.querySelector('form')?.getAttribute('aria-busy')).toBe('true');
      expect(root.querySelector<HTMLButtonElement>('button[type="submit"]')?.disabled).toBe(true);
    });

    it('configura el autocompletado de contraseñas segun el formulario', () => {
      const fixture = createFixture();
      const root: HTMLElement = fixture.nativeElement;
      for (const input of root.querySelectorAll('input[type="password"]')) {
        expect(input.getAttribute('autocomplete')).toBe(
          component === LoginComponent ? 'current-password' : 'new-password'
        );
      }
    });
  });
}
