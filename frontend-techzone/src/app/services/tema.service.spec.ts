import { TestBed } from '@angular/core/testing';
import { TemaService } from './tema.service';

describe('TemaService', () => {
  let service: TemaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TemaService);
  });

  it('debe crearse e iniciar con un tema valido', () => {
    expect(service).toBeTruthy();
    expect(['dark', 'light']).toContain(service.tema());
  });

  it('debe alternar entre dark y light al llamar toggleTema', () => {
    const temaInicial = service.tema();
    service.toggleTema();
    const temaSiguiente = service.tema();

    expect(temaSiguiente).not.toBe(temaInicial);

    service.toggleTema();
    expect(service.tema()).toBe(temaInicial);
  });
});

