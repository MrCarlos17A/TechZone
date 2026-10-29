import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface PersonaAdmin {
  idPersona: number;
  nombres: string;
  apellidoPaterno: string;
  apellidoMaterno?: string;
  numeroDocumento?: string;
  telefono?: string;
  direccion?: string;
  referencia?: string;
  tipoDocumento?: { idTipoDocumento: number; descripcion: string };
  fechaNacimiento?: string;
  fechaRegistro?: string;
  vigente?: boolean;
}

export interface ClienteAdmin {
  idCliente: number;
  persona?: PersonaAdmin;
  nombreCompleto?: string;
  tipoCliente?: string;
  codigoCliente?: string;
  vigente?: boolean;
  fechaRegistro?: string;
}

export interface EmpleadoAdmin {
  idEmpleado: number;
  persona?: PersonaAdmin;
  nombreCompleto?: string;
  cargo?: string;
  salario?: number;
  fechaContratacion?: string;
  vigente?: boolean;
}

export interface ProveedorAdmin {
  idProveedor: number;
  razonSocial: string;
  ruc?: string;
  numeroDocumento?: string;
  contactoNombre?: string;
  nombreEncargado?: string;
  telefono?: string;
  telefonoContacto?: string;
  correo?: string;
  direccion?: string;
  vigente?: boolean;
}

export interface UsuarioAdmin {
  idUsuario: number;
  correo: string;
  estado: string;
  fechaUltimoAcceso?: string;
  fechaCreacion?: string;
  intentosFallidos?: number;
  bloqueadoHasta?: string;
  persona?: PersonaAdmin;
  rol?: {
    idRol: number;
    nombre: string;
  };
}

export interface RolAdmin {
  idRol: number;
  nombre: string;
  vigente?: boolean;
}

export interface PagoAdmin {
  idPago: number;
  venta?: { idVenta: number; total: number };
  medioPago?: { idMedioPago: number; nombre: string };
  montoPago: number;
  fechaPago: string;
  estadoPago: string;
  codigoTransaccion?: string;
  codigoOperacion?: string;
}

export interface MedioPagoAdmin {
  idMedioPago: number;
  nombre: string;
  instrucciones?: string;
  codigo?: string;
  vigente?: boolean;
}

export interface TipoDocumentoAdmin {
  idTipoDocumento: number;
  nombre?: string;
  codigoSunat?: string;
  descripcion?: string;
  longitudExacta?: number;
  vigente?: boolean;
}

export interface CarritoAdmin {
  idCarrito: number;
  cliente?: ClienteAdmin;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  estado?: string;
}

export interface ProductoProveedorAdmin {
  id?: { idProducto: number; idProveedor: number };
  producto?: { idProducto: number; nombre: string; precioVenta: number; codigoSku?: string };
  proveedor?: { idProveedor: number; razonSocial: string };
  precioCompra: number;
  tiempoPedidoDias?: number;
}

export interface RegistroAcceso {
  idAcceso: number;
  correo: string;
  fechaHora: string;
  ip?: string;
  agente?: string;
  exitoso: boolean;
  motivo?: string;
}

export interface RegistroAuditoria {
  idAuditoria: number;
  fechaHora: string;
  modulo: string;
  accion: string;
  descripcion?: string;
  datosPrevios?: string;
  datosNuevos?: string;
  ip?: string;
}

export interface DetalleVentaAdmin {
  idDetalleVenta: number;
  venta?: { idVenta: number; total: number; estado?: string; fechaVenta?: string };
  producto?: { idProducto: number; nombre: string; precioVenta: number };
  cantidad: number;
  precioUnitario: number;
  subTotalVenta: number;
}

export interface DetalleCarritoAdmin {
  idDetalleCarrito: number;
  carrito?: { idCarrito: number; cliente?: any };
  producto?: { idProducto: number; nombre: string; precioVenta: number };
  cantidad: number;
}

export interface ProductoImagenAdmin {
  idProductoImagen: number;
  producto?: { idProducto: number; nombre: string };
  urlImagen: string;
  esPrincipal?: boolean;
  idProducto?: number;
  nombreProducto?: string;
  skuProducto?: string;
  categoriaProducto?: string;
  marcaProducto?: string;
  precioProducto?: number;
}

export interface UbigeoAdmin {
  idUbigeo: number;
  departamento: string;
  provincia: string;
  distrito: string;
  vigente?: boolean;
}

export interface CentroAyuda {
  idAyuda: number;
  seccion: string;
  titulo: string;
  contenido: string;
  orden: number;
  activo: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly baseUrl = environment.apiUrl;

  // --- Usuarios & Roles ---
  getUsuarios(): Observable<UsuarioAdmin[]> {
    return this.http.get<UsuarioAdmin[]>(`${this.baseUrl}/admin/usuarios`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getRoles(): Observable<RolAdmin[]> {
    return this.http.get<RolAdmin[]>(`${this.baseUrl}/admin/roles`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  crearUsuario(datos: any): Observable<UsuarioAdmin> {
    return this.http.post<UsuarioAdmin>(`${this.baseUrl}/admin/usuarios`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  actualizarUsuario(id: number, datos: any): Observable<UsuarioAdmin> {
    return this.http.put<UsuarioAdmin>(`${this.baseUrl}/admin/usuarios/${id}`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Personas ---
  getPersonas(): Observable<PersonaAdmin[]> {
    return this.http.get<PersonaAdmin[]>(`${this.baseUrl}/admin/personas`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  crearPersona(datos: any): Observable<PersonaAdmin> {
    return this.http.post<PersonaAdmin>(`${this.baseUrl}/admin/personas`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  eliminarPersona(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/personas/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Clientes ---
  getClientes(): Observable<ClienteAdmin[]> {
    return this.http.get<ClienteAdmin[]>(`${this.baseUrl}/admin/clientes`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Empleados ---
  getEmpleados(): Observable<EmpleadoAdmin[]> {
    return this.http.get<EmpleadoAdmin[]>(`${this.baseUrl}/admin/empleados`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  crearEmpleado(datos: any): Observable<EmpleadoAdmin> {
    return this.http.post<EmpleadoAdmin>(`${this.baseUrl}/admin/empleados`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  actualizarEmpleado(id: number, datos: any): Observable<EmpleadoAdmin> {
    return this.http.put<EmpleadoAdmin>(`${this.baseUrl}/admin/empleados/${id}`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  eliminarEmpleado(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/empleados/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Proveedores ---
  getProveedores(): Observable<ProveedorAdmin[]> {
    return this.http.get<ProveedorAdmin[]>(`${this.baseUrl}/admin/proveedores`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  crearProveedor(datos: any): Observable<ProveedorAdmin> {
    return this.http.post<ProveedorAdmin>(`${this.baseUrl}/admin/proveedores`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  actualizarProveedor(id: number, datos: any): Observable<ProveedorAdmin> {
    return this.http.put<ProveedorAdmin>(`${this.baseUrl}/admin/proveedores/${id}`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  eliminarProveedor(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/proveedores/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Pagos ---
  getPagos(): Observable<PagoAdmin[]> {
    return this.http.get<PagoAdmin[]>(`${this.baseUrl}/admin/pagos`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Medios de Pago ---
  getMediosPago(): Observable<MedioPagoAdmin[]> {
    return this.http.get<MedioPagoAdmin[]>(`${this.baseUrl}/admin/medios-pago`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  crearMedioPago(datos: any): Observable<MedioPagoAdmin> {
    return this.http.post<MedioPagoAdmin>(`${this.baseUrl}/admin/medios-pago`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  actualizarMedioPago(id: number, datos: any): Observable<MedioPagoAdmin> {
    return this.http.put<MedioPagoAdmin>(`${this.baseUrl}/admin/medios-pago/${id}`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  eliminarMedioPago(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/medios-pago/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Tipos de Documento ---
  getTiposDocumento(): Observable<TipoDocumentoAdmin[]> {
    return this.http.get<TipoDocumentoAdmin[]>(`${this.baseUrl}/admin/tipos-documento`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  crearTipoDocumento(datos: any): Observable<TipoDocumentoAdmin> {
    return this.http.post<TipoDocumentoAdmin>(`${this.baseUrl}/admin/tipos-documento`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  actualizarTipoDocumento(id: number, datos: any): Observable<TipoDocumentoAdmin> {
    return this.http.put<TipoDocumentoAdmin>(`${this.baseUrl}/admin/tipos-documento/${id}`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  eliminarTipoDocumento(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/tipos-documento/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Carritos ---
  getCarritos(): Observable<CarritoAdmin[]> {
    return this.http.get<CarritoAdmin[]>(`${this.baseUrl}/admin/carritos`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Producto - Proveedor ---
  getProductosProveedor(): Observable<ProductoProveedorAdmin[]> {
    return this.http.get<ProductoProveedorAdmin[]>(`${this.baseUrl}/admin/productos-proveedor`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Auditoría ---
  getAccesos(): Observable<RegistroAcceso[]> {
    return this.http.get<RegistroAcceso[]>(`${this.baseUrl}/admin/auditoria/accesos`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getAcciones(modulo?: string): Observable<RegistroAuditoria[]> {
    const url = modulo
      ? `${this.baseUrl}/admin/auditoria/acciones?modulo=${encodeURIComponent(modulo)}`
      : `${this.baseUrl}/admin/auditoria/acciones`;
    return this.http.get<RegistroAuditoria[]>(url, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Detalles de Venta ---
  getDetallesVenta(idVenta?: number): Observable<DetalleVentaAdmin[]> {
    const url = idVenta
      ? `${this.baseUrl}/admin/detalles-venta?idVenta=${idVenta}`
      : `${this.baseUrl}/admin/detalles-venta`;
    return this.http.get<DetalleVentaAdmin[]>(url, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Detalles de Carrito ---
  getDetallesCarrito(idCarrito?: number): Observable<DetalleCarritoAdmin[]> {
    const url = idCarrito
      ? `${this.baseUrl}/admin/detalles-carrito?idCarrito=${idCarrito}`
      : `${this.baseUrl}/admin/detalles-carrito`;
    return this.http.get<DetalleCarritoAdmin[]>(url, {
      headers: this.authService.getAuthHeaders()
    });
  }

  eliminarDetalleCarrito(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/detalles-carrito/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Imágenes de Producto ---
  getImagenesProducto(): Observable<ProductoImagenAdmin[]> {
    return this.http.get<ProductoImagenAdmin[]>(`${this.baseUrl}/admin/imagenes`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  eliminarImagenProducto(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/imagenes/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Ubigeos ---
  getUbigeos(): Observable<UbigeoAdmin[]> {
    return this.http.get<UbigeoAdmin[]>(`${this.baseUrl}/ubigeos`);
  }

  crearUbigeo(datos: any): Observable<UbigeoAdmin> {
    return this.http.post<UbigeoAdmin>(`${this.baseUrl}/ubigeos`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  actualizarUbigeo(id: number, datos: any): Observable<UbigeoAdmin> {
    return this.http.put<UbigeoAdmin>(`${this.baseUrl}/ubigeos/${id}`, datos, {
      headers: this.authService.getAuthHeaders()
    });
  }

  eliminarUbigeo(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/ubigeos/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  // --- Centro de Ayuda ---
  getCentroAyuda(): Observable<CentroAyuda[]> {
    return this.http.get<CentroAyuda[]>(`${this.baseUrl}/ayuda`);
  }
}
