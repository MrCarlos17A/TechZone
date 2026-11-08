import { TestBed } from '@angular/core/testing';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  it('debe crearse correctamente con lista vacia inicial', () => {
    expect(service).toBeTruthy();
    expect(service.toasts().length).toBe(0);
  });

  it('debe registrar un toast de exito', () => {
    service.exito('Guardado con exito', 'Exito');
    const items = service.toasts();

    expect(items.length).toBe(1);
    expect(items[0].tipo).toBe('exito');
    expect(items[0].mensaje).toBe('Guardado con exito');
    expect(items[0].titulo).toBe('Exito');
  });

  it('debe registrar un toast de error y permitir removerlo por id', () => {
    service.error('Fallo en la peticion', 'Error 500');
    expect(service.toasts().length).toBe(1);

    const toastId = service.toasts()[0].id;
    service.remover(toastId);

    expect(service.toasts().length).toBe(0);
  });
});
