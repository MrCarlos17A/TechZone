import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CarritoService } from '../../services/carrito.service';
import { ConfiguracionService } from '../../services/configuracion.service';
import { TemaService } from '../../services/tema.service';
import { NavbarComponent } from './navbar.component';

describe('NavbarComponent: navegacion movil', () => {
  const authenticated = signal(false);
  const staff = signal(false);

  beforeEach(async () => {
    authenticated.set(false);
    staff.set(false);
    await TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: {
          isAuthenticated: authenticated,
          isStaff: staff,
          esAdmin: () => false,
          currentUser: () => ({ nombre: 'Cliente', correo: 'cliente@ejemplo.com' }),
          logout: () => authenticated.set(false)
        } },
        { provide: CarritoService, useValue: { cantidadTotal: () => 2 } },
        { provide: ConfiguracionService, useValue: { configuracion: signal(null) } },
        { provide: TemaService, useValue: { esModoOscuro: () => true } }
      ]
    }).compileComponents();
  });

  function createFixture() {
    const fixture = TestBed.createComponent(NavbarComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('ofrece ingreso y registro dentro del menu y comunica su apertura', () => {
    const fixture = createFixture();
    const root: HTMLElement = fixture.nativeElement;
    const toggle = root.querySelector<HTMLButtonElement>('button[aria-controls="mobile-navigation"]')!;
    expect(toggle.getAttribute('aria-expanded')).toBe('false');
    toggle.click();
    fixture.detectChanges();
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    expect(root.querySelector('#mobile-navigation a[href="/login"]')).toBeTruthy();
    expect(root.querySelector('#mobile-navigation a[href="/registro"]')).toBeTruthy();
  });

  it('cierra el menu con Escape y devuelve el foco al boton', () => {
    const fixture = createFixture();
    fixture.componentInstance.toggleMenu();
    fixture.detectChanges();
    const root: HTMLElement = fixture.nativeElement;
    root.querySelector('#mobile-navigation')!.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true })
    );
    fixture.detectChanges();
    expect(fixture.componentInstance.menuAbierto()).toBe(false);
    expect(document.activeElement).toBe(root.querySelector('button[aria-controls="mobile-navigation"]'));
  });

  it('muestra opciones de cuenta y administracion segun la sesion', () => {
    authenticated.set(true);
    const fixture = createFixture();
    fixture.componentInstance.toggleMenu();
    fixture.detectChanges();
    const root: HTMLElement = fixture.nativeElement;
    expect(root.querySelector('#mobile-navigation a[href="/perfil"]')).toBeTruthy();
    expect(root.querySelector('#mobile-navigation a[href="/mis-pedidos"]')).toBeTruthy();
    expect(root.querySelector('#mobile-navigation a[href="/login"]')).toBeNull();
    expect(root.querySelector('#mobile-navigation a[href="/admin"]')).toBeNull();
    staff.set(true);
    fixture.detectChanges();
    expect(root.querySelector('#mobile-navigation a[href="/admin"]')).toBeTruthy();
  });

  it('cierra el menu al buscar y conserva la consulta', () => {
    const fixture = createFixture();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.componentInstance.menuAbierto.set(true);
    fixture.componentInstance.busqueda.set('  teclado  ');
    fixture.componentInstance.buscar();
    expect(fixture.componentInstance.menuAbierto()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/productos'], { queryParams: { q: 'teclado' } });
  });

  it('identifica los controles de tema y carrito sin depender de iconos', () => {
    const fixture = createFixture();
    const root: HTMLElement = fixture.nativeElement;
    expect(root.querySelector('button[aria-label="Cambiar a modo claro"]')).toBeTruthy();
    expect(root.querySelector('a[href="/carrito"]')?.getAttribute('aria-label')).toBe('Carrito, 2 productos');
  });
});
