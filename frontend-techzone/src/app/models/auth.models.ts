export interface UsuarioLoginReq {
  username: string;
  password: string;
}

export interface UsuarioRegistroReq {
  nombres: string;
  apellidos: string;
  apellidoMaterno?: string;
  correo: string;
  telefono?: string;
  password: string;
  idTipoDocumento?: number;
  numeroDocumento?: string;
  fechaNacimiento?: string;
  direccion?: string;
  referencia?: string;
  idUbigeo?: number;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  idUsuario: number;
  correo: string;
  rol?: string;
  roles: string[];
  esAdmin?: boolean;
  esStaff: boolean;
  esAdministrativo?: boolean;
  permisos?: Record<string, string[]> | string | null;
  nombre: string;
}
