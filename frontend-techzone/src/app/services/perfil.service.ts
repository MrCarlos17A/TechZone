import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface PerfilData {
  idUsuario?: number;
  correo?: string;
  rol?: string;
  idPersona?: number;
  nombres?: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
  idTipoDocumento?: number;
  numeroDocumento?: string;
  telefono?: string;
  direccion?: string;
  referencia?: string;
  idUbigeo?: number;
  departamento?: string;
  provincia?: string;
  distrito?: string;
  fechaNacimiento?: string;
}

export interface ActualizarPerfilDto {
  nombres?: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
  idTipoDocumento?: number;
  numeroDocumento?: string;
  telefono?: string;
  direccion?: string;
  referencia?: string;
  idUbigeo?: number;
  fechaNacimiento?: string;
  passwordActual?: string;
  passwordNuevo?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PerfilService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly baseUrl = environment.apiUrl;

  obtenerPerfil(): Observable<PerfilData> {
    return this.http.get<PerfilData>(`${this.baseUrl}/perfil`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  actualizarPerfil(dto: ActualizarPerfilDto): Observable<any> {
    return this.http.put(`${this.baseUrl}/perfil`, dto, {
      headers: this.authService.getAuthHeaders()
    });
  }
}
