import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, shareReplay, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Categoria, Marca, Producto, ProductoImagen } from '../models/producto.models';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly baseUrl = environment.apiUrl;

  private categoriasCache$: Observable<Categoria[]> | null = null;
  private marcasCache$: Observable<Marca[]> | null = null;
  private destacadosCache$: Observable<Producto[]> | null = null;
  private productosCache$: Observable<Producto[]> | null = null;

  limpiarCache(): void {
    this.categoriasCache$ = null;
    this.marcasCache$ = null;
    this.destacadosCache$ = null;
    this.productosCache$ = null;
  }

  getProductos(filtros?: { q?: string; categoria?: number; marca?: number }): Observable<Producto[]> {
    const sinFiltros = !filtros || (!filtros.q && !filtros.categoria && !filtros.marca);
    if (sinFiltros) {
      if (!this.productosCache$) {
        this.productosCache$ = this.http.get<Producto[]>(`${this.baseUrl}/productos`).pipe(
          shareReplay({ bufferSize: 1, refCount: false })
        );
      }
      return this.productosCache$;
    }

    let params = new HttpParams();
    if (filtros?.q) params = params.set('q', filtros.q);
    if (filtros?.categoria) params = params.set('categoria', filtros.categoria.toString());
    if (filtros?.marca) params = params.set('marca', filtros.marca.toString());
    return this.http.get<Producto[]>(`${this.baseUrl}/productos`, { params });
  }

  getDestacados(): Observable<Producto[]> {
    if (!this.destacadosCache$) {
      this.destacadosCache$ = this.http.get<Producto[]>(`${this.baseUrl}/productos/destacados`).pipe(
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }
    return this.destacadosCache$;
  }

  getProductoById(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.baseUrl}/productos/${id}`);
  }

  getCategorias(): Observable<Categoria[]> {
    if (!this.categoriasCache$) {
      this.categoriasCache$ = this.http.get<Categoria[]>(`${this.baseUrl}/categorias`).pipe(
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }
    return this.categoriasCache$;
  }

  getMarcas(): Observable<Marca[]> {
    if (!this.marcasCache$) {
      this.marcasCache$ = this.http.get<Marca[]>(`${this.baseUrl}/marcas`).pipe(
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }
    return this.marcasCache$;
  }

  getImagenesProducto(idProducto: number): Observable<ProductoImagen[]> {
    return this.http.get<ProductoImagen[]>(`${this.baseUrl}/productos/${idProducto}/imagenes`);
  }

  // --- Admin ---
  adminGetProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/admin/productos`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  adminCrearProducto(producto: Partial<Producto>): Observable<Producto> {
    return this.http.post<Producto>(`${this.baseUrl}/admin/productos`, producto, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }

  adminActualizarProducto(id: number, producto: Partial<Producto>): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/admin/productos/${id}`, producto, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }


  adminEliminarProducto(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/productos/${id}`, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }

  adminSubirImagen(idProducto: number, archivo: File, esPrincipal = false): Observable<ProductoImagen> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    formData.append('esPrincipal', String(esPrincipal));
    return this.http.post<ProductoImagen>(
      `${this.baseUrl}/admin/productos/${idProducto}/imagenes`,
      formData,
      { headers: this.authService.getAuthHeaders() }
    ).pipe(tap(() => this.limpiarCache()));
  }

  adminAgregarImagenPorUrl(idProducto: number, urlImagen: string, esPrincipal = false): Observable<ProductoImagen> {
    return this.http.post<ProductoImagen>(
      `${this.baseUrl}/admin/productos/${idProducto}/imagenes/url`,
      { urlImagen, esPrincipal },
      { headers: this.authService.getAuthHeaders() }
    ).pipe(tap(() => this.limpiarCache()));
  }

  adminEstablecerImagenPrincipal(idImagen: number): Observable<ProductoImagen> {
    return this.http.put<ProductoImagen>(
      `${this.baseUrl}/admin/imagenes/${idImagen}/principal`,
      {},
      { headers: this.authService.getAuthHeaders() }
    ).pipe(tap(() => this.limpiarCache()));
  }

  adminActualizarImagen(id: number, datos: { idProducto?: number; urlImagen?: string; esPrincipal?: boolean }): Observable<ProductoImagen> {
    return this.http.put<ProductoImagen>(
      `${this.baseUrl}/admin/imagenes/${id}`,
      datos,
      { headers: this.authService.getAuthHeaders() }
    ).pipe(tap(() => this.limpiarCache()));
  }

  adminActualizarImagenArchivo(id: number, archivo: File, idProducto?: number, esPrincipal?: boolean): Observable<ProductoImagen> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    if (idProducto) formData.append('idProducto', String(idProducto));
    if (esPrincipal !== undefined) formData.append('esPrincipal', String(esPrincipal));
    return this.http.post<ProductoImagen>(
      `${this.baseUrl}/admin/imagenes/${id}/archivo`,
      formData,
      { headers: this.authService.getAuthHeaders() }
    ).pipe(tap(() => this.limpiarCache()));
  }

  adminEliminarImagen(idImagen: number): Observable<any> {
    return this.http.delete(
      `${this.baseUrl}/admin/imagenes/${idImagen}`,
      { headers: this.authService.getAuthHeaders() }
    ).pipe(tap(() => this.limpiarCache()));
  }

  adminGetCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.baseUrl}/admin/categorias`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  adminCrearCategoria(cat: Partial<Categoria>): Observable<Categoria> {
    return this.http.post<Categoria>(`${this.baseUrl}/admin/categorias`, cat, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }

  adminActualizarCategoria(id: number, cat: Partial<Categoria>): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.baseUrl}/admin/categorias/${id}`, cat, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }

  adminEliminarCategoria(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/categorias/${id}`, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }

  adminGetMarcas(): Observable<Marca[]> {
    return this.http.get<Marca[]>(`${this.baseUrl}/admin/marcas`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  adminCrearMarca(marca: Partial<Marca>): Observable<Marca> {
    return this.http.post<Marca>(`${this.baseUrl}/admin/marcas`, marca, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }

  adminActualizarMarca(id: number, marca: Partial<Marca>): Observable<Marca> {
    return this.http.put<Marca>(`${this.baseUrl}/admin/marcas/${id}`, marca, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }

  adminSubirLogoMarca(id: number, archivo: File): Observable<Marca> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<Marca>(`${this.baseUrl}/admin/marcas/${id}/logo`, formData, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }

  adminEliminarMarca(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/marcas/${id}`, {
      headers: this.authService.getAuthHeaders()
    }).pipe(tap(() => this.limpiarCache()));
  }
}
