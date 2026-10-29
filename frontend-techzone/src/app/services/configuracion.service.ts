import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, shareReplay, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ConfiguracionSitio,
  DniConsultaResponse,
  MedioPago,
  TipoDocumento,
  Ubigeo
} from '../models/configuracion.models';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ConfiguracionService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly baseUrl = environment.apiUrl;

  readonly configuracion = signal<ConfiguracionSitio | null>(null);

  constructor() {
    this.cargarConfiguracion();
  }

  private configCache$: Observable<ConfiguracionSitio> | null = null;

  limpiarCache(): void {
    this.configCache$ = null;
  }

  cargarConfiguracion(): void {
    this.getConfiguracion().subscribe({
      next: (cfg) => this.configuracion.set(cfg),
      error: () => {}
    });
  }

  getConfiguracion(): Observable<ConfiguracionSitio> {
    if (!this.configCache$) {
      this.configCache$ = this.http.get<ConfiguracionSitio>(`${this.baseUrl}/configuracion`).pipe(
        tap((cfg) => this.configuracion.set(cfg)),
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }
    return this.configCache$;
  }

  getMediosPago(): Observable<MedioPago[]> {
    return this.http.get<MedioPago[]>(`${this.baseUrl}/medios-pago`);
  }

  getTiposDocumento(): Observable<TipoDocumento[]> {
    return this.http.get<TipoDocumento[]>(`${this.baseUrl}/tipos-documento`);
  }

  getUbigeos(): Observable<Ubigeo[]> {
    return this.http.get<Ubigeo[]>(`${this.baseUrl}/ubigeos`);
  }

  getDepartamentos(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/ubigeos/departamentos`);
  }

  getProvincias(departamento: string): Observable<string[]> {
    const params = new HttpParams().set('departamento', departamento);
    return this.http.get<string[]>(`${this.baseUrl}/ubigeos/provincias`, { params });
  }

  getDistritos(departamento: string, provincia: string): Observable<Ubigeo[]> {
    const params = new HttpParams()
      .set('departamento', departamento)
      .set('provincia', provincia);
    return this.http.get<Ubigeo[]>(`${this.baseUrl}/ubigeos/distritos`, { params });
  }

  consultarDocumento(tipoDocumento: number | undefined, numero: string): Observable<DniConsultaResponse> {
    let params = new HttpParams().set('numero', numero);
    if (tipoDocumento) {
      params = params.set('tipoDocumento', tipoDocumento.toString());
    }
    return this.http.get<DniConsultaResponse>(`${this.baseUrl}/consulta-documento`, { params });
  }

  getMisDatos(): Observable<any> {
    return this.http.get(`${this.baseUrl}/mis-datos`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  adminGuardarConfiguracion(config: ConfiguracionSitio): Observable<ConfiguracionSitio> {
    return this.http.put<ConfiguracionSitio>(`${this.baseUrl}/admin/configuracion`, config, {
      headers: this.authService.getAuthHeaders()
    }).pipe(
      tap((cfg) => {
        this.limpiarCache();
        this.configuracion.set(cfg);
      })
    );
  }

  adminSubirImagenesConfiguracion(formData: FormData): Observable<ConfiguracionSitio> {
    return this.http.post<ConfiguracionSitio>(`${this.baseUrl}/admin/configuracion/imagenes`, formData, {
      headers: this.authService.getAuthHeaders()
    }).pipe(
      tap((cfg) => {
        this.limpiarCache();
        this.configuracion.set(cfg);
      })
    );
  }
}
