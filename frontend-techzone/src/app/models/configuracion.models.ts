export interface ConfiguracionSitio {
  idConfig?: number;
  nombreSitio?: string;
  logoUrl?: string;
  tagline?: string;
  email?: string;
  telefono?: string;
  horario?: string;
  whatsapp?: string;
  whatsappTexto?: string;
  mostrarTiktok?: boolean;
  tiktokUrl?: string;
  mostrarFacebook?: boolean;
  facebookUrl?: string;
  mostrarInstagram?: boolean;
  instagramUrl?: string;
  mostrarYoutube?: boolean;
  youtubeUrl?: string;
  copyright?: string;
  libroReclamacionesImg?: string;
  libroReclamacionesUrl?: string;
}

export interface Ubigeo {
  idUbigeo: number;
  departamento: string;
  provincia: string;
  distrito: string;
}

export interface MedioPago {
  idMedioPago: number;
  nombre: string;
  instrucciones?: string;
  vigente?: boolean;
}

export interface TipoDocumento {
  idTipoDocumento: number;
  nombre: string;
  vigente?: boolean;
}

export interface DniConsultaResponse {
  ok: boolean;
  mensaje?: string;
  origen?: string;
  idPersona?: number;
  yaRegistrado?: boolean;
  datos?: {
    numero: string;
    nombres: string;
    apellidoPaterno: string;
    apellidoMaterno: string;
    telefono?: string;
    direccion?: string;
    referencia?: string;
    idUbigeo?: number;
  };
}

export interface MisDatosUsuario {
  ok: boolean;
  dni?: string;
  nombres?: string;
  apellidoPaterno?: string;
  apellidoMaterno?: string;
  telefono?: string;
  correo?: string;
  idUbigeo?: number;
  departamento?: string;
  provincia?: string;
  distrito?: string;
  direccionExacta?: string;
  referencia?: string;
}
