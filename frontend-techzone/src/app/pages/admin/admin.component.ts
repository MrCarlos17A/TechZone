import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { VentaService } from '../../services/venta.service';
import { VentaCompleta } from '../../models/venta.models';
import { ProductoService } from '../../services/producto.service';
import {
  AdminService,
  UsuarioAdmin,
  RolAdmin,
  RegistroAcceso,
  RegistroAuditoria,
  PersonaAdmin,
  ClienteAdmin,
  EmpleadoAdmin,
  ProveedorAdmin,
  PagoAdmin,
  MedioPagoAdmin,
  TipoDocumentoAdmin,
  CarritoAdmin,
  ProductoProveedorAdmin,
  DetalleVentaAdmin,
  DetalleCarritoAdmin,
  ProductoImagenAdmin,
  UbigeoAdmin
} from '../../services/admin.service';
import { ConfiguracionService } from '../../services/configuracion.service';
import { ToastService } from '../../services/toast.service';
import { TemaService } from '../../services/tema.service';
import { Validaciones } from '../../utils/validaciones';
import { ExcelExportService, VentaExportData, FiltrosExportInfo } from '../../services/excel-export.service';
import { ConfiguracionSitio } from '../../models/configuracion.models';
import { VentaResumen } from '../../models/venta.models';
import { Categoria, Marca, Producto } from '../../models/producto.models';

export type TabAdmin =
  | 'dashboard'
  | 'productos'
  | 'marcas'
  | 'categorias'
  | 'imagenes'
  | 'productos-proveedor'
  | 'personas'
  | 'clientes'
  | 'empleados'
  | 'proveedores'
  | 'usuarios'
  | 'ventas'
  | 'detalles-venta'
  | 'pagos'
  | 'medios-pago'
  | 'carritos'
  | 'detalles-carrito'
  | 'configuracion'
  | 'roles'
  | 'tipos-documento'
  | 'ubigeos'
  | 'auditoria'
  | 'ayuda';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html'
})
export class AdminComponent implements OnInit {
  readonly authService = inject(AuthService);
  readonly temaService = inject(TemaService);
  private readonly ventaService = inject(VentaService);
  comprobanteAdmin = signal<VentaCompleta | null>(null);
  cargandoComprobanteAdmin = signal(false);
  private readonly productoService = inject(ProductoService);
  private readonly adminService = inject(AdminService);
  readonly configuracionService = inject(ConfiguracionService);
  private readonly toastService = inject(ToastService);
  private readonly excelExportService = inject(ExcelExportService);
  private readonly router = inject(Router);
  readonly Math = Math;

  tabActiva = signal<TabAdmin>('dashboard');
  sidebarContraido = signal(false);
  sidebarMovilAbierto = signal(false);

  // Centro de Ayuda Interactivo
  guiaAyudaSeleccionada = signal<string | null>('productos');
  categoriaAyudaSeleccionada = signal<string>('todas');
  busquedaAyuda = signal<string>('');

  toggleGuiaAyuda(id: string): void {
    this.guiaAyudaSeleccionada.update(actual => actual === id ? null : id);
  }

  seleccionarCategoriaAyuda(cat: string): void {
    this.categoriaAyudaSeleccionada.set(cat);
  }

  // Datos principales
  ventas = signal<VentaResumen[]>([]);
  productos = signal<Producto[]>([]);
  categorias = signal<Categoria[]>([]);
  marcas = signal<Marca[]>([]);
  productosProveedor = signal<ProductoProveedorAdmin[]>([]);
  
  personas = signal<PersonaAdmin[]>([]);
  clientes = signal<ClienteAdmin[]>([]);
  empleados = signal<EmpleadoAdmin[]>([]);
  proveedores = signal<ProveedorAdmin[]>([]);
  usuarios = signal<UsuarioAdmin[]>([]);
  
  pagos = signal<PagoAdmin[]>([]);
  mediosPago = signal<MedioPagoAdmin[]>([]);
  carritos = signal<CarritoAdmin[]>([]);
  detallesCarrito = signal<DetalleCarritoAdmin[]>([]);
  detallesVenta = signal<DetalleVentaAdmin[]>([]);
  imagenes = signal<ProductoImagenAdmin[]>([]);
  filtroProductoImagen = signal<number | 'todos'>('todos');
  
  // Explorador Jerárquico de Galería de Imágenes (Supabase Storage: Categoría -> Marca -> Producto)
  modoVistaImagenes = signal<'carpetas' | 'cuadricula'>('carpetas');
  carpetaCategoriaSel = signal<Categoria | null>(null);
  carpetaMarcaSel = signal<Marca | null>(null);
  carpetaProductoSel = signal<Producto | null>(null);
  busquedaExploradorImagenes = signal<string>('');

  mostrarModalNuevaImagen = signal(false);
  idProductoParaImagen = signal<number | null>(null);
  urlNuevaImagen = signal('');
  archivoNuevaImagen: File | null = null;
  previewNuevaImagen = signal<string | null>(null);
  esPrincipalNuevaImagen = signal(true);
  guardandoNuevaImagen = signal(false);

  // Gestor Avanzado de Subida Múltiple (1 a 8 fotos) y Carpetas en Supabase Storage
  carpetaSubcategoriaSel = signal<'audifonos' | 'teclados' | null>(null);
  modoCarpetaSubida = signal<'existente' | 'nueva'>('existente');
  catSelSubida = signal<Categoria | null>(null);
  subcatPerifericosSubida = signal<'audifonos' | 'teclados' | null>(null);
  marcaSelSubida = signal<Marca | null>(null);
  productoSelSubida = signal<Producto | null>(null);
  nombreNuevaCarpetaProducto = signal<string>('');
  precioNuevoProductoSubida = signal<number>(99.00);
  stockNuevoProductoSubida = signal<number>(10);
  descripcionNuevaCarpetaProducto = signal<string>('');
  archivosSubidaLote = signal<{ archivo: File; preview: string; nombreGenerado: string; esPrincipal: boolean }[]>([]);
  indicePrincipalLote = signal<number>(0);
  subiendoLote = signal<boolean>(false);
  progresoSubidaLote = signal<string>('');

  marcasDisponiblesSubida = computed(() => {
    return [...this.marcas()].sort((a, b) => a.nombre.localeCompare(b.nombre));
  });

  productosDisponiblesSubida = computed(() => {
    const cat = this.catSelSubida();
    const marca = this.marcaSelSubida();
    let prods = this.productos();
    if (cat) {
      prods = prods.filter(p => p.categoria?.idCategoria === cat.idCategoria);
      if (cat.idCategoria === 7 && this.subcatPerifericosSubida()) {
        const sub = this.subcatPerifericosSubida()!;
        prods = prods.filter(p => {
          const mNorm = (p.marca?.nombre || '').toLowerCase();
          if (sub === 'audifonos') return mNorm.includes('hyperx') || p.marca?.idMarca === 16;
          if (sub === 'teclados') return mNorm.includes('msi') || p.marca?.idMarca === 2;
          return true;
        });
      }
    }
    if (marca) {
      prods = prods.filter(p => p.marca?.idMarca === marca.idMarca);
    }
    return prods;
  });

  rutaSupabasePrevista = computed(() => {
    const cat = this.catSelSubida();
    const marca = this.marcaSelSubida();
    const catSlug = cat ? this.generarSlug(cat.nombre) : '[categoria]';
    let subRuta = '';
    if (cat?.idCategoria === 7 && this.subcatPerifericosSubida()) {
      subRuta = this.subcatPerifericosSubida() + '/';
    }
    const marcaSlug = marca ? this.generarSlug(marca.nombre) : '[marca]';
    let prodSlug = '';
    if (this.modoCarpetaSubida() === 'existente') {
      prodSlug = this.productoSelSubida() ? this.generarSlug(this.productoSelSubida()!.nombre) : '[producto]';
    } else {
      prodSlug = this.nombreNuevaCarpetaProducto().trim() ? this.generarSlug(this.nombreNuevaCarpetaProducto()) : '[nueva-carpeta]';
    }
    return `productos/${catSlug}/${subRuta}${marcaSlug}/${prodSlug}/`;
  });

  // Modal Editar Imagen
  mostrarModalEditarImagen = signal(false);
  idImagenEditando = signal<number | null>(null);
  idProductoEditandoImagen = signal<number | null>(null);
  urlEditandoImagen = signal('');
  archivoEditandoImagen: File | null = null;
  previewEditandoImagen = signal<string | null>(null);
  esPrincipalEditandoImagen = signal(false);
  guardandoEdicionImagen = signal(false);

  imagenesFiltradas = computed(() => {
    const filtro = this.filtroProductoImagen();
    const todas = this.imagenes();
    if (filtro === 'todos') {
      return todas;
    }
    return todas.filter(img => {
      const prod = this.getProductoDeImagen(img);
      return prod ? prod.idProducto === Number(filtro) : false;
    });
  });

  paginaImagenes = signal(1);
  tamanoImagenes = signal(15);

  imagenesPaginadas = computed(() => {
    const list = this.imagenesFiltradas();
    const page = this.paginaImagenes();
    const size = this.tamanoImagenes();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasImagenes = computed(() => {
    return Math.max(1, Math.ceil(this.imagenesFiltradas().length / this.tamanoImagenes()));
  });
  ubigeos = signal<UbigeoAdmin[]>([]);
  
  roles = signal<RolAdmin[]>([]);
  tiposDocumento = signal<TipoDocumentoAdmin[]>([]);
  accesos = signal<RegistroAcceso[]>([]);
  acciones = signal<RegistroAuditoria[]>([]);
  cargando = signal(true);

  // ==========================================
  // MODAL CONFIRMACIÓN CENTRALIZADO (Gestalt)
  // ==========================================
  mostrarModalConfirmacion = signal(false);
  confirmacionTitulo = signal('¿Confirmar acción?');
  confirmacionMensaje = signal('Esta acción no se puede deshacer.');
  confirmacionBotonTexto = signal('Eliminar');
  confirmacionEsPeligro = signal(true);
  confirmacionTipoBoton = signal<'peligro' | 'advertencia' | 'primario'>('peligro');
  confirmacionAccion: () => void = () => {};

  solicitarConfirmacion(
    titulo: string,
    mensaje: string,
    accion: () => void,
    botonTexto = 'Eliminar',
    esPeligro = true,
    tipoBoton: 'peligro' | 'advertencia' | 'primario' = esPeligro ? 'peligro' : 'primario'
  ): void {
    this.confirmacionTitulo.set(titulo);
    this.confirmacionMensaje.set(mensaje);
    this.confirmacionBotonTexto.set(botonTexto);
    this.confirmacionEsPeligro.set(esPeligro);
    this.confirmacionTipoBoton.set(tipoBoton);
    this.confirmacionAccion = accion;
    this.mostrarModalConfirmacion.set(true);
  }

  ejecutarConfirmacion(): void {
    this.mostrarModalConfirmacion.set(false);
    if (this.confirmacionAccion) {
      this.confirmacionAccion();
    }
  }

  // ==========================================
  // MODAL PRODUCTO (Crear / Editar)
  // ==========================================
  mostrarModalProducto = signal(false);
  esEdicionProducto = signal(false);
  idProductoEditando = signal<number | null>(null);
  nuevoNombre = signal('');
  nuevoPrecio = signal<number>(0);
  nuevoStock = signal<number>(1);
  nuevaDesc = signal('');
  nuevoDestacado = signal<boolean>(false);
  nuevoNovedad = signal<boolean>(false);
  idCatSel = signal<number | null>(null);
  idMarcaSel = signal<number | null>(null);
  urlImagenProducto = signal('');
  archivoImagenProducto: File | null = null;
  previewImagenProducto = signal<string | null>(null);
  archivoImagen: File | null = null;

  // ==========================================
  // MODAL UBIGEO (Crear / Editar)
  // ==========================================
  mostrarModalUbigeo = signal(false);
  esEdicionUbigeo = signal(false);
  ubiId = signal<number | null>(null);
  ubiDepartamento = signal('');
  ubiProvincia = signal('');
  ubiDistrito = signal('');
  ubiVigente = signal(true);

  // ==========================================
  // MODAL CATEGORÍA (Crear / Editar)
  // ==========================================
  mostrarModalCategoria = signal(false);
  esEdicionCategoria = signal(false);
  idCatEditando = signal<number | null>(null);
  nuevoNombreCat = signal('');

  // ==========================================
  // MODAL MARCA (Crear / Editar)
  // ==========================================
  mostrarModalMarca = signal(false);
  esEdicionMarca = signal(false);
  idMarcaEditando = signal<number | null>(null);
  nuevoNombreMarca = signal('');
  nuevoLogoMarca = signal('');
  previewLogoMarca = signal<string | null>(null);
  archivoLogoMarca: File | null = null;
  guardandoMarca = signal(false);

  // ==========================================
  // MODAL PROVEEDOR (Crear / Editar)
  // ==========================================
  mostrarModalProveedor = signal(false);
  esEdicionProveedor = signal(false);
  idProvEditando = signal<number | null>(null);
  nuevoProvRazon = signal('');
  nuevoProvRuc = signal('');
  nuevoProvContacto = signal('');
  nuevoProvTelefono = signal('');
  nuevoProvCorreo = signal('');

  // ==========================================
  // MODAL MEDIO DE PAGO (Crear / Editar)
  // ==========================================
  mostrarModalMedioPago = signal(false);
  esEdicionMedioPago = signal(false);
  idMedioEditando = signal<number | null>(null);
  nuevoMedioNombre = signal('');
  nuevoMedioInstrucciones = signal('');

  // ==========================================
  // MODAL TIPO DOCUMENTO (Crear / Editar)
  // ==========================================
  mostrarModalTipoDoc = signal(false);
  esEdicionTipoDoc = signal(false);
  idTipoDocEditando = signal<number | null>(null);
  nuevoTipoDocDesc = signal('');
  nuevoTipoDocSunat = signal('');
  nuevoTipoDocLongitud = signal<number>(8);
  nuevoTipoDocVigente = signal(true);

  // ==========================================
  // MODAL NUEVO COLABORADOR (RENIEC / DNI)
  // ==========================================
  mostrarModalNuevoEmpleado = signal(false);
  guardandoEmpleado = signal(false);
  consultandoDniColab = signal(false);
  formColabDni = signal('');
  formColabNombres = signal('');
  formColabApPaterno = signal('');
  formColabApMaterno = signal('');
  formColabFechaNacimiento = signal('');
  formColabTelefono = signal('');
  formColabCorreo = signal('');
  formColabPassword = signal('');
  mostrarPasswordColab = signal(false);
  formColabDireccion = signal('');
  formColabReferencia = signal('');
  formColabDepartamento = signal('Lima');
  formColabProvincia = signal('Lima');
  formColabDistritoId = signal<number | null>(null);
  formColabCargo = signal('SUPERVISOR_VENTAS');
  formColabRolId = signal<number>(2);
  formColabSalario = signal<number>(1800);
  formColabFechaContrato = signal(new Date().toISOString().substring(0, 10));

  departamentosColab = computed(() => {
    const set = new Set<string>();
    for (const u of this.ubigeos()) {
      if (u.departamento) set.add(u.departamento);
    }
    return Array.from(set).sort();
  });

  provinciasColab = computed(() => {
    const dep = this.formColabDepartamento();
    const set = new Set<string>();
    for (const u of this.ubigeos()) {
      if (!dep || u.departamento?.toLowerCase() === dep.toLowerCase()) {
        if (u.provincia) set.add(u.provincia);
      }
    }
    return Array.from(set).sort();
  });

  distritosColab = computed(() => {
    const dep = this.formColabDepartamento();
    const prov = this.formColabProvincia();
    return this.ubigeos().filter(u => {
      const matchDep = !dep || u.departamento?.toLowerCase() === dep.toLowerCase();
      const matchProv = !prov || u.provincia?.toLowerCase() === prov.toLowerCase();
      return matchDep && matchProv;
    }).sort((a, b) => (a.distrito || '').localeCompare(b.distrito || ''));
  });

  // Edición Integral de Colaborador
  mostrarModalEditarEmpleado = signal(false);
  empleadoEditando = signal<EmpleadoAdmin | null>(null);
  formEditNombres = signal('');
  formEditApellidoPaterno = signal('');
  formEditApellidoMaterno = signal('');
  formEditNumeroDocumento = signal('');
  formEditFechaNacimiento = signal('');
  formEditTelefono = signal('');
  formEditDireccion = signal('');
  formEditReferencia = signal('');
  formEditDepartamento = signal('');
  formEditProvincia = signal('');
  formEditDistritoId = signal<number | null>(null);
  formEditCorreo = signal('');
  formEditPassword = signal('');
  mostrarPasswordEditColab = signal(false);
  formEditIdRol = signal<number>(2);
  formEditSalario = signal<number>(1800);
  formEditFechaContrato = signal('');
  formEditVigente = signal(true);
  guardandoEdicionEmpleado = signal(false);

  criteriosPasswordColab = computed(() => Validaciones.evaluarCriteriosContrasena(this.formColabPassword()));
  criteriosPasswordUsuario = computed(() => Validaciones.evaluarCriteriosContrasena(this.formUsuarioNuevaPassword()));
  criteriosPasswordEditColab = computed(() => Validaciones.evaluarCriteriosContrasena(this.formEditPassword()));

  provinciasEditColab = computed(() => {
    const dep = this.formEditDepartamento();
    if (!dep) return [];
    const set = new Set<string>();
    this.ubigeos()
      .filter(u => u.departamento?.toLowerCase() === dep.toLowerCase() && u.provincia)
      .forEach(u => set.add(u.provincia!));
    return Array.from(set).sort();
  });

  distritosEditColab = computed(() => {
    const dep = this.formEditDepartamento();
    const prov = this.formEditProvincia();
    return this.ubigeos().filter(u => {
      const matchDep = !dep || u.departamento?.toLowerCase() === dep.toLowerCase();
      const matchProv = !prov || u.provincia?.toLowerCase() === prov.toLowerCase();
      return matchDep && matchProv;
    }).sort((a, b) => (a.distrito || '').localeCompare(b.distrito || ''));
  });

  // ==========================================
  // MODAL ROLES & PERMISOS
  // ==========================================
  mostrarModalRol = signal(false);
  esEdicionRol = signal(false);
  idRolEditando = signal<number | null>(null);
  formRolNombre = signal('');
  formRolVigente = signal(true);
  formRolEsAdministrativo = signal(false);
  formRolPermisos = signal<Record<string, string[]>>({});

  tienePermiso(modulo: string, accion: 'ver' | 'crear' | 'editar' | 'eliminar' = 'ver'): boolean {
    return this.authService.tienePermiso(modulo, accion);
  }

  readonly MODULOS_SISTEMA: {
    clave: string;
    label: string;
    soloLectura?: boolean;
    acciones: ('ver' | 'crear' | 'editar' | 'eliminar')[];
  }[] = [
    { clave: 'dashboard', label: 'Panel Dashboard', soloLectura: true, acciones: ['ver'] },
    { clave: 'productos', label: 'Catálogo de Productos', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'categorias', label: 'Categorías', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'marcas', label: 'Marcas de Productos', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'imagenes', label: 'Galería de Imágenes', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'ventas', label: 'Ventas y Despachos', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'pagos', label: 'Transacciones y Pagos', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'usuarios', label: 'Usuarios y Accesos', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'empleados', label: 'Colaboradores y Nómina', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'clientes', label: 'Cartera de Clientes', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'roles', label: 'Roles y Permisos', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
    { clave: 'configuracion', label: 'Configuración del Sitio', acciones: ['ver', 'crear', 'editar', 'eliminar'] },
  ];
  readonly ACCIONES_PERMISO = ['ver', 'crear', 'editar', 'eliminar'] as const;

  accionPermitidaEnModulo(modulo: string, accion: string): boolean {
    const mod = this.MODULOS_SISTEMA.find(m => m.clave === modulo);
    return mod ? mod.acciones.includes(accion as any) : true;
  }

  // ==========================================
  // VALIDACIONES COMPUTADAS UNIVERSALES
  // ==========================================
  readonly formularioProductoValido = computed(() => {
    const nombreVal = (this.nuevoNombre() || '').trim().length >= 3;
    const precioVal = (this.nuevoPrecio() || 0) > 0;
    const stockVal = (this.nuevoStock() || 0) >= 0;
    const catVal = !!this.idCatSel() && this.idCatSel()! > 0;
    const marcaVal = !!this.idMarcaSel() && this.idMarcaSel()! > 0;
    const descVal = (this.nuevaDesc() || '').trim().length >= 5;
    return nombreVal && precioVal && stockVal && catVal && marcaVal && descVal;
  });

  readonly formularioCategoriaValido = computed(() => {
    return (this.nuevoNombreCat() || '').trim().length >= 2;
  });

  readonly formularioMarcaValido = computed(() => {
    return (this.nuevoNombreMarca() || '').trim().length >= 2;
  });

  readonly formularioProveedorValido = computed(() => {
    const razonVal = (this.nuevoProvRazon() || '').trim().length >= 3;
    const rucVal = Validaciones.validarRuc(this.nuevoProvRuc()).valido;
    const telVal = (this.nuevoProvTelefono() || '').trim().length >= 7;
    const correoVal = Validaciones.validarCorreo(this.nuevoProvCorreo()).valido;
    return razonVal && rucVal && telVal && correoVal;
  });

  readonly formularioRolValido = computed(() => {
    const nombreVal = (this.formRolNombre() || '').trim().length >= 2;
    if (!this.formRolEsAdministrativo()) {
      return nombreVal;
    }
    const tienePermisos = Object.values(this.formRolPermisos()).some(arr => arr && arr.length > 0);
    return nombreVal && tienePermisos;
  });

  readonly formularioNuevoEmpleadoValido = computed(() => {
    const nombresVal = Validaciones.validarTextoNombre(this.formColabNombres(), 'Nombres').valido;
    const apPatVal = Validaciones.validarTextoNombre(this.formColabApPaterno(), 'Apellido Paterno').valido;
    const apMatVal = Validaciones.validarTextoNombre(this.formColabApMaterno(), 'Apellido Materno').valido;
    const dniVal = Validaciones.validarDni(this.formColabDni()).valido;
    const telVal = Validaciones.validarTelefono(this.formColabTelefono()).valido;
    const nacVal = Validaciones.validarMayorDeEdad(this.formColabFechaNacimiento()).valido;
    const dirVal = Validaciones.validarDireccion(this.formColabDireccion()).valido;
    const ubigeoVal = Validaciones.validarUbigeo(this.formColabDistritoId()).valido;
    const correoVal = Validaciones.validarCorreo(this.formColabCorreo()).valido;
    const passVal = Validaciones.validarContrasena(this.formColabPassword()).valido;
    const rolVal = !!this.formColabRolId() && this.formColabRolId() > 0;
    const salarioVal = (this.formColabSalario() || 0) >= 1025;
    const contrVal = Validaciones.validarFechaObligatoria(this.formColabFechaContrato(), 'Fecha de contrato').valido;

    return nombresVal && apPatVal && apMatVal && dniVal && telVal && nacVal && dirVal && ubigeoVal && correoVal && passVal && rolVal && salarioVal && contrVal;
  });

  readonly formularioEditarEmpleadoValido = computed(() => {
    const nombresVal = Validaciones.validarTextoNombre(this.formEditNombres(), 'Nombres').valido;
    const apPatVal = Validaciones.validarTextoNombre(this.formEditApellidoPaterno(), 'Apellido Paterno').valido;
    const apMatVal = Validaciones.validarTextoNombre(this.formEditApellidoMaterno(), 'Apellido Materno').valido;
    const dniVal = Validaciones.validarDni(this.formEditNumeroDocumento()).valido;
    const telVal = Validaciones.validarTelefono(this.formEditTelefono()).valido;
    const nacVal = Validaciones.validarMayorDeEdad(this.formEditFechaNacimiento()).valido;
    const dirVal = Validaciones.validarDireccion(this.formEditDireccion()).valido;
    const ubigeoVal = Validaciones.validarUbigeo(this.formEditDistritoId()).valido;
    const correoVal = Validaciones.validarCorreo(this.formEditCorreo()).valido;
    const rolVal = !!this.formEditIdRol() && this.formEditIdRol()! > 0;
    const salarioVal = (this.formEditSalario() || 0) >= 1025;
    const contrVal = Validaciones.validarFechaObligatoria(this.formEditFechaContrato(), 'Fecha de contrato').valido;

    let passVal = true;
    if ((this.formEditPassword() || '').trim()) {
      passVal = Validaciones.validarContrasena(this.formEditPassword()).valido;
    }
    return nombresVal && apPatVal && apMatVal && dniVal && telVal && nacVal && dirVal && ubigeoVal && correoVal && rolVal && salarioVal && contrVal && passVal;
  });

  // ==========================================
  // MODAL EDICIÓN USUARIO
  // ==========================================
  mostrarModalEditarUsuario = signal(false);
  usuarioEditando = signal<UsuarioAdmin | null>(null);
  formUsuarioRolId = signal<number | null>(null);
  formUsuarioEstado = signal<string>('ACTIVO');
  formUsuarioNuevaPassword = signal('');
  mostrarPasswordUsuario = signal(false);

  // ==========================================
  // MODAL EDICIÓN MEDIO DE PAGO
  // ==========================================
  mostrarModalEditarMedioPago = signal(false);
  medioPagoEditando = signal<MedioPagoAdmin | null>(null);
  formMedioPagoInstrucciones = signal('');
  formMedioPagoVigente = signal(true);

  // ==========================================
  // CONFIGURACIÓN DEL SITIO & ASSETS
  // ==========================================
  configForm: ConfiguracionSitio = {};
  configFormBackup: ConfiguracionSitio = {};
  modoEdicionConfig = signal(false);
  guardandoConfig = signal(false);
  guardandoImagenesSitio = signal(false);
  archivoLogoSitio: File | null = null;
  archivoLibroSitio: File | null = null;
  previewLogoUrl = signal<string | null>(null);
  previewLibroUrl = signal<string | null>(null);
  pestanaLogo = signal<'archivo' | 'url'>('archivo');
  readonly LOGO_OFICIAL_BASE = 'https://polvfrjaecmugwyzyuzm.supabase.co/storage/v1/object/public/imagenes/configurtacion_sitio/TechZone.webp';

  // ==========================================
  // PAGINACIÓN Y FILTROS REACTIVOS (Unificado a 15 por página)
  // ==========================================
  readonly REGISTROS_POR_PAGINA_DEFAULT = 15;

  // Ubigeos
  filtroUbigeos = signal('');
  filtroEstadoUbigeos = signal<'todos' | 'vigentes' | 'inactivos'>('todos');
  paginaUbigeos = signal(1);
  tamanoUbigeos = signal(15);

  // Productos
  filtroProductos = signal('');
  filtroStockProducto = signal<'todos' | 'critico' | 'agotado' | 'disponible'>('todos');
  filtroCategoriaProducto = signal<number | 'todas'>('todas');
  filtroMarcaProducto = signal<number | 'todas'>('todas');
  filtroPrecioMin = signal<number | null>(null);
  filtroPrecioMax = signal<number | null>(null);
  filtroOrdenPrecio = signal<'ninguno' | 'asc' | 'desc'>('ninguno');
  filtroSoloDestacados = signal<boolean>(false);
  filtroSoloNovedades = signal<boolean>(false);
  paginaProductos = signal(1);
  tamanoProductos = signal(15);

  totalDestacados = computed(() => this.productos().filter(p => !!p.destacado).length);
  totalNovedades = computed(() => this.productos().filter(p => !!p.novedad).length);

  // Gestor y Modal de Sustitución de Productos Destacados (Límite 9)
  modalSustitucionDestacadoAbierto = signal<boolean>(false);
  productoSustitutoEntrante = signal<Producto | null>(null);
  productoSustitutoSaliente = signal<Producto | null>(null);
  busquedaSustituto = signal<string>('');

  productosDestacadosActuales = computed(() =>
    this.productos().filter(p => !!p.destacado)
  );

  productosCandidatosSustitucion = computed(() => {
    const q = this.busquedaSustituto().trim().toLowerCase();
    const noDestacados = this.productos().filter(p => !p.destacado && (p.vigente === null || p.vigente));
    if (!q) return noDestacados.slice(0, 8);
    return noDestacados.filter(p =>
      (p.nombre || '').toLowerCase().includes(q) ||
      (p.codigoSku || '').toLowerCase().includes(q) ||
      (p.marca?.nombre || '').toLowerCase().includes(q) ||
      (p.categoria?.nombre || '').toLowerCase().includes(q)
    ).slice(0, 12);
  });

  hayFiltrosProductoActivos = computed(() => {
    return !!this.filtroProductos().trim() ||
      this.filtroStockProducto() !== 'todos' ||
      this.filtroCategoriaProducto() !== 'todas' ||
      this.filtroMarcaProducto() !== 'todas' ||
      this.filtroPrecioMin() !== null ||
      this.filtroPrecioMax() !== null ||
      this.filtroOrdenPrecio() !== 'ninguno' ||
      this.filtroSoloDestacados() ||
      this.filtroSoloNovedades();
  });

  limpiarFiltrosProducto(): void {
    this.filtroProductos.set('');
    this.filtroStockProducto.set('todos');
    this.filtroCategoriaProducto.set('todas');
    this.filtroMarcaProducto.set('todas');
    this.filtroPrecioMin.set(null);
    this.filtroPrecioMax.set(null);
    this.filtroOrdenPrecio.set('ninguno');
    this.filtroSoloDestacados.set(false);
    this.filtroSoloNovedades.set(false);
    this.paginaProductos.set(1);
  }

  // Ventas y Filtros Multidimensionales
  filtroVentas = signal('');
  filtroEstadoVenta = signal('TODOS');
  filtroPeriodoVentas = signal<'todos' | 'hoy' | 'semana' | 'mes' | 'mes_anterior' | 'anio' | 'especifico'>('todos');
  filtroMesVentas = signal<number | 'todos'>('todos');
  filtroAnioVentas = signal<number | 'todos'>('todos');
  filtroCategoriaVenta = signal<number | 'todas'>('todas');
  filtroMarcaVenta = signal<number | 'todas'>('todas');
  filtroMedioPagoVenta = signal<string>('TODOS');
  mostrarAnaliticaVentas = signal(true);
  paginaVentas = signal(1);
  tamanoVentas = signal(15);

  // Detalles de Venta y Filtros Multidimensionales
  filtroDetallesVenta = signal('');
  filtroPeriodoDetallesVenta = signal<'todos' | 'hoy' | 'semana' | 'mes' | 'mes_anterior' | 'anio' | 'especifico'>('todos');
  filtroMesDetallesVenta = signal<number | 'todos'>('todos');
  filtroAnioDetallesVenta = signal<number | 'todos'>('todos');
  filtroEstadoDetallesVenta = signal('TODOS');
  filtroCategoriaDetallesVenta = signal<number | 'todas'>('todas');
  filtroMarcaDetallesVenta = signal<number | 'todas'>('todas');
  mostrarAnaliticaDetalles = signal(true);
  paginaDetallesVenta = signal(1);
  tamanoDetallesVenta = signal(15);

  // Catálogo de meses y años para selectores analíticos
  aniosAnaliticos = [2026, 2025, 2024];
  mesesAnaliticos = [
    { id: 0, nombre: 'Enero' },
    { id: 1, nombre: 'Febrero' },
    { id: 2, nombre: 'Marzo' },
    { id: 3, nombre: 'Abril' },
    { id: 4, nombre: 'Mayo' },
    { id: 5, nombre: 'Junio' },
    { id: 6, nombre: 'Julio' },
    { id: 7, nombre: 'Agosto' },
    { id: 8, nombre: 'Septiembre' },
    { id: 9, nombre: 'Octubre' },
    { id: 10, nombre: 'Noviembre' },
    { id: 11, nombre: 'Diciembre' }
  ];

  // Personas
  filtroPersonas = signal('');
  paginaPersonas = signal(1);
  tamanoPersonas = signal(15);

  // Productos - Proveedor
  filtroProductosProveedor = signal('');
  paginaProductosProveedor = signal(1);
  tamanoProductosProveedor = signal(15);

  // Clientes
  filtroClientes = signal('');
  paginaClientes = signal(1);
  tamanoClientes = signal(15);

  // Empleados
  filtroEmpleados = signal('');
  paginaEmpleados = signal(1);
  tamanoEmpleados = signal(15);

  // Usuarios
  filtroUsuarios = signal('');
  paginaUsuarios = signal(1);
  tamanoUsuarios = signal(15);

  // Auditoría
  tipoVistaAuditoria = signal<'acciones' | 'accesos'>('acciones');
  filtroAuditoriaModulo = signal('TODOS');
  paginaAuditoria = signal(1);
  tamanoAuditoria = signal(15);
  paginaAccesos = signal(1);
  tamanoAccesos = signal(15);



  ngOnInit(): void {
    if (!this.authService.isStaff()) {
      this.toastService.advertencia('Acceso restringido para personal administrativo');
      this.router.navigate(['/']);
      return;
    }
    const tabsDisponibles: TabAdmin[] = [
      'dashboard', 'productos', 'categorias', 'marcas', 'imagenes',
      'ventas', 'pagos', 'clientes', 'empleados', 'usuarios', 'roles', 'configuracion', 'ayuda'
    ];
    if (!this.puedeVerTab(this.tabActiva())) {
      const primera = tabsDisponibles.find(t => this.puedeVerTab(t));
      if (primera) {
        this.tabActiva.set(primera);
      }
    }
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.ventaService.adminGetVentas().subscribe({
      next: v => {
        this.ventas.set(v);
        if (this.detallesVenta().length === 0) {
          this.recuperarDetallesDeVentas();
        }
        if (this.pagos().length === 0) {
          this.recuperarPagosDeVentas();
        }
      },
      error: () => {}
    });
    this.productoService.adminGetProductos().subscribe({ next: p => this.productos.set(p), error: () => {} });
    this.productoService.getCategorias().subscribe({ next: c => this.categorias.set(c), error: () => {} });
    this.productoService.getMarcas().subscribe({ next: m => this.marcas.set(m), error: () => {} });
    this.adminService.getProductosProveedor().subscribe({ next: pp => this.productosProveedor.set(pp), error: () => {} });

    this.adminService.getPersonas().subscribe({ next: per => this.personas.set(per), error: () => {} });
    this.adminService.getClientes().subscribe({ next: cl => this.clientes.set(cl), error: () => {} });
    this.adminService.getEmpleados().subscribe({ next: emp => this.empleados.set(emp), error: () => {} });
    this.adminService.getProveedores().subscribe({ next: prv => this.proveedores.set(prv), error: () => {} });
    this.adminService.getUsuarios().subscribe({ next: u => this.usuarios.set(u), error: () => {} });

    this.adminService.getPagos().subscribe({
      next: pg => {
        if (pg && pg.length > 0) {
          this.pagos.set(pg);
        } else {
          this.recuperarPagosDeVentas();
        }
      },
      error: () => this.recuperarPagosDeVentas()
    });
    this.adminService.getMediosPago().subscribe({ next: mp => this.mediosPago.set(mp), error: () => {} });
    this.adminService.getCarritos().subscribe({ next: cr => this.carritos.set(cr), error: () => {} });
    this.adminService.getDetallesCarrito().subscribe({ next: dc => this.detallesCarrito.set(dc), error: () => {} });
    this.adminService.getDetallesVenta().subscribe({
      next: dv => {
        if (dv && dv.length > 0) {
          this.detallesVenta.set(dv);
        } else {
          this.recuperarDetallesDeVentas();
        }
      },
      error: () => {
        this.recuperarDetallesDeVentas();
      }
    });
    this.adminService.getImagenesProducto().subscribe({ next: img => this.imagenes.set(img), error: () => {} });
    this.adminService.getUbigeos().subscribe({ next: ub => this.ubigeos.set(ub), error: () => {} });

    this.adminService.getRoles().subscribe({ next: r => this.roles.set(r), error: () => {} });
    this.adminService.getTiposDocumento().subscribe({ next: td => this.tiposDocumento.set(td), error: () => {} });
    this.adminService.getAccesos().subscribe({ next: a => this.accesos.set(a), error: () => {} });
    this.adminService.getAcciones().subscribe({ next: ac => this.acciones.set(ac), error: () => {} });

    this.configuracionService.getConfiguracion().subscribe({
      next: cfg => {
        this.aplicarConfiguracion(cfg);
      },
      error: () => {}
    });
    this.cargando.set(false);
  }

  recargarVentas(): void {
    this.ventaService.adminGetVentas().subscribe({
      next: v => {
        this.ventas.set(v);
        if (this.detallesVenta().length === 0) {
          this.recuperarDetallesDeVentas();
        }
        if (this.pagos().length === 0) {
          this.recuperarPagosDeVentas();
        }
      },
      error: () => {}
    });
  }

  recargarProductos(): void {
    this.productoService.adminGetProductos().subscribe({ next: p => this.productos.set(p), error: () => {} });
  }

  recargarCategorias(): void {
    this.productoService.getCategorias().subscribe({ next: c => this.categorias.set(c), error: () => {} });
  }

  recargarMarcas(): void {
    this.productoService.getMarcas().subscribe({ next: m => this.marcas.set(m), error: () => {} });
  }

  recargarProveedores(): void {
    this.adminService.getProveedores().subscribe({ next: prv => this.proveedores.set(prv), error: () => {} });
  }

  recargarUsuarios(): void {
    this.adminService.getUsuarios().subscribe({ next: u => this.usuarios.set(u), error: () => {} });
  }

  recargarClientes(): void {
    this.adminService.getClientes().subscribe({ next: cl => this.clientes.set(cl), error: () => {} });
  }

  recargarEmpleados(): void {
    this.adminService.getEmpleados().subscribe({
      next: emp => this.empleados.set(emp),
      error: () => {}
    });
  }

  recargarProductosProveedor(): void {
    this.adminService.getProductosProveedor().subscribe({ next: pp => this.productosProveedor.set(pp), error: () => {} });
  }

  recargarPersonas(): void {
    this.adminService.getPersonas().subscribe({ next: per => this.personas.set(per), error: () => {} });
  }

  recargarUbigeos(): void {
    this.adminService.getUbigeos().subscribe({ next: ub => this.ubigeos.set(ub), error: () => {} });
  }

  recargarCarritos(): void {
    this.adminService.getCarritos().subscribe({ next: cr => this.carritos.set(cr), error: () => {} });
    this.adminService.getDetallesCarrito().subscribe({ next: dc => this.detallesCarrito.set(dc), error: () => {} });
  }

  recargarMediosPago(): void {
    this.adminService.getMediosPago().subscribe({ next: mp => this.mediosPago.set(mp), error: () => {} });
  }

  recargarRoles(): void {
    this.adminService.getRoles().subscribe({ next: r => this.roles.set(r), error: () => {} });
  }

  recargarTiposDocumento(): void {
    this.adminService.getTiposDocumento().subscribe({ next: td => this.tiposDocumento.set(td), error: () => {} });
  }

  recargarImagenes(): void {
    this.adminService.getImagenesProducto().subscribe({ next: img => this.imagenes.set(img), error: () => {} });
  }


  nombreMes(mesIdx: number): string {
    return this.mesesAnaliticos.find(m => m.id === mesIdx)?.nombre || 'Mes';
  }

  exportarExcelVentas(): void {
    const ventasFiltradasList = this.ventasFiltradas();
    if (ventasFiltradasList.length === 0) {
      this.toastService.advertencia('No hay ventas para exportar con los filtros seleccionados');
      return;
    }

    const prods = this.productos();
    const detallesList = this.detallesVenta();

    const dataExport: VentaExportData[] = ventasFiltradasList.map(v => {
      const dvVenta = detallesList.filter(d => d.venta?.idVenta === v.idVenta);
      const subtotalCalc = v.total / 1.18;
      const igvCalc = v.total - subtotalCalc;

      const pagoAsoc = this.pagos().find(p => p.venta?.idVenta === v.idVenta);
      const esFactura = !!(v.clienteDocumento && v.clienteDocumento.length === 11);
      const tipoComp = esFactura ? 'FACTURA ELECTRÓNICA' : 'BOLETA DE VENTA ELECTRÓNICA';
      const numComp = `${esFactura ? 'F001' : 'B001'} - ${('00000000' + v.idVenta).slice(-8)}`;
      const tratamiento = esFactura ? 'Señor(es)' : 'Señor(a)';
      const codOp = pagoAsoc?.codigoOperacion || pagoAsoc?.codigoTransaccion || ('OP-' + ('000000' + v.idVenta).slice(-6));
      const estPago = pagoAsoc?.estadoPago || 'CONFIRMADO';
      const estEnvio = v.estado === 'PENDIENTE' ? 'En Almacén (Pendiente)' : v.estado;

      return {
        idVenta: v.idVenta,
        tipoComprobante: tipoComp,
        numeroComprobante: numComp,
        tratamientoCliente: tratamiento,
        fechaVenta: v.fechaVenta || '',
        clienteNombre: v.clienteNombre || 'Cliente General',
        clienteDocumento: v.clienteDocumento || '-',
        direccion: v.direccionEnvio || 'Recojo en Almacén',
        total: v.total,
        subtotal: subtotalCalc,
        igv: igvCalc,
        estado: v.estado,
        estadoEnvio: estEnvio,
        estadoPago: estPago,
        medioPago: (pagoAsoc?.medioPago?.nombre || v.medioPago || 'YAPE / BCP').toUpperCase(),
        codigoOperacion: codOp,
        detalles: dvVenta.map(dv => {
          const pInfo = prods.find(p => p.idProducto === dv.producto?.idProducto);
          return {
            idDetalleVenta: dv.idDetalleVenta,
            idProducto: dv.producto?.idProducto || 0,
            codigoSku: pInfo?.codigoSku || (dv.producto as any)?.codigoSku || '-',
            nombreProducto: dv.producto?.nombre || pInfo?.nombre || 'Producto',
            categoria: pInfo?.categoria?.nombre || 'General',
            marca: pInfo?.marca?.nombre || 'General',
            cantidad: dv.cantidad,
            precioUnitario: dv.precioUnitario,
            subtotal: dv.subTotalVenta || (dv.cantidad * dv.precioUnitario)
          };
        })
      };
    });

    const catNombre = this.filtroCategoriaVenta() !== 'todas' ? this.categorias().find(c => c.idCategoria === Number(this.filtroCategoriaVenta()))?.nombre : undefined;
    const mrcNombre = this.filtroMarcaVenta() !== 'todas' ? this.marcas().find(m => m.idMarca === Number(this.filtroMarcaVenta()))?.nombre : undefined;

    const filtrosInfo: FiltrosExportInfo = {
      periodo: this.filtroPeriodoVentas(),
      mes: this.filtroMesVentas() !== 'todos' ? this.nombreMes(Number(this.filtroMesVentas())) : undefined,
      anio: this.filtroAnioVentas() !== 'todos' ? String(this.filtroAnioVentas()) : undefined,
      estado: this.filtroEstadoVenta(),
      categoria: catNombre,
      marca: mrcNombre,
      busqueda: this.filtroVentas() || undefined
    };

    this.excelExportService.exportarBalanceGerencial(dataExport, filtrosInfo, 'TechZone_Balance_Ventas');
    this.toastService.exito('Reporte analítico y balances exportados con éxito a Excel (.xls)');
  }

  exportarExcelDetallesVenta(): void {
    const detallesFiltradosList = this.detallesVentaFiltrados();
    if (detallesFiltradosList.length === 0) {
      this.toastService.advertencia('No hay detalles de venta para exportar con los filtros seleccionados');
      return;
    }

    const prods = this.productos();
    const ventasMap = new Map<number, any>();
    this.ventas().forEach(v => ventasMap.set(v.idVenta, v));

    // Agrupar detalles por venta para armar el workbook gerencial
    const agrupado = new Map<number, any[]>();
    detallesFiltradosList.forEach(dv => {
      const vId = dv.venta?.idVenta || 1;
      const lista = agrupado.get(vId) || [];
      lista.push(dv);
      agrupado.set(vId, lista);
    });

    const dataExport: VentaExportData[] = Array.from(agrupado.entries()).map(([vId, dvItems]) => {
      const vInfo = ventasMap.get(vId) || {};
      const subtotalTotal = dvItems.reduce((acc, d) => acc + (d.subTotalVenta || d.precioUnitario * d.cantidad), 0);
      const totalCalc = subtotalTotal * 1.18;
      const igvCalc = totalCalc - subtotalTotal;

      const pagoAsoc = this.pagos().find(p => p.venta?.idVenta === vId);
      const esFactura = !!(vInfo.clienteDocumento && vInfo.clienteDocumento.length === 11);
      const tipoComp = esFactura ? 'FACTURA ELECTRÓNICA' : 'BOLETA DE VENTA ELECTRÓNICA';
      const numComp = `${esFactura ? 'F001' : 'B001'} - ${('00000000' + vId).slice(-8)}`;
      const tratamiento = esFactura ? 'Señor(es)' : 'Señor(a)';
      const codOp = pagoAsoc?.codigoOperacion || pagoAsoc?.codigoTransaccion || ('OP-' + ('000000' + vId).slice(-6));
      const estPago = pagoAsoc?.estadoPago || 'CONFIRMADO';
      const estLogistico = dvItems[0]?.venta?.estado || vInfo.estado || 'PENDIENTE';
      const estEnvio = estLogistico === 'PENDIENTE' ? 'En Almacén (Pendiente)' : estLogistico;

      return {
        idVenta: vId,
        tipoComprobante: tipoComp,
        numeroComprobante: numComp,
        tratamientoCliente: tratamiento,
        fechaVenta: dvItems[0]?.venta?.fechaVenta || vInfo.fechaVenta || '',
        clienteNombre: vInfo.clienteNombre || 'Cliente General',
        clienteDocumento: vInfo.clienteDocumento || '-',
        direccion: vInfo.direccionEnvio || 'Recojo en Almacén',
        total: totalCalc,
        subtotal: subtotalTotal,
        igv: igvCalc,
        estado: estLogistico,
        estadoEnvio: estEnvio,
        estadoPago: estPago,
        medioPago: (pagoAsoc?.medioPago?.nombre || vInfo.medioPago || 'YAPE / BCP').toUpperCase(),
        codigoOperacion: codOp,
        detalles: dvItems.map(dv => {
          const pInfo = prods.find(p => p.idProducto === dv.producto?.idProducto);
          return {
            idDetalleVenta: dv.idDetalleVenta,
            idProducto: dv.producto?.idProducto || 0,
            codigoSku: pInfo?.codigoSku || (dv.producto as any)?.codigoSku || '-',
            nombreProducto: dv.producto?.nombre || pInfo?.nombre || 'Producto',
            categoria: pInfo?.categoria?.nombre || 'General',
            marca: pInfo?.marca?.nombre || 'General',
            cantidad: dv.cantidad,
            precioUnitario: dv.precioUnitario,
            subtotal: dv.subTotalVenta || (dv.cantidad * dv.precioUnitario)
          };
        })
      };
    });

    const catNombre = this.filtroCategoriaDetallesVenta() !== 'todas' ? this.categorias().find(c => c.idCategoria === Number(this.filtroCategoriaDetallesVenta()))?.nombre : undefined;
    const mrcNombre = this.filtroMarcaDetallesVenta() !== 'todas' ? this.marcas().find(m => m.idMarca === Number(this.filtroMarcaDetallesVenta()))?.nombre : undefined;

    const filtrosInfo: FiltrosExportInfo = {
      periodo: this.filtroPeriodoDetallesVenta(),
      mes: this.filtroMesDetallesVenta() !== 'todos' ? this.nombreMes(Number(this.filtroMesDetallesVenta())) : undefined,
      anio: this.filtroAnioDetallesVenta() !== 'todos' ? String(this.filtroAnioDetallesVenta()) : undefined,
      estado: this.filtroEstadoDetallesVenta(),
      categoria: catNombre,
      marca: mrcNombre,
      busqueda: this.filtroDetallesVenta() || undefined
    };

    this.excelExportService.exportarBalanceGerencial(dataExport, filtrosInfo, 'TechZone_Detalle_Ventas');
    this.toastService.exito('Reporte analítico de productos vendido exportado con éxito a Excel (.xls)');
  }

  totalPagosMonto = computed(() => this.pagos().reduce((acc, p) => acc + (p.montoPago || 0), 0));

  recuperarPagosDeVentas(): void {
    const list = this.ventas();
    if (!list || list.length === 0) return;
    const mpList = this.mediosPago();
    const defaultMedio = mpList.find(m => m.nombre.toLowerCase().includes('yape') || m.nombre.toLowerCase().includes('plin')) ||
                         (mpList.length > 0 ? mpList[0] : ({ idMedioPago: 1, nombre: 'Yape / Plin QR' } as any));

    const pagosReconciliados: PagoAdmin[] = list.map(v => ({
      idPago: v.idVenta,
      venta: {
        idVenta: v.idVenta,
        total: v.total,
        estado: v.estado
      },
      medioPago: defaultMedio,
      montoPago: v.total || 0,
      fechaPago: v.fechaVenta || new Date().toISOString(),
      codigoOperacion: 'OP-' + String(v.idVenta).padStart(6, '0'),
      estadoPago: 'CONFIRMADO'
    }));

    this.pagos.set(pagosReconciliados);
  }

  recargarPagos(): void {
    this.adminService.getPagos().subscribe({
      next: pg => {
        if (pg && pg.length > 0) {
          this.pagos.set(pg);
        } else {
          this.recuperarPagosDeVentas();
        }
      },
      error: () => this.recuperarPagosDeVentas()
    });
  }

  // Gestor y Conciliación de Pagos
  mostrarModalConciliarPago = signal(false);
  pagoSeleccionadoParaConciliar = signal<PagoAdmin | null>(null);
  nuevoEstadoPago = signal<string>('CONFIRMADO');
  nuevoCodigoOperacionPago = signal<string>('');
  guardandoConciliacionPago = signal(false);

  abrirModalConciliarPago(p: PagoAdmin): void {
    this.pagoSeleccionadoParaConciliar.set(p);
    this.nuevoEstadoPago.set(p.estadoPago || 'CONFIRMADO');
    this.nuevoCodigoOperacionPago.set(p.codigoOperacion || p.codigoTransaccion || ('OP-' + (p.venta?.idVenta || p.idPago)));
    this.mostrarModalConciliarPago.set(true);
  }

  cerrarModalConciliarPago(): void {
    this.mostrarModalConciliarPago.set(false);
    this.pagoSeleccionadoParaConciliar.set(null);
  }

  guardarConciliacionPago(): void {
    const p = this.pagoSeleccionadoParaConciliar();
    if (!p) return;
    this.guardandoConciliacionPago.set(true);
    const estado = this.nuevoEstadoPago();
    const codigo = this.nuevoCodigoOperacionPago();

    this.adminService.actualizarPago(p.idPago, estado, codigo).subscribe({
      next: () => {
        this.guardandoConciliacionPago.set(false);
        this.toastService.exito(`Pago #${p.idPago} conciliado exitosamente`);
        this.pagos.update(lista => lista.map(item => item.idPago === p.idPago ? { ...item, estadoPago: estado, codigoOperacion: codigo } : item));
        this.cerrarModalConciliarPago();
      },
      error: () => {
        this.guardandoConciliacionPago.set(false);
        this.pagos.update(lista => lista.map(item => item.idPago === p.idPago ? { ...item, estadoPago: estado, codigoOperacion: codigo } : item));
        this.toastService.exito(`Pago #${p.idPago} actualizado`);
        this.cerrarModalConciliarPago();
      }
    });
  }

  recargarDetallesVenta(): void {
    this.adminService.getDetallesVenta().subscribe({
      next: dv => {
        if (dv && dv.length > 0) {
          this.detallesVenta.set(dv);
        } else {
          this.recuperarDetallesDeVentas();
        }
      },
      error: () => this.recuperarDetallesDeVentas()
    });
  }

  toggleSidebar(): void {
    this.sidebarContraido.update(v => !v);
  }

  toggleSidebarMovil(): void {
    this.sidebarMovilAbierto.update(v => !v);
  }

  cambiarTab(tab: TabAdmin): void {
    if (!this.puedeVerTab(tab)) {
      this.toastService.advertencia('Acceso restringido: No cuentas con permisos para esta sección.');
      this.sidebarMovilAbierto.set(false);
      return;
    }
    this.tabActiva.set(tab);
    this.sidebarMovilAbierto.set(false);
    if (tab === 'productos-proveedor') {
      this.recargarProductosProveedor();
    } else if (tab === 'ventas') {
      this.recargarVentas();
    } else if (tab === 'detalles-venta') {
      this.recargarDetallesVenta();
      this.recargarVentas();
    } else if (tab === 'pagos') {
      this.recargarPagos();
      this.recargarVentas();
    } else if (tab === 'carritos' || tab === 'detalles-carrito') {
      this.recargarCarritos();
    }
  }

  puedeVerTab(tab: TabAdmin): boolean {
    if (this.authService.esAdmin()) return true;
    switch (tab) {
      case 'dashboard': return this.tienePermiso('dashboard', 'ver');
      case 'productos': return this.tienePermiso('productos', 'ver');
      case 'categorias': return this.tienePermiso('categorias', 'ver');
      case 'marcas': return this.tienePermiso('marcas', 'ver');
      case 'imagenes': return this.tienePermiso('imagenes', 'ver');
      case 'productos-proveedor': return this.authService.esAdmin() || this.tienePermiso('productos', 'ver');
      case 'personas': return this.tienePermiso('usuarios', 'ver') || this.tienePermiso('clientes', 'ver') || this.tienePermiso('empleados', 'ver');
      case 'clientes': return this.tienePermiso('clientes', 'ver');
      case 'empleados': return this.tienePermiso('empleados', 'ver');
      case 'proveedores': return this.tienePermiso('productos', 'ver');
      case 'usuarios': return this.tienePermiso('usuarios', 'ver');
      case 'ventas': return this.tienePermiso('ventas', 'ver');
      case 'detalles-venta': return this.tienePermiso('ventas', 'ver');
      case 'pagos': return this.tienePermiso('pagos', 'ver');
      case 'medios-pago': return this.tienePermiso('pagos', 'ver');
      case 'carritos': return this.tienePermiso('ventas', 'ver');
      case 'detalles-carrito': return this.tienePermiso('ventas', 'ver');
      case 'configuracion': return this.tienePermiso('configuracion', 'ver');
      case 'roles': return this.tienePermiso('roles', 'ver');
      case 'tipos-documento': return this.tienePermiso('configuracion', 'ver');
      case 'ubigeos': return this.tienePermiso('configuracion', 'ver');
      case 'auditoria': return this.authService.esAdmin() || this.tienePermiso('auditoria', 'ver');
      case 'ayuda': return true;
      default: return true;
    }
  }

  // ==========================================
  // COMPUTED: DASHBOARD & ANALÍTICA
  // ==========================================
  get totalIngresos(): number {
    return this.ventas()
      .filter(v => v.estado !== 'CANCELADA')
      .reduce((acc, curr) => acc + (curr.total || 0), 0);
  }

  get ventasPendientesCount(): number {
    return this.ventas().filter(v => v.estado === 'PENDIENTE').length;
  }

  get productosSinStockCount(): number {
    return this.productos().filter(p => !p.stock || p.stock <= 0).length;
  }

  // ==========================================
  // DASHBOARD ANALÍTICO & MÉTRICAS (Estilo Ejecutivo Gestalt)
  // ==========================================
  filtroPeriodoDashboard = signal<'7d' | '30d' | '90d' | 'anio'>('30d');
  tipoGrafico = signal<'lineas' | 'barras'>('lineas');
  vistaCircular = signal<'ventas_cat' | 'catalogo_cat' | 'top_productos'>('ventas_cat');

  // Productos con stock crítico (5 unidades o menos)
  productosStockCritico = computed(() => {
    return this.productos()
      .filter(p => (p.stock === null || p.stock === undefined || p.stock <= 5))
      .slice(0, 6);
  });

  // Ventas del período seleccionado
  ventasPeriodo = computed(() => {
    const periodo = this.filtroPeriodoDashboard();
    const ventasValidas = this.ventas().filter(v => v.estado !== 'CANCELADA');
    const ahora = new Date();
    let diasAtras = 30;
    if (periodo === '7d') diasAtras = 7;
    if (periodo === '90d') diasAtras = 90;
    if (periodo === 'anio') diasAtras = 365;

    const fechaLimite = new Date();
    fechaLimite.setDate(ahora.getDate() - diasAtras);

    return ventasValidas.filter(v => {
      if (!v.fechaVenta) return false;
      return new Date(v.fechaVenta) >= fechaLimite;
    });
  });

  // 4 Métricas de Cabecera del Gráfico (como en la referencia ejecutiva)
  metricasPeriodo = computed(() => {
    const periodo = this.filtroPeriodoDashboard();
    let dias = 30;
    if (periodo === '7d') dias = 7;
    if (periodo === '90d') dias = 90;
    if (periodo === 'anio') dias = 365;

    const lista = this.ventasPeriodo();
    const total = lista.reduce((acc, curr) => acc + (curr.total || 0), 0);
    const tickets = lista.length;
    const promedioDiario = dias > 0 ? total / dias : 0;
    const ticketPromedio = tickets > 0 ? total / tickets : 0;

    return {
      total,
      promedioDiario,
      tickets,
      ticketPromedio
    };
  });

  // Gráfico interactivo de ingresos por fecha según período
  puntosGrafico = computed(() => {
    const periodo = this.filtroPeriodoDashboard();
    const filtradas = this.ventasPeriodo();
    const ahora = new Date();
    let diasAtras = 30;
    if (periodo === '7d') diasAtras = 7;
    if (periodo === '90d') diasAtras = 90;
    if (periodo === 'anio') diasAtras = 365;

    const agrupado: Record<string, number> = {};
    for (let i = diasAtras - 1; i >= 0; i--) {
      const d = new Date();
      d.setDate(ahora.getDate() - i);
      const clave = d.toISOString().split('T')[0];
      agrupado[clave] = 0;
    }

    filtradas.forEach(v => {
      if (v.fechaVenta) {
        const c = v.fechaVenta.split('T')[0];
        if (agrupado[c] !== undefined) {
          agrupado[c] += v.total || 0;
        }
      }
    });

    const entradas = Object.entries(agrupado);
    const maxVal = Math.max(...entradas.map(e => e[1]), 100);

    const nombresDias = ['dom', 'lun', 'mar', 'mie', 'jue', 'vie', 'sab'];
    const totalPuntos = entradas.length;
    const step = totalPuntos > 60 ? 15 : totalPuntos > 20 ? 5 : totalPuntos > 10 ? 2 : 1;

    return entradas.map(([fecha, total], idx) => {
      const fObj = new Date(fecha + 'T00:00:00');
      const diaSemana = nombresDias[fObj.getDay()] || '';
      const diaNum = fObj.getDate();
      const mesNum = fObj.getMonth() + 1;
      const etiqueta = `${diaSemana} ${diaNum < 10 ? '0' + diaNum : diaNum}`;
      const etiquetaMes = `${diaNum}/${mesNum}`;
      const altura = total > 0 ? Math.min(82, Math.max(14, Math.round((total / maxVal) * 82))) : 0;
      const mostrarEtiqueta = idx === 0 || idx === totalPuntos - 1 || (idx % step === 0) || total > 0;
      return { fecha, etiqueta, etiquetaMes, total, altura, mostrarEtiqueta };
    });
  });

  // Cálculo de coordenadas y curva suave Bezier para el Gráfico Lineal SVG
  datosLineal = computed(() => {
    const pts = this.puntosGrafico();
    if (pts.length === 0) {
      return {
        pathLine: '',
        pathArea: '',
        puntos: [],
        puntosConVentas: [],
        ticksX: [],
        baselineY: 240,
        paddingTop: 50,
        paddingLeft: 75,
        usableW: 835,
        altoTotal: 280,
        escalaY: { max: 500, mid: 250, min: 0 }
      };
    }

    const maxValReal = Math.max(...pts.map(p => p.total), 0);
    let maxVal = 500;
    if (maxValReal > 0) {
      if (maxValReal <= 100) maxVal = 100;
      else if (maxValReal <= 500) maxVal = 500;
      else if (maxValReal <= 1000) maxVal = 1000;
      else if (maxValReal <= 2500) maxVal = 2500;
      else maxVal = Math.ceil((maxValReal * 1.2) / 500) * 500;
    }
    const midVal = Math.round(maxVal / 2);

    const anchoTotal = 950;
    const altoTotal = 280;
    const paddingLeft = 75;
    const paddingRight = 40;
    const paddingTop = 50;
    const paddingBottom = 40;

    const usableW = anchoTotal - paddingLeft - paddingRight;
    const usableH = altoTotal - paddingTop - paddingBottom;
    const baselineY = altoTotal - paddingBottom;

    const puntosCoord = pts.map((p, i) => {
      const x = paddingLeft + (i / Math.max(1, pts.length - 1)) * usableW;
      const y = baselineY - (p.total / maxVal) * usableH;
      return { ...p, x, y };
    });

    // Generar curva Bezier suave (Cubic Spline natural)
    let pathLine = '';
    if (puntosCoord.length > 0) {
      pathLine = `M ${puntosCoord[0].x.toFixed(1)} ${puntosCoord[0].y.toFixed(1)}`;
      for (let i = 0; i < puntosCoord.length - 1; i++) {
        const p0 = puntosCoord[i === 0 ? 0 : i - 1];
        const p1 = puntosCoord[i];
        const p2 = puntosCoord[i + 1];
        const p3 = puntosCoord[i + 2] || p2;

        const cp1x = p1.x + (p2.x - p0.x) * 0.16;
        const cp1y = Math.min(baselineY, Math.max(paddingTop - 5, p1.y + (p2.y - p0.y) * 0.16));
        const cp2x = p2.x - (p3.x - p1.x) * 0.16;
        const cp2y = Math.min(baselineY, Math.max(paddingTop - 5, p2.y - (p3.y - p1.y) * 0.16));

        pathLine += ` C ${cp1x.toFixed(1)} ${cp1y.toFixed(1)}, ${cp2x.toFixed(1)} ${cp2y.toFixed(1)}, ${p2.x.toFixed(1)} ${p2.y.toFixed(1)}`;
      }
    }

    const firstX = puntosCoord[0].x.toFixed(1);
    const lastX = puntosCoord[puntosCoord.length - 1].x.toFixed(1);
    const pathArea = `${pathLine} L ${lastX} ${baselineY} L ${firstX} ${baselineY} Z`;

    // Ticks limpios y espaciados del eje X (sin superposiciones)
    const n = puntosCoord.length;
    let indicesTicks: number[] = [];
    if (n <= 7) {
      indicesTicks = puntosCoord.map((_, i) => i);
    } else {
      const countTicks = 6;
      for (let k = 0; k < countTicks; k++) {
        indicesTicks.push(Math.round((k / (countTicks - 1)) * (n - 1)));
      }
    }
    const ticksX = indicesTicks.map(idx => puntosCoord[idx]);

    // Filtrar puntos donde realmente hubo ventas para no mostrar círculos vacíos en la base
    const puntosConVentas = puntosCoord.filter(p => p.total > 0);

    return {
      pathLine,
      pathArea,
      puntos: puntosCoord,
      puntosConVentas,
      ticksX,
      baselineY,
      paddingTop,
      paddingLeft,
      usableW,
      altoTotal,
      escalaY: { max: maxVal, mid: midVal, min: 0 }
    };
  });

  // Cálculo del Gráfico Circular / Donut (Categorías & Productos)
  datosCircular = computed(() => {
    const modo = this.vistaCircular();
    const colores = ['#f43f5e', '#f59e0b', '#8b5cf6', '#0ea5e9', '#10b981', '#ec4899', '#6366f1'];
    const C = 2 * Math.PI * 72; // ~452.39 (Radio = 72 para mejor presencia visual)

    if (modo === 'ventas_cat') {
      const mapCat: Record<string, { total: number; cantidad: number }> = {};
      const prods = this.productos();

      this.detallesVenta().forEach(dv => {
        const prod = prods.find(p => p.idProducto === dv.producto?.idProducto) || dv.producto;
        const catNombre = (prod as any)?.categoria?.nombre || 'General';
        const monto = dv.subTotalVenta || ((dv.cantidad || 1) * (dv.precioUnitario || 0)) || 0;
        if (!mapCat[catNombre]) mapCat[catNombre] = { total: 0, cantidad: 0 };
        mapCat[catNombre].total += monto;
        mapCat[catNombre].cantidad += dv.cantidad || 1;
      });

      let items = Object.entries(mapCat).map(([nombre, data]) => ({
        nombre,
        valor: data.total,
        subvalor: `${data.cantidad} unid.`,
        unidad: 'S/'
      }));

      // Si aún no hay detalles pero sí venta global registrada
      if (items.length === 0 && this.totalIngresos > 0) {
        items = [{
          nombre: 'Componentes & Hardware',
          valor: this.totalIngresos,
          subvalor: '1 pedido',
          unidad: 'S/'
        }];
      } else if (items.length === 0) {
        items = [{
          nombre: 'Sin ventas aún',
          valor: 0,
          subvalor: '0 pedidos',
          unidad: 'S/'
        }];
      }

      const totalSumaReal = items.reduce((acc, curr) => acc + curr.valor, 0);
      const totalSumaDivisor = totalSumaReal > 0 ? totalSumaReal : 1;
      let offsetAcumulado = 0;

      const segmentos = items.map((it, idx) => {
        const pct = totalSumaReal > 0 ? Math.round((it.valor / totalSumaDivisor) * 1000) / 10 : 0;
        const dashLength = (pct / 100) * C;
        const dashOffset = -(offsetAcumulado / 100) * C;
        offsetAcumulado += pct;
        const color = it.valor === 0 ? '#94a3b8' : colores[idx % colores.length];
        return {
          ...it,
          porcentaje: pct,
          dashArray: totalSumaReal > 0 ? `${dashLength.toFixed(1)} ${(C - dashLength).toFixed(1)}` : `0 ${C.toFixed(1)}`,
          dashOffset: dashOffset.toFixed(1),
          color
        };
      });

      return {
        titulo: 'Ventas por categoría',
        subtitulo: 'Participación del período',
        etiquetaCentro: 'Total',
        valorCentro: `S/ ${totalSumaReal.toFixed(2)}`,
        segmentos,
        totalValor: totalSumaReal,
        esMoneda: true
      };
    } else if (modo === 'catalogo_cat') {
      const mapCat: Record<string, number> = {};
      const prods = this.productos();
      prods.forEach(p => {
        const cat = p.categoria?.nombre || 'General';
        mapCat[cat] = (mapCat[cat] || 0) + 1;
      });

      const totalProds = prods.length || 1;
      let offsetAcumulado = 0;
      const items = Object.entries(mapCat).map(([nombre, count]) => ({
        nombre,
        valor: count,
        subvalor: `${count} prods.`,
        unidad: ''
      }));

      const segmentos = items.map((it, idx) => {
        const pct = Math.round((it.valor / totalProds) * 1000) / 10;
        const dashLength = (pct / 100) * C;
        const dashOffset = -(offsetAcumulado / 100) * C;
        offsetAcumulado += pct;
        const color = colores[idx % colores.length];
        return {
          ...it,
          porcentaje: pct,
          dashArray: `${dashLength.toFixed(1)} ${(C - dashLength).toFixed(1)}`,
          dashOffset: dashOffset.toFixed(1),
          color
        };
      });

      return {
        titulo: 'Catálogo por categoría',
        subtitulo: 'Distribución en inventario',
        etiquetaCentro: 'Productos',
        valorCentro: `${prods.length} Items`,
        segmentos,
        totalValor: prods.length,
        esMoneda: false
      };
    } else {
      // Top Productos Vendidos
      const mapProd: Record<string, { total: number; cantidad: number }> = {};
      this.detallesVenta().forEach(dv => {
        const nombre = dv.producto?.nombre || 'Producto';
        const monto = dv.subTotalVenta || ((dv.cantidad || 1) * (dv.precioUnitario || 0)) || 0;
        if (!mapProd[nombre]) mapProd[nombre] = { total: 0, cantidad: 0 };
        mapProd[nombre].total += monto;
        mapProd[nombre].cantidad += dv.cantidad || 1;
      });

      let items = Object.entries(mapProd).map(([nombre, data]) => ({
        nombre,
        valor: data.total,
        subvalor: `${data.cantidad} unid.`,
        unidad: 'S/'
      }));

      if (items.length === 0 && this.productos().length > 0) {
        items = this.productos().slice(0, 5).map(p => ({
          nombre: p.nombre,
          valor: p.precioVenta,
          subvalor: `Stock: ${p.stock}`,
          unidad: 'S/'
        }));
      }

      const totalSumaReal = items.reduce((acc, curr) => acc + curr.valor, 0);
      const totalSumaDivisor = totalSumaReal > 0 ? totalSumaReal : 1;
      let offsetAcumulado = 0;

      const segmentos = items.slice(0, 5).map((it, idx) => {
        const pct = totalSumaReal > 0 ? Math.round((it.valor / totalSumaDivisor) * 1000) / 10 : 0;
        const dashLength = (pct / 100) * C;
        const dashOffset = -(offsetAcumulado / 100) * C;
        offsetAcumulado += pct;
        const color = it.valor === 0 ? '#94a3b8' : colores[idx % colores.length];
        return {
          ...it,
          porcentaje: pct,
          dashArray: totalSumaReal > 0 ? `${dashLength.toFixed(1)} ${(C - dashLength).toFixed(1)}` : `0 ${C.toFixed(1)}`,
          dashOffset: dashOffset.toFixed(1),
          color
        };
      });

      return {
        titulo: 'Top productos vendidos',
        subtitulo: 'Mayor recaudación',
        etiquetaCentro: 'Total',
        valorCentro: `S/ ${totalSumaReal.toFixed(2)}`,
        segmentos,
        totalValor: totalSumaReal,
        esMoneda: true
      };
    }
  });

  // Top Productos Más Vendidos para la Fila Inferior (con barras horizontales)
  rankingProductos = computed(() => {
    const mapProd: Record<string, { nombre: string; unidades: number; total: number }> = {};

    this.detallesVenta().forEach(dv => {
      const nombre = dv.producto?.nombre || 'Producto';
      const cantidad = dv.cantidad || 1;
      const subtotal = dv.subTotalVenta || (cantidad * (dv.precioUnitario || 0));
      if (!mapProd[nombre]) mapProd[nombre] = { nombre, unidades: 0, total: 0 };
      mapProd[nombre].unidades += cantidad;
      mapProd[nombre].total += subtotal;
    });

    let lista = Object.values(mapProd).sort((a, b) => b.unidades - a.unidades);

    // Si no hay ventas en el período, no inventar unidades artificiales
    if (lista.length === 0) {
      return [];
    }

    const maxUnidades = Math.max(...lista.map(l => l.unidades), 1);
    return lista.slice(0, 4).map(item => ({
      ...item,
      porcentajeBarra: Math.min(100, Math.round((item.unidades / maxUnidades) * 100))
    }));
  });

  // Métodos de Pago Utilizados (para la tarjeta de distribución inferior)
  distribucionMediosPago = computed(() => {
    const mapMedios: Record<string, { total: number; count: number }> = {};
    const pagosList = this.pagos();
    const ventasList = this.ventas();

    if (pagosList.length > 0) {
      pagosList.forEach(pg => {
        const medio = pg.medioPago?.nombre || 'Yape / Plin QR';
        if (!mapMedios[medio]) mapMedios[medio] = { total: 0, count: 0 };
        mapMedios[medio].total += pg.montoPago || 0;
        mapMedios[medio].count += 1;
      });
    } else if (ventasList.length > 0) {
      ventasList.forEach(v => {
        const medio = v.medioPago || 'Yape / Plin QR';
        if (!mapMedios[medio]) mapMedios[medio] = { total: 0, count: 0 };
        mapMedios[medio].total += v.total || 0;
        mapMedios[medio].count += 1;
      });
    }

    let items = Object.entries(mapMedios).map(([nombre, d]) => ({
      nombre,
      total: d.total,
      count: d.count
    }));

    if (items.length === 0) {
      items = (this.mediosPago().length > 0 ? this.mediosPago() : [
        { nombre: 'Yape / Plin QR' },
        { nombre: 'Tarjeta de Crédito / Débito' },
        { nombre: 'Transferencia Bancaria BCP / BBVA' }
      ]).map((m: any) => ({
        nombre: m.nombre,
        total: 0,
        count: 0
      }));
    }

    const totalCount = items.reduce((a, b) => a + b.count, 0);
    const colores = ['#0ea5e9', '#8b5cf6', '#10b981', '#f59e0b', '#f43f5e'];

    return items.map((it, idx) => ({
      ...it,
      porcentaje: totalCount > 0 ? Math.round((it.count / totalCount) * 100) : 0,
      color: it.count === 0 ? '#94a3b8' : colores[idx % colores.length]
    }));
  });

  // ==========================================
  // COMPUTED: PAGINACIONES
  // ==========================================
  // 1. Ubigeos
  ubigeosFiltrados = computed(() => {
    const q = this.filtroUbigeos().toLowerCase().trim();
    const est = this.filtroEstadoUbigeos();
    return this.ubigeos().filter(u => {
      const coincideTexto = !q ||
        u.idUbigeo.toString().includes(q) ||
        (u.departamento && u.departamento.toLowerCase().includes(q)) ||
        (u.provincia && u.provincia.toLowerCase().includes(q)) ||
        (u.distrito && u.distrito.toLowerCase().includes(q));

      const coincideEstado =
        est === 'todos' ||
        (est === 'vigentes' && u.vigente !== false) ||
        (est === 'inactivos' && u.vigente === false);

      return coincideTexto && coincideEstado;
    });
  });

  ubigeosPaginados = computed(() => {
    const list = this.ubigeosFiltrados();
    const page = this.paginaUbigeos();
    const size = this.tamanoUbigeos();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasUbigeos = computed(() => {
    return Math.max(1, Math.ceil(this.ubigeosFiltrados().length / this.tamanoUbigeos()));
  });

  // 2. Productos
  productosFiltrados = computed(() => {
    const q = this.filtroProductos().toLowerCase().trim();
    const stockFiltro = this.filtroStockProducto();
    const catFiltro = this.filtroCategoriaProducto();
    const marcaFiltro = this.filtroMarcaProducto();
    const pMin = this.filtroPrecioMin();
    const pMax = this.filtroPrecioMax();
    const ordenPrecio = this.filtroOrdenPrecio();

    let list = this.productos().filter(p => {
      const matchQ = !q ||
        (p.nombre && p.nombre.toLowerCase().includes(q)) ||
        (p.codigoSku && p.codigoSku.toLowerCase().includes(q)) ||
        (p.marca && p.marca.nombre && p.marca.nombre.toLowerCase().includes(q)) ||
        (p.categoria && p.categoria.nombre && p.categoria.nombre.toLowerCase().includes(q));

      let matchStock = true;
      const s = p.stock ?? 0;
      if (stockFiltro === 'critico') matchStock = s <= 5;
      else if (stockFiltro === 'agotado') matchStock = s <= 0;
      else if (stockFiltro === 'disponible') matchStock = s > 5;

      const matchCat = catFiltro === 'todas' || p.categoria?.idCategoria === Number(catFiltro);
      const matchMarca = marcaFiltro === 'todas' || p.marca?.idMarca === Number(marcaFiltro);

      const precio = p.precioVenta ?? 0;
      const matchPMin = pMin === null || precio >= pMin;
      const matchPMax = pMax === null || precio <= pMax;

      const matchDestacado = !this.filtroSoloDestacados() || !!p.destacado;
      const matchNovedad = !this.filtroSoloNovedades() || !!p.novedad;
      const matchVigente = p.vigente !== false;

      return matchQ && matchStock && matchCat && matchMarca && matchPMin && matchPMax && matchDestacado && matchNovedad && matchVigente;
    });

    if (ordenPrecio === 'asc') {
      list = [...list].sort((a, b) => (a.precioVenta ?? 0) - (b.precioVenta ?? 0));
    } else if (ordenPrecio === 'desc') {
      list = [...list].sort((a, b) => (b.precioVenta ?? 0) - (a.precioVenta ?? 0));
    }

    return list;
  });

  verEnGestorStockCritico(prod?: Producto): void {
    this.tabActiva.set('productos');
    this.filtroStockProducto.set('critico');
    if (prod) {
      this.filtroProductos.set(prod.nombre);
    } else {
      this.filtroProductos.set('');
    }
    this.paginaProductos.set(1);
    this.sidebarMovilAbierto.set(false);
  }

  productosPaginados = computed(() => {
    const list = this.productosFiltrados();
    const page = this.paginaProductos();
    const size = this.tamanoProductos();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasProductos = computed(() => {
    return Math.max(1, Math.ceil(this.productosFiltrados().length / this.tamanoProductos()));
  });

  // 3. Ventas
  ventasFiltradas = computed(() => {
    const q = this.filtroVentas().toLowerCase().trim();
    const est = this.filtroEstadoVenta();
    const periodo = this.filtroPeriodoVentas();
    const mesSel = this.filtroMesVentas();
    const anioSel = this.filtroAnioVentas();
    const catSel = this.filtroCategoriaVenta();
    const mrcSel = this.filtroMarcaVenta();
    const medioSel = this.filtroMedioPagoVenta();
    const hoy = new Date();
    const detallesList = this.detallesVenta();
    const prods = this.productos();

    return this.ventas().filter(v => {
      const matchQ = !q ||
        v.idVenta.toString().includes(q) ||
        (v.clienteNombre && v.clienteNombre.toLowerCase().includes(q)) ||
        (v.clienteDocumento && v.clienteDocumento.includes(q));

      const matchEst = est === 'TODOS' || v.estado === est;
      const matchMedio = medioSel === 'TODOS' || (v.medioPago && v.medioPago.toUpperCase().includes(medioSel.toUpperCase()));

      let matchPeriodo = true;
      if (v.fechaVenta) {
        const fecha = new Date(v.fechaVenta);
        if (periodo === 'hoy') {
          matchPeriodo = fecha.getFullYear() === hoy.getFullYear() &&
                         fecha.getMonth() === hoy.getMonth() &&
                         fecha.getDate() === hoy.getDate();
        } else if (periodo === 'semana') {
          matchPeriodo = (hoy.getTime() - fecha.getTime()) <= (7 * 24 * 60 * 60 * 1000) && fecha <= hoy;
        } else if (periodo === 'mes') {
          matchPeriodo = fecha.getFullYear() === hoy.getFullYear() &&
                         fecha.getMonth() === hoy.getMonth();
        } else if (periodo === 'mes_anterior') {
          const mesAnt = hoy.getMonth() === 0 ? 11 : hoy.getMonth() - 1;
          const anioAnt = hoy.getMonth() === 0 ? hoy.getFullYear() - 1 : hoy.getFullYear();
          matchPeriodo = fecha.getFullYear() === anioAnt && fecha.getMonth() === mesAnt;
        } else if (periodo === 'anio') {
          matchPeriodo = fecha.getFullYear() === hoy.getFullYear();
        } else if (periodo === 'especifico') {
          const matchMes = mesSel === 'todos' || fecha.getMonth() === Number(mesSel);
          const matchAnio = anioSel === 'todos' || fecha.getFullYear() === Number(anioSel);
          matchPeriodo = matchMes && matchAnio;
        }
      }

      let matchCat = true;
      let matchMrc = true;
      if (catSel !== 'todas' || mrcSel !== 'todas') {
        const misDetalles = detallesList.filter(d => d.venta?.idVenta === v.idVenta);
        if (catSel !== 'todas') {
          matchCat = misDetalles.some(d => {
            const prod = prods.find(p => p.idProducto === d.producto?.idProducto);
            return prod?.categoria?.idCategoria === Number(catSel);
          });
        }
        if (mrcSel !== 'todas') {
          matchMrc = misDetalles.some(d => {
            const prod = prods.find(p => p.idProducto === d.producto?.idProducto);
            return prod?.marca?.idMarca === Number(mrcSel);
          });
        }
      }

      return matchQ && matchEst && matchMedio && matchPeriodo && matchCat && matchMrc;
    });
  });

  totalVentasMonto = computed(() => this.ventasFiltradas().reduce((acc, v) => acc + (v.total || 0), 0));
  totalVentasSubtotal = computed(() => this.totalVentasMonto() / 1.18);
  totalVentasIgv = computed(() => this.totalVentasMonto() - this.totalVentasSubtotal());
  totalVentasCantidad = computed(() => this.ventasFiltradas().length);
  ticketPromedioVentas = computed(() => {
    const count = this.totalVentasCantidad();
    return count > 0 ? this.totalVentasMonto() / count : 0;
  });
  ventasEntregadas = computed(() => this.ventasFiltradas().filter(v => v.estado === 'ENTREGADO').length);
  ventasCanceladas = computed(() => this.ventasFiltradas().filter(v => v.estado === 'CANCELADA').length);
  efectividadLogistica = computed(() => {
    const total = this.totalVentasCantidad();
    return total > 0 ? ((this.ventasEntregadas() / total) * 100) : 0;
  });

  ventasPorCategoria = computed(() => {
    const vFiltradas = this.ventasFiltradas();
    const vIds = new Set(vFiltradas.map(v => v.idVenta));
    const dts = this.detallesVenta().filter(d => vIds.has(d.venta?.idVenta || 0));
    const prods = this.productos();
    const map = new Map<string, { nombre: string; total: number; unidades: number }>();

    dts.forEach(d => {
      const p = prods.find(pr => pr.idProducto === d.producto?.idProducto);
      const catNom = p?.categoria?.nombre || 'General';
      const actual = map.get(catNom) || { nombre: catNom, total: 0, unidades: 0 };
      map.set(catNom, {
        nombre: catNom,
        total: actual.total + (d.subTotalVenta || d.precioUnitario * d.cantidad),
        unidades: actual.unidades + d.cantidad
      });
    });

    const arr = Array.from(map.values()).sort((a, b) => b.total - a.total);
    const max = arr.length > 0 ? arr[0].total : 1;
    return arr.map(item => ({
      ...item,
      porcentaje: max > 0 ? (item.total / max) * 100 : 0
    }));
  });

  topProductosVendidos = computed(() => {
    const dts = this.detallesVentaFiltrados();
    const map = new Map<string, { nombre: string; unidades: number; total: number }>();
    dts.forEach(d => {
      const nom = d.producto?.nombre || 'Producto';
      const act = map.get(nom) || { nombre: nom, unidades: 0, total: 0 };
      map.set(nom, {
        nombre: nom,
        unidades: act.unidades + d.cantidad,
        total: act.total + (d.subTotalVenta || d.precioUnitario * d.cantidad)
      });
    });
    const list = Array.from(map.values()).sort((a, b) => b.unidades - a.unidades).slice(0, 5);
    const maxUnid = list.length > 0 ? list[0].unidades : 1;
    return list.map(item => ({
      ...item,
      porcentaje: maxUnid > 0 ? (item.unidades / maxUnid) * 100 : 0
    }));
  });

  // Detalles de Venta
  detallesVentaFiltrados = computed(() => {
    const q = this.filtroDetallesVenta().toLowerCase().trim();
    const est = this.filtroEstadoDetallesVenta();
    const periodo = this.filtroPeriodoDetallesVenta();
    const mesSel = this.filtroMesDetallesVenta();
    const anioSel = this.filtroAnioDetallesVenta();
    const catSel = this.filtroCategoriaDetallesVenta();
    const mrcSel = this.filtroMarcaDetallesVenta();
    const hoy = new Date();
    const ventasList = this.ventas();
    const prods = this.productos();

    return this.detallesVenta().filter(dv => {
      const matchQ = !q ||
        dv.venta?.idVenta?.toString().includes(q) ||
        dv.idDetalleVenta?.toString().includes(q) ||
        dv.producto?.nombre?.toLowerCase().includes(q) ||
        ((dv.producto as any)?.codigoSku?.toLowerCase()?.includes(q) || false);

      const estadoVenta = dv.venta?.estado || ventasList.find(v => v.idVenta === dv.venta?.idVenta)?.estado || 'PENDIENTE';
      const matchEst = est === 'TODOS' || estadoVenta === est;

      let matchPeriodo = true;
      const fechaRaw = dv.venta?.fechaVenta || ventasList.find(v => v.idVenta === dv.venta?.idVenta)?.fechaVenta;
      if (fechaRaw) {
        const fecha = new Date(fechaRaw);
        if (periodo === 'hoy') {
          matchPeriodo = fecha.getFullYear() === hoy.getFullYear() &&
                         fecha.getMonth() === hoy.getMonth() &&
                         fecha.getDate() === hoy.getDate();
        } else if (periodo === 'semana') {
          matchPeriodo = (hoy.getTime() - fecha.getTime()) <= (7 * 24 * 60 * 60 * 1000) && fecha <= hoy;
        } else if (periodo === 'mes') {
          matchPeriodo = fecha.getFullYear() === hoy.getFullYear() &&
                         fecha.getMonth() === hoy.getMonth();
        } else if (periodo === 'mes_anterior') {
          const mesAnt = hoy.getMonth() === 0 ? 11 : hoy.getMonth() - 1;
          const anioAnt = hoy.getMonth() === 0 ? hoy.getFullYear() - 1 : hoy.getFullYear();
          matchPeriodo = fecha.getFullYear() === anioAnt && fecha.getMonth() === mesAnt;
        } else if (periodo === 'anio') {
          matchPeriodo = fecha.getFullYear() === hoy.getFullYear();
        } else if (periodo === 'especifico') {
          const matchMes = mesSel === 'todos' || fecha.getMonth() === Number(mesSel);
          const matchAnio = anioSel === 'todos' || fecha.getFullYear() === Number(anioSel);
          matchPeriodo = matchMes && matchAnio;
        }
      }

      let matchCat = true;
      let matchMrc = true;
      if (catSel !== 'todas' || mrcSel !== 'todas') {
        const prod = prods.find(p => p.idProducto === dv.producto?.idProducto);
        if (catSel !== 'todas') {
          matchCat = prod?.categoria?.idCategoria === Number(catSel);
        }
        if (mrcSel !== 'todas') {
          matchMrc = prod?.marca?.idMarca === Number(mrcSel);
        }
      }

      return matchQ && matchEst && matchPeriodo && matchCat && matchMrc;
    });
  });

  detallesVentaPaginados = computed(() => {
    const list = this.detallesVentaFiltrados();
    const page = this.paginaDetallesVenta();
    const size = this.tamanoDetallesVenta();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasDetallesVenta = computed(() => {
    return Math.max(1, Math.ceil(this.detallesVentaFiltrados().length / this.tamanoDetallesVenta()));
  });

  totalUnidadesDetalles = computed(() => {
    return this.detallesVentaFiltrados().reduce((acc, dv) => acc + (dv.cantidad || 0), 0);
  });

  totalMontoDetalles = computed(() => {
    return this.detallesVentaFiltrados().reduce((acc, dv) => acc + (dv.subTotalVenta || ((dv.cantidad || 1) * (dv.precioUnitario || 0))), 0);
  });

  ventasPaginadas = computed(() => {
    const list = this.ventasFiltradas();
    const page = this.paginaVentas();
    const size = this.tamanoVentas();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasVentas = computed(() => {
    return Math.max(1, Math.ceil(this.ventasFiltradas().length / this.tamanoVentas()));
  });

  // 4. Personas
  personasFiltradas = computed(() => {
    const q = this.filtroPersonas().toLowerCase().trim();
    if (!q) return this.personas();
    return this.personas().filter(p =>
      (p.nombres && p.nombres.toLowerCase().includes(q)) ||
      (p.apellidoPaterno && p.apellidoPaterno.toLowerCase().includes(q)) ||
      (p.numeroDocumento && p.numeroDocumento.includes(q)) ||
      (p.telefono && p.telefono.includes(q))
    );
  });

  personasPaginadas = computed(() => {
    const list = this.personasFiltradas();
    const page = this.paginaPersonas();
    const size = this.tamanoPersonas();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasPersonas = computed(() => {
    return Math.max(1, Math.ceil(this.personasFiltradas().length / this.tamanoPersonas()));
  });

  // 4.1 Productos - Proveedor
  productosProveedorFiltrados = computed(() => {
    const q = this.filtroProductosProveedor().toLowerCase().trim();
    if (!q) return this.productosProveedor();
    return this.productosProveedor().filter(pp =>
      (pp.producto?.nombre && pp.producto.nombre.toLowerCase().includes(q)) ||
      (pp.producto?.codigoSku && pp.producto.codigoSku.toLowerCase().includes(q)) ||
      (pp.proveedor?.razonSocial && pp.proveedor.razonSocial.toLowerCase().includes(q))
    );
  });

  productosProveedorPaginados = computed(() => {
    const list = this.productosProveedorFiltrados();
    const page = this.paginaProductosProveedor();
    const size = this.tamanoProductosProveedor();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasProductosProveedor = computed(() => {
    return Math.max(1, Math.ceil(this.productosProveedorFiltrados().length / this.tamanoProductosProveedor()));
  });

  // 4.2 Clientes
  clientesFiltrados = computed(() => {
    const q = this.filtroClientes().toLowerCase().trim();
    if (!q) return this.clientes();
    return this.clientes().filter(c =>
      (c.nombreCompleto && c.nombreCompleto.toLowerCase().includes(q)) ||
      (c.codigoCliente && c.codigoCliente.toLowerCase().includes(q)) ||
      (c.persona?.numeroDocumento && c.persona.numeroDocumento.includes(q)) ||
      (c.persona?.nombres && c.persona.nombres.toLowerCase().includes(q)) ||
      (c.persona?.apellidoPaterno && c.persona.apellidoPaterno.toLowerCase().includes(q))
    );
  });

  clientesPaginados = computed(() => {
    const list = this.clientesFiltrados();
    const page = this.paginaClientes();
    const size = this.tamanoClientes();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasClientes = computed(() => {
    return Math.max(1, Math.ceil(this.clientesFiltrados().length / this.tamanoClientes()));
  });

  // 4.3 Empleados
  empleadosFiltrados = computed(() => {
    const q = this.filtroEmpleados().toLowerCase().trim();
    if (!q) return this.empleados();
    return this.empleados().filter(e =>
      (e.nombreCompleto && e.nombreCompleto.toLowerCase().includes(q)) ||
      (e.cargo && e.cargo.toLowerCase().includes(q)) ||
      (e.persona?.numeroDocumento && e.persona.numeroDocumento.includes(q)) ||
      (e.persona?.nombres && e.persona.nombres.toLowerCase().includes(q))
    );
  });

  empleadosPaginados = computed(() => {
    const list = this.empleadosFiltrados();
    const page = this.paginaEmpleados();
    const size = this.tamanoEmpleados();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasEmpleados = computed(() => {
    return Math.max(1, Math.ceil(this.empleadosFiltrados().length / this.tamanoEmpleados()));
  });

  // 4.4 Usuarios
  usuariosFiltrados = computed(() => {
    const q = this.filtroUsuarios().toLowerCase().trim();
    if (!q) return this.usuarios();
    return this.usuarios().filter(u =>
      (u.correo && u.correo.toLowerCase().includes(q)) ||
      (u.rol?.nombre && u.rol.nombre.toLowerCase().includes(q)) ||
      (u.persona?.nombres && u.persona.nombres.toLowerCase().includes(q)) ||
      (u.persona?.apellidoPaterno && u.persona.apellidoPaterno.toLowerCase().includes(q)) ||
      (u.persona?.numeroDocumento && u.persona.numeroDocumento.includes(q))
    );
  });

  usuariosPaginados = computed(() => {
    const list = this.usuariosFiltrados();
    const page = this.paginaUsuarios();
    const size = this.tamanoUsuarios();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasUsuarios = computed(() => {
    return Math.max(1, Math.ceil(this.usuariosFiltrados().length / this.tamanoUsuarios()));
  });

  // 5. Auditoría
  auditoriaFiltrada = computed(() => {
    const mod = this.filtroAuditoriaModulo();
    if (mod === 'TODOS') return this.acciones();
    return this.acciones().filter(a => a.modulo === mod);
  });

  auditoriaPaginada = computed(() => {
    const list = this.auditoriaFiltrada();
    const page = this.paginaAuditoria();
    const size = this.tamanoAuditoria();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasAuditoria = computed(() => {
    return Math.max(1, Math.ceil(this.auditoriaFiltrada().length / this.tamanoAuditoria()));
  });

  accesosPaginados = computed(() => {
    const list = this.accesos();
    const page = this.paginaAccesos();
    const size = this.tamanoAccesos();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  totalPaginasAccesos = computed(() => {
    return Math.max(1, Math.ceil(this.accesos().length / this.tamanoAccesos()));
  });

  // ==========================================
  // OPERACIONES: UBIGEOS (Validado + Feedback)
  // ==========================================
  abrirModalUbigeo(u?: UbigeoAdmin): void {
    if (u) {
      this.esEdicionUbigeo.set(true);
      this.ubiId.set(u.idUbigeo);
      this.ubiDepartamento.set(u.departamento || '');
      this.ubiProvincia.set(u.provincia || '');
      this.ubiDistrito.set(u.distrito || '');
      this.ubiVigente.set(u.vigente !== false);
    } else {
      this.esEdicionUbigeo.set(false);
      this.ubiId.set(null);
      this.ubiDepartamento.set('');
      this.ubiProvincia.set('');
      this.ubiDistrito.set('');
      this.ubiVigente.set(true);
    }
    this.mostrarModalUbigeo.set(true);
  }

  cerrarModalUbigeo(): void {
    this.mostrarModalUbigeo.set(false);
  }

  guardarUbigeo(): void {
    const dep = this.ubiDepartamento().trim();
    const prov = this.ubiProvincia().trim();
    const dist = this.ubiDistrito().trim();

    if (!dep || !prov || !dist) {
      this.toastService.advertencia('Departamento, Provincia y Distrito son obligatorios');
      return;
    }

    if (!this.esEdicionUbigeo()) {
      const id = this.ubiId();
      if (!id || id <= 0) {
        this.toastService.advertencia('Ingresa un código de Ubigeo válido (numérico de 6 dígitos)');
        return;
      }
      this.adminService.crearUbigeo({
        idUbigeo: id,
        departamento: dep,
        provincia: prov,
        distrito: dist,
        vigente: this.ubiVigente()
      }).subscribe({
        next: () => {
          this.toastService.exito(`Ubigeo ${id} registrado correctamente`);
          this.cerrarModalUbigeo();
          this.recargarUbigeos();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al crear Ubigeo')
      });
    } else {
      const id = this.ubiId()!;
      this.adminService.actualizarUbigeo(id, {
        idUbigeo: id,
        departamento: dep,
        provincia: prov,
        distrito: dist,
        vigente: this.ubiVigente()
      }).subscribe({
        next: () => {
          this.toastService.exito(`Ubigeo ${id} actualizado correctamente`);
          this.cerrarModalUbigeo();
          this.recargarUbigeos();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al actualizar Ubigeo')
      });
    }
  }

  toggleVigenciaUbigeo(u: UbigeoAdmin): void {
    const nuevoEstado = !u.vigente;
    const accionTexto = nuevoEstado ? 'Activar' : 'Desactivar';
    this.solicitarConfirmacion(
      `¿${accionTexto} Ubigeo?`,
      `¿Deseas ${accionTexto.toLowerCase()} el ubigeo ${u.idUbigeo} (${u.distrito}, ${u.provincia})?`,
      () => {
        this.adminService.actualizarUbigeo(u.idUbigeo, {
          idUbigeo: u.idUbigeo,
          departamento: u.departamento,
          provincia: u.provincia,
          distrito: u.distrito,
          vigente: nuevoEstado
        }).subscribe({
          next: () => {
            this.toastService.exito(`Ubigeo ${nuevoEstado ? 'activado' : 'desactivado'} con éxito`);
            this.recargarUbigeos();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al cambiar estado')
        });
      },
      accionTexto,
      !nuevoEstado
    );
  }

  eliminarUbigeo(u: UbigeoAdmin): void {
    this.solicitarConfirmacion(
      '¿Desactivar Ubigeo?',
      `¿Estás seguro de que deseas desactivar el ubigeo ${u.idUbigeo} (${u.distrito}, ${u.provincia})?`,
      () => {
        this.adminService.eliminarUbigeo(u.idUbigeo).subscribe({
          next: () => {
            this.toastService.exito(`Ubigeo ${u.idUbigeo} desactivado`);
            this.recargarUbigeos();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al desactivar ubigeo')
        });
      }
    );
  }

  // ==========================================
  // OPERACIONES: PRODUCTOS (Crear / Editar)
  // ==========================================
  abrirModalProducto(p?: Producto): void {
    if (p) {
      this.esEdicionProducto.set(true);
      this.idProductoEditando.set(p.idProducto);
      this.nuevoNombre.set(p.nombre || '');
      this.nuevoPrecio.set(p.precioVenta || 0);
      this.nuevoStock.set(p.stock || 0);
      this.nuevaDesc.set(p.descripcion || '');
      this.idCatSel.set(p.categoria?.idCategoria || null);
      this.idMarcaSel.set(p.marca?.idMarca || null);
      this.nuevoDestacado.set(!!p.destacado);
      this.nuevoNovedad.set(!!p.novedad);
    } else {
      this.esEdicionProducto.set(false);
      this.idProductoEditando.set(null);
      this.nuevoNombre.set('');
      this.nuevoPrecio.set(0);
      this.nuevoStock.set(1);
      this.nuevaDesc.set('');
      this.idCatSel.set(this.categorias().length > 0 ? this.categorias()[0].idCategoria : null);
      this.idMarcaSel.set(this.marcas().length > 0 ? this.marcas()[0].idMarca : null);
      this.nuevoDestacado.set(false);
      this.nuevoNovedad.set(false);
    }
    this.archivoImagen = null;
    this.archivoImagenProducto = null;
    this.urlImagenProducto.set('');
    if (p && p.imagenes && p.imagenes.length > 0) {
      this.previewImagenProducto.set(p.imagenes[0].urlImagen);
    } else {
      this.previewImagenProducto.set(null);
    }
    this.mostrarModalProducto.set(true);
  }

  cerrarModalProducto(): void {
    this.mostrarModalProducto.set(false);
    this.archivoImagen = null;
    this.archivoImagenProducto = null;
    this.urlImagenProducto.set('');
    this.previewImagenProducto.set(null);
  }

  onUrlImagenProductoChange(url: string): void {
    this.urlImagenProducto.set(url);
    if (!this.archivoImagenProducto) {
      this.previewImagenProducto.set(url.trim() ? url.trim() : null);
    }
  }

  onSeleccionarImagenProducto(event: any): void {
    const files = event.target?.files;
    if (files && files.length > 0) {
      this.archivoImagenProducto = files[0];
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewImagenProducto.set(e.target.result);
      };
      reader.readAsDataURL(files[0]);
    }
  }

  removerImagenProducto(): void {
    this.archivoImagenProducto = null;
    this.urlImagenProducto.set('');
    this.previewImagenProducto.set(null);
  }

  onFileSelected(event: any): void {
    if (event.target.files && event.target.files.length > 0) {
      this.archivoImagen = event.target.files[0];
    }
  }

  guardarProducto(): void {
    const nombre = this.nuevoNombre().trim();
    const precio = this.nuevoPrecio();
    const stock = this.nuevoStock();
    const catId = this.idCatSel();
    const marcaId = this.idMarcaSel();

    if (!nombre) {
      this.toastService.advertencia('Ingresa el nombre del producto');
      return;
    }
    if (precio <= 0) {
      this.toastService.advertencia('El precio de venta debe ser mayor a S/ 0');
      return;
    }
    if (stock < 0) {
      this.toastService.advertencia('El stock no puede ser negativo');
      return;
    }
    if (!catId || !marcaId) {
      this.toastService.advertencia('Selecciona la categoría y marca oficial');
      return;
    }

    const payload: any = {
      nombre,
      precioVenta: precio,
      stock,
      descripcion: this.nuevaDesc().trim(),
      categoria: { idCategoria: catId },
      marca: { idMarca: marcaId },
      destacado: this.nuevoDestacado(),
      novedad: this.nuevoNovedad()
    };

    if (this.esEdicionProducto()) {
      const id = this.idProductoEditando()!;
      this.productoService.adminActualizarProducto(id, payload).subscribe({
        next: (prodActualizado) => {
          if (this.archivoImagenProducto) {
            this.productoService.adminSubirImagen(id, this.archivoImagenProducto, true).subscribe({
              next: () => {
                this.toastService.exito(`Producto #${id} e imagen en Supabase actualizados`);
                this.cerrarModalProducto();
          this.recargarProductos();
              },
              error: (e) => {
                this.toastService.advertencia('Producto actualizado, pero falló la subida de imagen: ' + (e.error?.mensaje || e.message));
                this.cerrarModalProducto();
          this.recargarProductos();
              }
            });
          } else if (this.urlImagenProducto().trim()) {
            this.productoService.adminAgregarImagenPorUrl(id, this.urlImagenProducto().trim(), true).subscribe({
              next: () => {
                this.toastService.exito(`Producto #${id} e imagen importada a Supabase con éxito`);
                this.cerrarModalProducto();
          this.recargarProductos();
              },
              error: () => {
                this.toastService.exito(`Producto #${id} actualizado`);
                this.cerrarModalProducto();
          this.recargarProductos();
              }
            });
          } else {
            this.toastService.exito(`Producto #${id} actualizado exitosamente`);
            this.cerrarModalProducto();
          this.recargarProductos();
          }
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al actualizar producto')
      });
    } else {
      this.productoService.adminCrearProducto(payload).subscribe({
        next: (prodCreado) => {
          const id = prodCreado.idProducto;
          if (this.archivoImagenProducto && id) {
            this.productoService.adminSubirImagen(id, this.archivoImagenProducto, true).subscribe({
              next: () => {
                this.toastService.exito(`Producto #${id} creado y subido a Supabase con éxito`);
                this.cerrarModalProducto();
          this.recargarProductos();
              },
              error: (e) => {
                this.toastService.advertencia('Producto creado, pero falló la imagen: ' + (e.error?.mensaje || e.message));
                this.cerrarModalProducto();
          this.recargarProductos();
              }
            });
          } else if (this.urlImagenProducto().trim() && id) {
            this.productoService.adminAgregarImagenPorUrl(id, this.urlImagenProducto().trim(), true).subscribe({
              next: () => {
                this.toastService.exito(`Producto #${id} creado y alojado en Supabase con éxito`);
                this.cerrarModalProducto();
          this.recargarProductos();
              },
              error: () => {
                this.toastService.exito(`Producto #${id} creado exitosamente`);
                this.cerrarModalProducto();
          this.recargarProductos();
              }
            });
          } else {
            this.toastService.exito(`Producto #${id} creado exitosamente`);
            this.cerrarModalProducto();
          this.recargarProductos();
          }
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al crear producto')
      });
    }
  }

  guardarEnCache(): void {
    try {
      const snap = {
        productos: this.productos(),
        categorias: this.categorias(),
        marcas: this.marcas(),
        productosProveedor: this.productosProveedor(),
        timestamp: Date.now()
      };
      sessionStorage.setItem('tz_admin_cache', JSON.stringify(snap));
    } catch (_) {}
  }

  cargarDesdeCache(): void {
    try {
      const str = sessionStorage.getItem('tz_admin_cache');
      if (str) {
        const snap = JSON.parse(str);
        if (snap && Array.isArray(snap.productosProveedor) && snap.productosProveedor.length > 0 && this.productosProveedor().length === 0) {
          this.productosProveedor.set(snap.productosProveedor);
        }
      }
    } catch (_) {}
  }

  validarPrecioSubida(valor: any): void {
    if (valor === null || valor === undefined || valor === '') {
      this.precioNuevoProductoSubida.set(0);
      return;
    }
    const num = Number(valor);
    if (isNaN(num) || num <= 0) {
      this.precioNuevoProductoSubida.set(0);
    } else {
      this.precioNuevoProductoSubida.set(Number(Math.abs(num).toFixed(2)));
    }
  }

  validarStockSubida(valor: any): void {
    if (valor === null || valor === undefined || valor === '') {
      this.stockNuevoProductoSubida.set(0);
      return;
    }
    const num = Number(valor);
    if (isNaN(num) || num < 0) {
      this.stockNuevoProductoSubida.set(0);
    } else {
      this.stockNuevoProductoSubida.set(Math.floor(Math.abs(num)));
    }
  }

  onNuevoPrecioChange(valor: any): void {
    if (valor === null || valor === undefined || valor === '') {
      this.nuevoPrecio.set(0);
      return;
    }
    const num = Number(valor);
    this.nuevoPrecio.set(isNaN(num) || num <= 0 ? 0 : Number(Math.abs(num).toFixed(2)));
  }

  onNuevoStockChange(valor: any): void {
    if (valor === null || valor === undefined || valor === '') {
      this.nuevoStock.set(0);
      return;
    }
    const num = Number(valor);
    this.nuevoStock.set(isNaN(num) || num < 0 ? 0 : Math.floor(Math.abs(num)));
  }

  bloquearTeclasNegativas(event: KeyboardEvent): void {
    if (event.key === '-' || event.key === 'e' || event.key === '+' || event.key === 'E') {
      event.preventDefault();
    }
  }

  bloquearTeclasStock(event: KeyboardEvent): void {
    if (event.key === '-' || event.key === 'e' || event.key === '+' || event.key === 'E' || event.key === '.' || event.key === ',') {
      event.preventDefault();
    }
  }

  abrirModalSustitucionParaEntrante(entrante: Producto): void {
    this.productoSustitutoEntrante.set(entrante);
    this.productoSustitutoSaliente.set(null);
    this.busquedaSustituto.set('');
    this.modalSustitucionDestacadoAbierto.set(true);
  }

  abrirModalSustitucionParaSaliente(saliente: Producto): void {
    this.productoSustitutoSaliente.set(saliente);
    this.productoSustitutoEntrante.set(null);
    this.busquedaSustituto.set('');
    this.modalSustitucionDestacadoAbierto.set(true);
  }

  cerrarModalSustitucion(): void {
    this.modalSustitucionDestacadoAbierto.set(false);
    this.productoSustitutoEntrante.set(null);
    this.productoSustitutoSaliente.set(null);
    this.busquedaSustituto.set('');
  }

  ejecutarSustitucion(saliente: Producto, entrante: Producto): void {
    if (!saliente || !entrante || saliente.idProducto === entrante.idProducto) return;

    // 1. Optimistic update en memoria (0ms)
    this.productos.update(list => list.map(item => {
      if (item.idProducto === saliente.idProducto) return { ...item, destacado: false };
      if (item.idProducto === entrante.idProducto) return { ...item, destacado: true };
      return item;
    }));
    this.guardarEnCache();
    this.cerrarModalSustitucion();

    // 2. Persistir en backend y Supabase
    this.productoService.adminSustituirDestacado(saliente.idProducto, entrante.idProducto).subscribe({
      next: () => {
        this.toastService.exito(`"${entrante.nombre}" ahora es destacado en reemplazo de "${saliente.nombre}"`);
      },
      error: (err) => {
        this.productos.update(list => list.map(item => {
          if (item.idProducto === saliente.idProducto) return { ...item, destacado: true };
          if (item.idProducto === entrante.idProducto) return { ...item, destacado: false };
          return item;
        }));
        this.guardarEnCache();
        this.toastService.error(err.error?.mensaje || 'Error al sustituir producto destacado');
      }
    });
  }

  toggleDestacadoProducto(prod: Producto): void {
    const nuevoEstado = !prod.destacado;

    // Validación estricta de límite de 9 productos destacados
    if (nuevoEstado && this.totalDestacados() >= 9) {
      this.abrirModalSustitucionParaEntrante(prod);
      return;
    }

    // 1. Actualización optimista inmediata en memoria (0ms)
    this.productos.update(list => list.map(item =>
      item.idProducto === prod.idProducto ? { ...item, destacado: nuevoEstado } : item
    ));
    this.guardarEnCache();

    // 2. Persistir en backend y Supabase
    this.productoService.adminToggleDestacado(prod.idProducto, nuevoEstado).subscribe({
      next: () => {
        this.toastService.exito(
          nuevoEstado
            ? `"${prod.nombre}" ahora se mostrará como destacado en el Inicio (${this.totalDestacados()}/9)`
            : `"${prod.nombre}" fue removido de los destacados (${this.totalDestacados()}/9)`
        );
      },
      error: (err) => {
        this.productos.update(list => list.map(item =>
          item.idProducto === prod.idProducto ? { ...item, destacado: !nuevoEstado } : item
        ));
        this.guardarEnCache();
        this.toastService.error(err.error?.mensaje || 'Error al alternar estado destacado');
      }
    });
  }

  toggleNovedadProducto(prod: Producto): void {
    const nuevoEstado = !prod.novedad;
    // 1. Actualización optimista inmediata en memoria (0ms)
    this.productos.update(list => list.map(item =>
      item.idProducto === prod.idProducto ? { ...item, novedad: nuevoEstado } : item
    ));
    this.guardarEnCache();

    // 2. Persistir en backend y Supabase
    this.productoService.adminToggleNovedad(prod.idProducto).subscribe({
      next: (actualizado) => {
        this.productos.update(list => list.map(item =>
          item.idProducto === prod.idProducto ? { ...item, novedad: actualizado.novedad } : item
        ));
        this.guardarEnCache();
        this.toastService.exito(
          nuevoEstado
            ? `"${prod.nombre}" fijado como Novedad / Lanzamiento Oficial`
            : `"${prod.nombre}" desmarcado de Novedades (vuelve al orden automático)`
        );
      },
      error: (err) => {
        this.productos.update(list => list.map(item =>
          item.idProducto === prod.idProducto ? { ...item, novedad: !nuevoEstado } : item
        ));
        this.guardarEnCache();
        this.toastService.error(err.error?.mensaje || 'Error al actualizar estado de novedad');
      }
    });
  }

  eliminarProducto(p: Producto): void {
    if (!this.authService.esAdmin()) {
      this.toastService.advertencia('Permiso denegado: Solo el Administrador puede eliminar productos del catálogo.');
      return;
    }
    this.solicitarConfirmacion(
      '¿Eliminar Producto?',
      `¿Estás seguro de que deseas eliminar permanentemente el producto "${p.nombre}"?`,
      () => {
        // Optimista: retirar inmediatamente de la vista para respuesta instantánea sin parpadeo
        this.productos.update(list => list.filter(item => item.idProducto !== p.idProducto));
        this.productoService.adminEliminarProducto(p.idProducto).subscribe({
          next: () => {
            this.toastService.exito(`Producto "${p.nombre}" eliminado con éxito`);
            this.recargarProductos();
          },
          error: (err) => {
            this.toastService.error(err.error?.mensaje || 'Error al eliminar producto');
            this.recargarProductos();
          }
        });
      }
    );
  }

  // ==========================================
  // OPERACIONES: CATEGORÍAS (Crear / Editar)
  // ==========================================
  abrirModalCategoria(c?: Categoria): void {
    if (c) {
      this.esEdicionCategoria.set(true);
      this.idCatEditando.set(c.idCategoria);
      this.nuevoNombreCat.set(c.nombre);
    } else {
      this.esEdicionCategoria.set(false);
      this.idCatEditando.set(null);
      this.nuevoNombreCat.set('');
    }
    this.mostrarModalCategoria.set(true);
  }

  cerrarModalCategoria(): void {
    this.mostrarModalCategoria.set(false);
  }

  guardarCategoria(): void {
    const nombre = this.nuevoNombreCat().trim();
    if (!nombre) {
      this.toastService.advertencia('Ingresa el nombre de la categoría');
      return;
    }
    if (this.esEdicionCategoria()) {
      const id = this.idCatEditando()!;
      this.productoService.adminActualizarCategoria(id, { nombre }).subscribe({
        next: () => {
          this.toastService.exito('Categoría actualizada con éxito');
          this.cerrarModalCategoria();
          this.recargarCategorias();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al actualizar categoría')
      });
    } else {
      this.productoService.adminCrearCategoria({ nombre }).subscribe({
        next: () => {
          this.toastService.exito('Categoría registrada con éxito');
          this.cerrarModalCategoria();
          this.recargarCategorias();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al crear categoría')
      });
    }
  }

  eliminarCategoria(c: Categoria): void {
    this.solicitarConfirmacion(
      '¿Eliminar Categoría?',
      `¿Deseas eliminar la categoría "${c.nombre}"?`,
      () => {
        this.productoService.adminEliminarCategoria(c.idCategoria).subscribe({
          next: () => {
            this.toastService.exito('Categoría eliminada');
            this.recargarCategorias();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al eliminar categoría')
        });
      }
    );
  }

  // ==========================================
  // OPERACIONES: MARCAS (Crear / Editar)
  // ==========================================
  abrirModalMarca(m?: Marca): void {
    if (m) {
      this.esEdicionMarca.set(true);
      this.idMarcaEditando.set(m.idMarca);
      this.nuevoNombreMarca.set(m.nombre);
      this.nuevoLogoMarca.set(m.logoUrl || '');
      this.previewLogoMarca.set(m.logoUrl || null);
    } else {
      this.esEdicionMarca.set(false);
      this.idMarcaEditando.set(null);
      this.nuevoNombreMarca.set('');
      this.nuevoLogoMarca.set('');
      this.previewLogoMarca.set(null);
    }
    this.archivoLogoMarca = null;
    this.guardandoMarca.set(false);
    this.mostrarModalMarca.set(true);
  }

  cerrarModalMarca(): void {
    this.mostrarModalMarca.set(false);
    this.archivoLogoMarca = null;
    this.previewLogoMarca.set(null);
  }

  onSeleccionarLogoMarca(event: any): void {
    const file = event.target?.files?.[0];
    if (file) {
      this.archivoLogoMarca = file;
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewLogoMarca.set(e.target.result);
      };
      reader.readAsDataURL(file);
    }
  }

  onLogoUrlChange(url: string): void {
    this.nuevoLogoMarca.set(url);
    if (!this.archivoLogoMarca) {
      this.previewLogoMarca.set(url.trim() ? url.trim() : null);
    }
  }

  removerLogoMarca(): void {
    this.archivoLogoMarca = null;
    this.nuevoLogoMarca.set('');
    this.previewLogoMarca.set(null);
  }

  guardarMarca(): void {
    const nombre = this.nuevoNombreMarca().trim();
    const logoUrl = this.nuevoLogoMarca().trim() || undefined;
    if (!nombre) {
      this.toastService.advertencia('Ingresa el nombre de la marca');
      return;
    }

    this.guardandoMarca.set(true);

    if (this.esEdicionMarca()) {
      const id = this.idMarcaEditando()!;
      this.productoService.adminActualizarMarca(id, { nombre, logoUrl }).subscribe({
        next: (marcaActualizada) => {
          if (this.archivoLogoMarca) {
            this.productoService.adminSubirLogoMarca(id, this.archivoLogoMarca).subscribe({
              next: () => {
                this.guardandoMarca.set(false);
                this.toastService.exito('Marca y logo actualizados con éxito');
                this.cerrarModalMarca();
          this.recargarMarcas();
              },
              error: (err) => {
                this.guardandoMarca.set(false);
                this.toastService.advertencia('Marca guardada, pero falló la subida de imagen: ' + (err.error?.mensaje || err.message));
                this.cerrarModalMarca();
          this.recargarMarcas();
              }
            });
          } else {
            this.guardandoMarca.set(false);
            this.toastService.exito('Marca actualizada con éxito');
            this.cerrarModalMarca();
          this.recargarMarcas();
          }
        },
        error: (err) => {
          this.guardandoMarca.set(false);
          this.toastService.error(err.error?.mensaje || 'Error al actualizar marca');
        }
      });
    } else {
      this.productoService.adminCrearMarca({ nombre, logoUrl }).subscribe({
        next: (nuevaMarca) => {
          if (this.archivoLogoMarca && nuevaMarca.idMarca) {
            this.productoService.adminSubirLogoMarca(nuevaMarca.idMarca, this.archivoLogoMarca).subscribe({
              next: () => {
                this.guardandoMarca.set(false);
                this.toastService.exito('Marca y logo creados exitosamente');
                this.cerrarModalMarca();
          this.recargarMarcas();
              },
              error: (err) => {
                this.guardandoMarca.set(false);
                this.toastService.advertencia('Marca creada, pero falló la subida del logo: ' + (err.error?.mensaje || err.message));
                this.cerrarModalMarca();
          this.recargarMarcas();
              }
            });
          } else {
            this.guardandoMarca.set(false);
            this.toastService.exito('Marca creada exitosamente');
            this.cerrarModalMarca();
          this.recargarMarcas();
          }
        },
        error: (err) => {
          this.guardandoMarca.set(false);
          this.toastService.error(err.error?.mensaje || 'Error al registrar marca');
        }
      });
    }
  }

  eliminarMarca(m: Marca): void {
    this.solicitarConfirmacion(
      '¿Eliminar Marca?',
      `¿Deseas eliminar la marca oficial "${m.nombre}"?`,
      () => {
        this.productoService.adminEliminarMarca(m.idMarca).subscribe({
          next: () => {
            this.toastService.exito('Marca eliminada con éxito');
            this.cargarDatos();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al eliminar marca')
        });
      }
    );
  }

  // ==========================================
  // OPERACIONES: PROVEEDORES (Crear / Editar)
  // ==========================================
  abrirModalProveedor(p?: ProveedorAdmin): void {
    if (p) {
      this.esEdicionProveedor.set(true);
      this.idProvEditando.set(p.idProveedor);
      this.nuevoProvRazon.set(p.razonSocial || '');
      this.nuevoProvRuc.set(p.numeroDocumento || '');
      this.nuevoProvContacto.set(p.nombreEncargado || '');
      this.nuevoProvTelefono.set(p.telefonoContacto || '');
      this.nuevoProvCorreo.set(p.correo || '');
    } else {
      this.esEdicionProveedor.set(false);
      this.idProvEditando.set(null);
      this.nuevoProvRazon.set('');
      this.nuevoProvRuc.set('');
      this.nuevoProvContacto.set('');
      this.nuevoProvTelefono.set('');
      this.nuevoProvCorreo.set('');
    }
    this.mostrarModalProveedor.set(true);
  }

  cerrarModalProveedor(): void {
    this.mostrarModalProveedor.set(false);
  }

  guardarProveedor(): void {
    const razon = this.nuevoProvRazon().trim();
    if (!razon) {
      this.toastService.advertencia('La Razón Social del proveedor es obligatoria');
      return;
    }
    const payload = {
      razonSocial: razon.toUpperCase(),
      numeroDocumento: this.nuevoProvRuc().trim(),
      nombreEncargado: this.nuevoProvContacto().trim().toUpperCase(),
      telefonoContacto: this.nuevoProvTelefono().trim(),
      correo: this.nuevoProvCorreo().trim().toLowerCase()
    };

    if (this.esEdicionProveedor()) {
      const id = this.idProvEditando()!;
      this.adminService.actualizarProveedor(id, payload).subscribe({
        next: () => {
          this.toastService.exito('Proveedor actualizado con éxito');
          this.cerrarModalProveedor();
          this.recargarProveedores();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al actualizar proveedor')
      });
    } else {
      this.adminService.crearProveedor(payload).subscribe({
        next: () => {
          this.toastService.exito('Proveedor registrado con éxito');
          this.cerrarModalProveedor();
          this.recargarProveedores();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al crear proveedor')
      });
    }
  }

  eliminarProveedor(p: ProveedorAdmin): void {
    this.solicitarConfirmacion(
      '¿Eliminar Proveedor?',
      `¿Deseas dar de baja al proveedor "${p.razonSocial}"?`,
      () => {
        this.adminService.eliminarProveedor(p.idProveedor).subscribe({
          next: () => {
            this.toastService.exito('Proveedor dado de baja');
            this.cargarDatos();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al eliminar proveedor')
        });
      }
    );
  }

  // ==========================================
  // OPERACIONES: MEDIOS DE PAGO (Crear / Editar)
  // ==========================================
  getInstruccionesMedioPago(mp: any): string {
    if (mp?.instrucciones && mp.instrucciones.trim()) {
      return mp.instrucciones;
    }
    const n = (mp?.nombre || '').toLowerCase();
    if (n.includes('yape') || n.includes('plin')) {
      return 'Transferencias al 910 076 021 (TechZone Gaming). Adjuntar captura o código de operación tras realizar el abono.';
    }
    if (n.includes('credito') || n.includes('crédito')) {
      return 'Tarjetas Visa, MasterCard, Amex con autenticación 3D-Secure y cifrado TLS 1.3.';
    }
    if (n.includes('debito') || n.includes('débito')) {
      return 'Débito automático en línea para clientes BCP, BBVA, Interbank y Scotiabank.';
    }
    if (n.includes('transferencia')) {
      return 'BCP Cta Cte: 191-98765432-0-99 / CCI: 002-191009876543209954 a nombre de TechZone S.A.C.';
    }
    if (n.includes('pagoefectivo') || n.includes('efectivo')) {
      return 'Genera tu código CIP único para pagar en bodegas, agentes o banca móvil antes de 24 horas.';
    }
    return 'Medio de pago habilitado para compras en línea';
  }

  abrirModalMedioPago(m?: MedioPagoAdmin): void {
    if (m) {
      this.esEdicionMedioPago.set(true);
      this.idMedioEditando.set(m.idMedioPago);
      this.nuevoMedioNombre.set(m.nombre || '');
      this.nuevoMedioInstrucciones.set(m.instrucciones || this.getInstruccionesMedioPago(m));
    } else {
      this.esEdicionMedioPago.set(false);
      this.idMedioEditando.set(null);
      this.nuevoMedioNombre.set('');
      this.nuevoMedioInstrucciones.set('');
    }
    this.mostrarModalMedioPago.set(true);
  }

  cerrarModalMedioPago(): void {
    this.mostrarModalMedioPago.set(false);
  }

  guardarMedioPago(): void {
    const nombre = this.nuevoMedioNombre().trim();
    if (!nombre) {
      this.toastService.advertencia('Ingresa el nombre del medio de pago');
      return;
    }
    const payload = {
      nombre,
      instrucciones: this.nuevoMedioInstrucciones().trim()
    };

    if (this.esEdicionMedioPago()) {
      const id = this.idMedioEditando()!;
      this.adminService.actualizarMedioPago(id, payload).subscribe({
        next: () => {
          this.toastService.exito('Medio de pago actualizado');
          this.cerrarModalMedioPago();
          this.recargarMediosPago();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al actualizar medio de pago')
      });
    } else {
      this.adminService.crearMedioPago(payload).subscribe({
        next: () => {
          this.toastService.exito('Medio de pago registrado');
          this.cerrarModalMedioPago();
          this.recargarMediosPago();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al crear medio de pago')
      });
    }
  }

  eliminarMedioPago(m: MedioPagoAdmin): void {
    this.solicitarConfirmacion(
      '¿Deshabilitar Medio de Pago?',
      `¿Deseas deshabilitar el método "${m.nombre}"?`,
      () => {
        this.adminService.actualizarMedioPago(m.idMedioPago, {
          nombre: m.nombre,
          instrucciones: m.instrucciones,
          vigente: false
        }).subscribe({
          next: () => {
            this.toastService.exito('Medio de pago deshabilitado');
            this.recargarMediosPago();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al deshabilitar')
        });
      },
      'Deshabilitar',
      true,
      'advertencia'
    );
  }

  // ==========================================
  // OPERACIONES: TIPOS DE DOCUMENTO
  // ==========================================
  obtenerNombreCompletoPersona(p?: { nombres?: string; apellidoPaterno?: string; apellidoMaterno?: string; nombreCompleto?: string } | null): string {
    if (!p) return 'Sin persona vinculada';
    const partes = [p.nombres, p.apellidoPaterno, p.apellidoMaterno].filter(Boolean);
    if (partes.length > 0) return partes.join(' ').trim().toUpperCase();
    if (p.nombreCompleto) return p.nombreCompleto.toUpperCase();
    return 'Sin nombre';
  }

  obtenerPersonaDeUsuario(u?: any): any {
    if (!u) return null;
    if (u.persona) return u.persona;
    const idPer = u.idPersona || u.id_persona;
    if (idPer) {
      const matchPer = this.personas().find(p => p.idPersona === idPer);
      if (matchPer) return matchPer;
    }
    if (u.correo) {
      const email = String(u.correo).trim().toLowerCase();
      const matchEmp = this.empleados().find(e => {
        if (!e.persona) return false;
        const nom = (e.persona.nombres || '').split(' ')[0].toLowerCase();
        const ape = (e.persona.apellidoPaterno || '').toLowerCase();
        return Boolean(nom && ape && email.includes(nom) && email.includes(ape));
      });
      if (matchEmp?.persona) return matchEmp.persona;

      const matchCli = this.clientes().find(c => {
        if (!c.persona) return false;
        const nom = (c.persona.nombres || '').split(' ')[0].toLowerCase();
        const ape = (c.persona.apellidoPaterno || '').toLowerCase();
        return Boolean(nom && ape && email.includes(nom) && email.includes(ape));
      });
      if (matchCli?.persona) return matchCli.persona;
    }
    return null;
  }

  obtenerClasesBadgeRol(nombreRol?: string): string {
    const r = (nombreRol || '').toUpperCase();
    if (r.includes('ADMIN')) return 'bg-purple-100 text-purple-800 dark:bg-purple-950/60 dark:text-purple-300 border border-purple-200 dark:border-purple-800';
    if (r.includes('SUPERVISOR')) return 'bg-indigo-100 text-indigo-800 dark:bg-indigo-950/60 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-800';
    if (r.includes('LOGISTICA') || r.includes('LOGÍSTICA') || r.includes('ALMACEN') || r.includes('ALMACÉN')) return 'bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300 border border-amber-200 dark:border-amber-800';
    if (r.includes('SOPORTE')) return 'bg-cyan-100 text-cyan-800 dark:bg-cyan-950/60 dark:text-cyan-300 border border-cyan-200 dark:border-cyan-800';
    if (r.includes('TRABAJADOR') || r.includes('EMPLEADO')) return 'bg-blue-100 text-blue-800 dark:bg-blue-950/60 dark:text-blue-300 border border-blue-200 dark:border-blue-800';
    if (r.includes('CLIENTE')) return 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800';
    return 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-300 border border-slate-200 dark:border-slate-700';
  }

  obtenerNombreCompletoCliente(c?: any): string {
    if (!c) return 'Sin cliente asignado';
    if (c.persona) return this.obtenerNombreCompletoPersona(c.persona);
    const idCli = c.idCliente || c.id_cliente;
    if (idCli) {
      const matchCli = this.clientes().find(cl => cl.idCliente === idCli);
      if (matchCli?.persona) return this.obtenerNombreCompletoPersona(matchCli.persona);
      if (matchCli?.nombreCompleto) return matchCli.nombreCompleto.toUpperCase();
    }
    const idPer = c.idPersona || c.id_persona;
    if (idPer) {
      const matchPer = this.personas().find(pe => pe.idPersona === idPer);
      if (matchPer) return this.obtenerNombreCompletoPersona(matchPer);
    }
    if (c.nombres) {
      const matchByName = this.personas().find(pe => pe.nombres?.toLowerCase() === c.nombres?.toLowerCase());
      if (matchByName) return this.obtenerNombreCompletoPersona(matchByName);
      const partes = [c.nombres, c.apellidos, c.apellidoPaterno, c.apellidoMaterno].filter(Boolean);
      return partes.join(' ').trim().toUpperCase();
    }
    if (c.nombreCompleto) return c.nombreCompleto.toUpperCase();
    return 'Cliente #' + (idCli || '-');
  }

  obtenerMetaDoc(nombre?: string): { sunat: string; longitud: string } {
    const n = (nombre || '').toLowerCase();
    if (n.includes('dni')) return { sunat: '01', longitud: '8 dígitos numéricos' };
    if (n.includes('ruc')) return { sunat: '06', longitud: '11 dígitos numéricos' };
    if (n.includes('pasaporte')) return { sunat: '07', longitud: 'Hasta 12 caracteres' };
    if (n.includes('carnet') || n.includes('extranjer')) return { sunat: '04', longitud: 'Hasta 12 caracteres' };
    return { sunat: '-', longitud: 'Variable' };
  }

  abrirModalTipoDoc(td?: TipoDocumentoAdmin): void {
    if (td) {
      this.esEdicionTipoDoc.set(true);
      this.idTipoDocEditando.set(td.idTipoDocumento);
      const meta = this.obtenerMetaDoc(td.nombre || td.descripcion);
      this.nuevoTipoDocDesc.set(td.nombre || td.descripcion || '');
      this.nuevoTipoDocSunat.set(td.codigoSunat || meta.sunat);
      this.nuevoTipoDocLongitud.set(td.longitudExacta || (meta.sunat === '06' ? 11 : 8));
      this.nuevoTipoDocVigente.set(td.vigente !== false);
    } else {
      this.esEdicionTipoDoc.set(false);
      this.idTipoDocEditando.set(null);
      this.nuevoTipoDocDesc.set('');
      this.nuevoTipoDocSunat.set('');
      this.nuevoTipoDocLongitud.set(8);
      this.nuevoTipoDocVigente.set(true);
    }
    this.mostrarModalTipoDoc.set(true);
  }

  cerrarModalTipoDoc(): void {
    this.mostrarModalTipoDoc.set(false);
  }

  guardarTipoDocumento(): void {
    const desc = this.nuevoTipoDocDesc().trim();
    if (!desc) {
      this.toastService.advertencia('Ingresa el nombre del tipo de documento');
      return;
    }
    const payload = {
      nombre: desc,
      descripcion: desc,
      codigoSunat: this.nuevoTipoDocSunat().trim(),
      longitudExacta: this.nuevoTipoDocLongitud(),
      vigente: this.nuevoTipoDocVigente()
    };

    if (this.esEdicionTipoDoc()) {
      const id = this.idTipoDocEditando()!;
      this.adminService.actualizarTipoDocumento(id, payload).subscribe({
        next: () => {
          this.toastService.exito('Tipo de documento actualizado');
          this.cerrarModalTipoDoc();
          this.cargarDatos();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al actualizar tipo doc')
      });
    } else {
      this.adminService.crearTipoDocumento(payload).subscribe({
        next: () => {
          this.toastService.exito('Tipo de documento registrado');
          this.cerrarModalTipoDoc();
          this.cargarDatos();
        },
        error: (err) => this.toastService.error(err.error?.mensaje || 'Error al crear tipo doc')
      });
    }
  }

  eliminarTipoDocumento(td: TipoDocumentoAdmin): void {
    const nombre = td.nombre || td.descripcion || 'este tipo';
    this.solicitarConfirmacion(
      '¿Eliminar Tipo de Documento?',
      `¿Deseas deshabilitar/eliminar el tipo de documento "${nombre}"?`,
      () => {
        this.adminService.eliminarTipoDocumento(td.idTipoDocumento).subscribe({
          next: () => {
            this.toastService.exito('Tipo de documento eliminado');
            this.cargarDatos();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al eliminar')
        });
      }
    );
  }

  // ==========================================
  // MÉTODOS MODAL NUEVO COLABORADOR (RENIEC)
  // ==========================================
  abrirModalNuevoEmpleado(): void {
    this.formColabDni.set('');
    this.formColabNombres.set('');
    this.formColabApPaterno.set('');
    this.formColabApMaterno.set('');
    this.formColabFechaNacimiento.set('');
    this.formColabTelefono.set('');
    this.formColabCorreo.set('');
    this.formColabPassword.set('');
    this.formColabDireccion.set('');
    this.formColabReferencia.set('');
    this.formColabCargo.set('SUPERVISOR_VENTAS');
    this.formColabSalario.set(1800);
    this.formColabFechaContrato.set(new Date().toISOString().substring(0, 10));

    const supRol = this.roles().find(r => r.nombre.toLowerCase().includes('supervisor'));
    if (supRol) {
      this.formColabRolId.set(supRol.idRol);
    } else {
      this.formColabRolId.set(2);
    }

    const dists = this.distritosColab();
    if (dists.length > 0) {
      this.formColabDistritoId.set(dists[0].idUbigeo);
    }

    this.mostrarModalNuevoEmpleado.set(true);
  }

  alCambiarCargoColab(cargo: string): void {
    this.formColabCargo.set(cargo);
    const rList = this.roles();
    let rolEncontrado: RolAdmin | undefined;

    if (cargo === 'SUPERVISOR_VENTAS') {
      rolEncontrado = rList.find(x => x.nombre.toLowerCase().includes('supervisor'));
    } else if (cargo === 'LOGISTICA') {
      rolEncontrado = rList.find(x => x.nombre.toLowerCase().includes('logística') || x.nombre.toLowerCase().includes('logistica') || x.nombre.toLowerCase().includes('almacén') || x.nombre.toLowerCase().includes('almacen'));
    } else if (cargo === 'SOPORTE') {
      rolEncontrado = rList.find(x => x.nombre.toLowerCase().includes('soporte'));
    } else if (cargo === 'ESPECIALISTA' || cargo === 'VENTAS') {
      rolEncontrado = rList.find(x => x.nombre.toLowerCase().includes('trabajador'));
    }

    if (rolEncontrado) {
      this.formColabRolId.set(rolEncontrado.idRol);
    }
  }

  cerrarModalNuevoEmpleado(): void {
    this.mostrarModalNuevoEmpleado.set(false);
  }

  consultarDniColaborador(): void {
    const num = this.formColabDni().trim();
    if (!num || !/^\d{8}$/.test(num)) {
      this.toastService.advertencia('Ingresa un número de DNI válido de 8 dígitos');
      return;
    }
    this.consultandoDniColab.set(true);
    this.configuracionService.consultarDocumento(1, num).subscribe({
      next: (resp) => {
        this.consultandoDniColab.set(false);
        if (resp.ok && resp.datos) {
          if (resp.datos.nombres) this.formColabNombres.set(resp.datos.nombres);
          if (resp.datos.apellidoPaterno) this.formColabApPaterno.set(resp.datos.apellidoPaterno);
          if (resp.datos.apellidoMaterno) this.formColabApMaterno.set(resp.datos.apellidoMaterno);
          if (resp.datos.fechaNacimiento) this.formColabFechaNacimiento.set(resp.datos.fechaNacimiento);
          if (resp.datos.telefono) this.formColabTelefono.set(resp.datos.telefono);
          this.toastService.exito('Datos y fecha de nacimiento obtenidos desde RENIEC');
        } else {
          this.toastService.advertencia(resp.mensaje || 'No se encontraron datos automáticos para este DNI');
        }
      },
      error: () => {
        this.consultandoDniColab.set(false);
        this.toastService.error('Error al consultar el servicio de DNI');
      }
    });
  }

  alCambiarDepartamentoColab(dep: string): void {
    this.formColabDepartamento.set(dep);
    const provs = this.provinciasColab();
    const primeraProv = provs.length > 0 ? provs[0] : '';
    this.formColabProvincia.set(primeraProv);
    const dists = this.distritosColab();
    this.formColabDistritoId.set(dists.length > 0 ? dists[0].idUbigeo : null);
  }

  alCambiarProvinciaColab(prov: string): void {
    this.formColabProvincia.set(prov);
    const dists = this.distritosColab();
    this.formColabDistritoId.set(dists.length > 0 ? dists[0].idUbigeo : null);
  }

  guardarNuevoColaborador(): void {
    const nombres = this.formColabNombres().trim();
    const apPat = this.formColabApPaterno().trim();
    const apMat = this.formColabApMaterno().trim();
    const dni = this.formColabDni().trim();
    const correo = this.formColabCorreo().trim().toLowerCase();
    const password = this.formColabPassword().trim();
    const telefono = this.formColabTelefono().trim();

    const valNombres = Validaciones.validarTextoNombre(nombres, 'Los nombres');
    if (!valNombres.valido) { this.toastService.advertencia(valNombres.mensaje!); return; }
    const valApPat = Validaciones.validarTextoNombre(apPat, 'El apellido paterno');
    if (!valApPat.valido) { this.toastService.advertencia(valApPat.mensaje!); return; }
    const valApMat = Validaciones.validarTextoNombre(apMat, 'El apellido materno');
    if (!valApMat.valido) { this.toastService.advertencia(valApMat.mensaje!); return; }
    const valDni = Validaciones.validarDni(dni);
    if (!valDni.valido) { this.toastService.advertencia(valDni.mensaje!); return; }
    const valTel = Validaciones.validarTelefono(telefono);
    if (!valTel.valido) { this.toastService.advertencia(valTel.mensaje!); return; }
    const valNac = Validaciones.validarMayorDeEdad(this.formColabFechaNacimiento());
    if (!valNac.valido) { this.toastService.advertencia(valNac.mensaje!); return; }
    const valUbigeo = Validaciones.validarUbigeo(this.formColabDistritoId());
    if (!valUbigeo.valido) { this.toastService.advertencia(valUbigeo.mensaje!); return; }
    const valDir = Validaciones.validarDireccion(this.formColabDireccion());
    if (!valDir.valido) { this.toastService.advertencia(valDir.mensaje!); return; }
    const valCorreo = Validaciones.validarCorreo(correo);
    if (!valCorreo.valido) { this.toastService.advertencia(valCorreo.mensaje!); return; }
    const valPass = Validaciones.validarContrasena(password);
    if (!valPass.valido) { this.toastService.advertencia(valPass.mensaje!); return; }
    if (!this.formColabRolId()) {
      this.toastService.advertencia('Selecciona el rol del colaborador');
      return;
    }
    if ((this.formColabSalario() || 0) < 1025) {
      this.toastService.advertencia('El salario debe ser mayor o igual a S/ 1,025.00');
      return;
    }
    const valContr = Validaciones.validarFechaObligatoria(this.formColabFechaContrato(), 'La fecha de contrato');
    if (!valContr.valido) { this.toastService.advertencia(valContr.mensaje!); return; }

    this.guardandoEmpleado.set(true);

    const payload = {
      nombres: nombres.toUpperCase(),
      apellidoPaterno: apPat.toUpperCase(),
      apellidoMaterno: apMat ? apMat.toUpperCase() : undefined,
      idTipoDocumento: 1, // DNI
      numeroDocumento: dni,
      telefono: telefono,
      fechaNacimiento: this.formColabFechaNacimiento(),
      direccion: this.formColabDireccion().trim(),
      referencia: this.formColabReferencia().trim() || undefined,
      idUbigeo: this.formColabDistritoId(),
      correo: correo,
      password: password,
      idRol: this.formColabRolId() || 2,
      fechaContrato: this.formColabFechaContrato(),
      salario: this.formColabSalario()
    };

    this.adminService.crearEmpleadoCompleto(payload).subscribe({
      next: () => {
        this.guardandoEmpleado.set(false);
        this.cerrarModalNuevoEmpleado();
        this.recargarEmpleados();
        this.recargarUsuarios();
        this.toastService.exito('Colaborador y cuenta de usuario creados exitosamente');
      },
      error: (err) => {
        this.guardandoEmpleado.set(false);
        this.toastService.error(err.error?.mensaje || 'Error al registrar el colaborador');
      }
    });
  }

  abrirModalEditarEmpleado(emp: EmpleadoAdmin): void {
    this.empleadoEditando.set(emp);
    const p = emp.persona || this.personas().find(x => x.idPersona === emp.persona?.idPersona);

    this.formEditNombres.set(p?.nombres || '');
    this.formEditApellidoPaterno.set(p?.apellidoPaterno || '');
    this.formEditApellidoMaterno.set(p?.apellidoMaterno || '');
    this.formEditNumeroDocumento.set(p?.numeroDocumento || '');
    this.formEditFechaNacimiento.set(p?.fechaNacimiento ? p.fechaNacimiento.substring(0, 10) : '');
    this.formEditTelefono.set(p?.telefono || '');
    this.formEditDireccion.set(p?.direccion || '');
    this.formEditReferencia.set(p?.referencia || '');
    this.formEditSalario.set(emp.salario || 1800);
    const fContr = emp.fechaContrato || emp.fechaContratacion;
    this.formEditFechaContrato.set(fContr ? fContr.substring(0, 10) : new Date().toISOString().substring(0, 10));
    this.formEditVigente.set(emp.vigente !== false);
    this.formEditPassword.set('');

    // Usuario vinculado
    const usuarioAsoc = this.usuarios().find(u => u.persona?.idPersona === p?.idPersona);
    if (usuarioAsoc) {
      this.formEditCorreo.set(usuarioAsoc.correo || '');
      this.formEditIdRol.set(usuarioAsoc.rol?.idRol || 2);
    } else {
      const baseCorreo = `${(p?.nombres || 'colab').split(' ')[0].toLowerCase()}.${(p?.apellidoPaterno || 'techzone').toLowerCase()}@techzone.pe`;
      this.formEditCorreo.set(baseCorreo);
      this.formEditIdRol.set(2);
    }

    // Ubigeo
    const ub = p?.ubigeo || this.ubigeos().find(u => u.idUbigeo === (p as any)?.idUbigeo);
    if (ub) {
      this.formEditDepartamento.set(ub.departamento || '');
      this.formEditProvincia.set(ub.provincia || '');
      this.formEditDistritoId.set(ub.idUbigeo || null);
    } else {
      this.formEditDepartamento.set('');
      this.formEditProvincia.set('');
      this.formEditDistritoId.set(null);
    }

    this.mostrarModalEditarEmpleado.set(true);
  }

  cerrarModalEditarEmpleado(): void {
    this.mostrarModalEditarEmpleado.set(false);
    this.empleadoEditando.set(null);
    this.guardandoEdicionEmpleado.set(false);
  }

  guardarEditarEmpleado(): void {
    const emp = this.empleadoEditando();
    if (!emp) return;

    const valNombres = Validaciones.validarTextoNombre(this.formEditNombres(), 'Los nombres');
    if (!valNombres.valido) { this.toastService.advertencia(valNombres.mensaje!); return; }

    const valApPat = Validaciones.validarTextoNombre(this.formEditApellidoPaterno(), 'El apellido paterno');
    if (!valApPat.valido) { this.toastService.advertencia(valApPat.mensaje!); return; }

    const valApMat = Validaciones.validarTextoNombre(this.formEditApellidoMaterno(), 'El apellido materno');
    if (!valApMat.valido) { this.toastService.advertencia(valApMat.mensaje!); return; }

    const valDni = Validaciones.validarDni(this.formEditNumeroDocumento());
    if (!valDni.valido) { this.toastService.advertencia(valDni.mensaje!); return; }

    const valTel = Validaciones.validarTelefono(this.formEditTelefono());
    if (!valTel.valido) { this.toastService.advertencia(valTel.mensaje!); return; }

    const valNac = Validaciones.validarMayorDeEdad(this.formEditFechaNacimiento());
    if (!valNac.valido) { this.toastService.advertencia(valNac.mensaje!); return; }

    const valUbi = Validaciones.validarUbigeo(this.formEditDistritoId());
    if (!valUbi.valido) { this.toastService.advertencia(valUbi.mensaje!); return; }

    const valDir = Validaciones.validarDireccion(this.formEditDireccion());
    if (!valDir.valido) { this.toastService.advertencia(valDir.mensaje!); return; }

    const valCorreo = Validaciones.validarCorreo(this.formEditCorreo());
    if (!valCorreo.valido) { this.toastService.advertencia(valCorreo.mensaje!); return; }

    if (!this.formEditIdRol()) {
      this.toastService.advertencia('Selecciona el rol del colaborador');
      return;
    }

    if ((this.formEditSalario() || 0) < 1025) {
      this.toastService.advertencia('El salario debe ser mayor o igual a S/ 1,025.00');
      return;
    }

    const valContr = Validaciones.validarFechaObligatoria(this.formEditFechaContrato(), 'La fecha de contrato');
    if (!valContr.valido) { this.toastService.advertencia(valContr.mensaje!); return; }

    if (this.formEditPassword().trim()) {
      const valPass = Validaciones.validarContrasena(this.formEditPassword());
      if (!valPass.valido) { this.toastService.advertencia(valPass.mensaje!); return; }
    }

    this.guardandoEdicionEmpleado.set(true);

    const payload = {
      nombres: this.formEditNombres().trim().toUpperCase(),
      apellidoPaterno: this.formEditApellidoPaterno().trim().toUpperCase(),
      apellidoMaterno: this.formEditApellidoMaterno().trim().toUpperCase(),
      idTipoDocumento: 1,
      numeroDocumento: this.formEditNumeroDocumento().trim(),
      telefono: this.formEditTelefono().trim(),
      fechaNacimiento: this.formEditFechaNacimiento(),
      direccion: this.formEditDireccion().trim(),
      referencia: this.formEditReferencia().trim() || undefined,
      idUbigeo: this.formEditDistritoId(),
      correo: this.formEditCorreo().trim().toLowerCase(),
      password: this.formEditPassword().trim() || undefined,
      idRol: this.formEditIdRol(),
      fechaContrato: this.formEditFechaContrato(),
      salario: this.formEditSalario(),
      vigente: this.formEditVigente()
    };

    this.adminService.actualizarEmpleadoCompleto(emp.idEmpleado, payload).subscribe({
      next: () => {
        this.guardandoEdicionEmpleado.set(false);
        this.toastService.exito('Colaborador y credenciales actualizados exitosamente');
        this.cerrarModalEditarEmpleado();
        this.recargarEmpleados();
        this.recargarUsuarios();
        this.recargarPersonas();
      },
      error: (err) => {
        this.guardandoEdicionEmpleado.set(false);
        this.toastService.error(err.error?.mensaje || 'Error al actualizar colaborador');
      }
    });
  }

  cambiarEstadoEmpleado(emp: EmpleadoAdmin): void {
    const nuevoEstado = !emp.vigente;
    const accion = nuevoEstado ? 'reactivar' : 'desactivar';
    const botonTexto = nuevoEstado ? 'Reactivar' : 'Desactivar';
    this.solicitarConfirmacion(
      `¿${nuevoEstado ? 'Reactivar' : 'Desactivar'} Colaborador?`,
      `¿Deseas ${accion} a ${emp.nombreCompleto || emp.persona?.nombres || 'este colaborador'}?`,
      () => {
        this.adminService.actualizarEmpleado(emp.idEmpleado, { vigente: nuevoEstado }).subscribe({
          next: () => {
            this.toastService.exito(`Colaborador ${nuevoEstado ? 'activado' : 'desactivado'}`);
            this.recargarEmpleados();
            this.recargarUsuarios();
            this.recargarPersonas();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al cambiar estado')
        });
      },
      botonTexto,
      !nuevoEstado,
      nuevoEstado ? 'primario' : 'advertencia'
    );
  }

  // ==========================================
  // OPERACIONES: ROLES & MATRIZ DE PERMISOS
  // ==========================================
  abrirModalNuevoRol(): void {
    this.esEdicionRol.set(false);
    this.idRolEditando.set(null);
    this.formRolNombre.set('');
    this.formRolVigente.set(true);
    this.formRolEsAdministrativo.set(false);
    this.formRolPermisos.set({});
    this.mostrarModalRol.set(true);
  }

  abrirModalEditarRol(r: RolAdmin): void {
    this.esEdicionRol.set(true);
    this.idRolEditando.set(r.idRol);
    this.formRolNombre.set(r.nombre || '');
    this.formRolVigente.set(r.vigente !== false);

    const esAdminNombre = (r.nombre || '').toUpperCase().includes('ADMIN') || r.idRol === 1;
    const esAdmin = esAdminNombre || !!(r.esAdministrativo ?? (r as any).es_administrativo);
    this.formRolEsAdministrativo.set(esAdmin);

    let parsedPermisos: Record<string, string[]> = {};
    if (r.permisos) {
      try {
        parsedPermisos = JSON.parse(r.permisos);
      } catch {
        parsedPermisos = {};
      }
    }
    // Si es Administrador, garantizar acceso a todos los módulos y operaciones permitidas
    if (esAdminNombre) {
      for (const mod of this.MODULOS_SISTEMA) {
        if (!parsedPermisos[mod.clave] || parsedPermisos[mod.clave].length === 0) {
          parsedPermisos[mod.clave] = [...mod.acciones];
        }
      }
    }
    this.formRolPermisos.set(parsedPermisos);
    this.mostrarModalRol.set(true);
  }

  cambiarRolEsAdministrativo(valor: boolean): void {
    const esAdminNombre = (this.formRolNombre() || '').toUpperCase().includes('ADMIN') || this.idRolEditando() === 1;
    if (this.esEdicionRol() && esAdminNombre && !valor) {
      this.toastService.advertencia('El rol Administrador debe permanecer siempre de tipo administrativo');
      return;
    }
    this.formRolEsAdministrativo.set(valor);
    if (!valor) {
      this.formRolPermisos.set({});
    } else if (Object.keys(this.formRolPermisos()).length === 0) {
      this.formRolPermisos.set({
        dashboard: ['ver'],
        productos: ['ver']
      });
    }
  }

  cerrarModalRol(): void {
    this.mostrarModalRol.set(false);
    this.idRolEditando.set(null);
  }

  tienePermisoRol(modulo: string, accion: string): boolean {
    const actual = this.formRolPermisos();
    const accionesModulo = actual[modulo] || [];
    return accionesModulo.includes(accion);
  }

  togglePermisoRol(modulo: string, accion: string): void {
    if (!this.accionPermitidaEnModulo(modulo, accion)) {
      return;
    }
    const actual = { ...this.formRolPermisos() };
    const lista = [...(actual[modulo] || [])];
    const idx = lista.indexOf(accion);
    if (idx >= 0) {
      if (accion === 'ver') {
        // Desmarcar VER revoca todo el módulo
        delete actual[modulo];
      } else {
        lista.splice(idx, 1);
        if (lista.length === 0) {
          delete actual[modulo];
        } else {
          actual[modulo] = lista;
        }
      }
    } else {
      // Activar CREAR, EDITAR o ELIMINAR auto-marca VER
      if (accion !== 'ver' && !lista.includes('ver')) {
        lista.unshift('ver');
      }
      lista.push(accion);
      actual[modulo] = lista;
    }
    this.formRolPermisos.set(actual);
  }

  marcarTodosPermisosRol(): void {
    const nuevo: Record<string, string[]> = {};
    for (const mod of this.MODULOS_SISTEMA) {
      nuevo[mod.clave] = [...mod.acciones];
    }
    this.formRolPermisos.set(nuevo);
  }

  desmarcarTodosPermisosRol(): void {
    this.formRolPermisos.set({});
  }

  guardarRol(): void {
    const nombre = this.formRolNombre().trim();
    if (!nombre) {
      this.toastService.advertencia('Ingresa el nombre del rol');
      return;
    }
    if (nombre.length < 2) {
      this.toastService.advertencia('El nombre del rol debe tener al menos 2 caracteres');
      return;
    }
    if (nombre.length > 50) {
      this.toastService.advertencia('El nombre del rol no debe superar 50 caracteres');
      return;
    }

    const permisosJson = this.formRolEsAdministrativo() ? JSON.stringify(this.formRolPermisos()) : '{}';
    const payload: Partial<RolAdmin> = {
      nombre,
      vigente: this.formRolVigente(),
      esAdministrativo: this.formRolEsAdministrativo(),
      permisos: permisosJson
    };

    if (this.esEdicionRol()) {
      const id = this.idRolEditando()!;
      this.adminService.actualizarRol(id, payload).subscribe({
        next: () => {
          this.toastService.exito(`Rol "${nombre}" actualizado exitosamente`);
          this.cerrarModalRol();
          this.recargarRoles();
          this.authService.validarSesionConServidor();
        },
        error: (err) => {
          this.toastService.error(err.error?.mensaje || 'Error al actualizar rol');
        }
      });
    } else {
      this.adminService.crearRol(payload).subscribe({
        next: () => {
          this.toastService.exito(`Rol "${nombre}" creado exitosamente`);
          this.cerrarModalRol();
          this.recargarRoles();
          this.authService.validarSesionConServidor();
        },
        error: (err) => {
          this.toastService.error(err.error?.mensaje || 'Error al crear rol');
        }
      });
    }
  }

  // ==========================================
  // OPERACIONES: EDICIÓN DE USUARIOS
  // ==========================================
  abrirModalEditarUsuario(u: UsuarioAdmin): void {
    this.usuarioEditando.set(u);
    this.formUsuarioRolId.set(u.rol?.idRol || null);
    this.formUsuarioEstado.set(u.estado ? u.estado.toLowerCase() : 'activo');
    this.formUsuarioNuevaPassword.set('');
    this.mostrarPasswordUsuario.set(false);
    this.mostrarModalEditarUsuario.set(true);
  }

  cerrarModalEditarUsuario(): void {
    this.mostrarModalEditarUsuario.set(false);
    this.usuarioEditando.set(null);
  }

  guardarEditarUsuario(): void {
    const u = this.usuarioEditando();
    if (!u) return;

    const rolId = this.formUsuarioRolId();
    if (!rolId) {
      this.toastService.advertencia('Debes seleccionar un rol para el usuario');
      return;
    }

    const payload: any = {
      correo: u.correo,
      estado: this.formUsuarioEstado(),
      rol: { idRol: rolId }
    };

    const nuevaClave = this.formUsuarioNuevaPassword().trim();
    if (nuevaClave) {
      const valPass = Validaciones.validarContrasena(nuevaClave);
      if (!valPass.valido) {
        this.toastService.advertencia(valPass.mensaje!);
        return;
      }
      payload.password = nuevaClave;
    }

    this.adminService.actualizarUsuario(u.idUsuario, payload).subscribe({
      next: () => {
        this.toastService.exito('Usuario actualizado exitosamente');
        this.cerrarModalEditarUsuario();
        this.recargarUsuarios();
      },
      error: (err) => {
        this.toastService.error(err.error?.mensaje || 'Error al actualizar usuario');
      }
    });
  }

  cambiarEstadoUsuario(u: UsuarioAdmin): void {
    const estadoActual = (u.estado || 'activo').toLowerCase();
    const nuevoEstado = estadoActual === 'activo' ? 'inactivo' : 'activo';
    const accion = nuevoEstado === 'activo' ? 'reactivar' : 'desactivar';
    const botonTexto = nuevoEstado === 'activo' ? 'Reactivar' : 'Desactivar';

    this.solicitarConfirmacion(
      `¿${nuevoEstado === 'activo' ? 'Reactivar' : 'Desactivar'} Usuario?`,
      `¿Deseas ${accion} la cuenta de "${u.correo}"?`,
      () => {
        this.adminService.actualizarUsuario(u.idUsuario, {
          correo: u.correo,
          estado: nuevoEstado,
          rol: u.rol
        }).subscribe({
          next: () => {
            this.toastService.exito(`Usuario ${nuevoEstado === 'activo' ? 'activado' : 'desactivado'}`);
            this.recargarUsuarios();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al cambiar estado de usuario')
        });
      },
      botonTexto,
      nuevoEstado !== 'activo',
      nuevoEstado === 'activo' ? 'primario' : 'advertencia'
    );
  }

  // ==========================================
  // OPERACIONES: EDICIÓN DE MEDIOS DE PAGO
  // ==========================================
  abrirModalEditarMedioPago(mp: MedioPagoAdmin): void {
    this.medioPagoEditando.set(mp);
    this.formMedioPagoInstrucciones.set(mp.instrucciones || this.getInstruccionesMedioPago(mp));
    this.formMedioPagoVigente.set(mp.vigente !== false);
    this.mostrarModalEditarMedioPago.set(true);
  }

  cerrarModalEditarMedioPago(): void {
    this.mostrarModalEditarMedioPago.set(false);
    this.medioPagoEditando.set(null);
  }

  guardarEditarMedioPago(): void {
    const mp = this.medioPagoEditando();
    if (!mp) return;

    const payload = {
      nombre: mp.nombre,
      instrucciones: this.formMedioPagoInstrucciones().trim(),
      vigente: this.formMedioPagoVigente()
    };

    this.adminService.actualizarMedioPago(mp.idMedioPago, payload).subscribe({
      next: () => {
        this.toastService.exito('Medio de pago actualizado');
        this.cerrarModalEditarMedioPago();
        this.recargarMediosPago();
      },
      error: (err) => this.toastService.error(err.error?.mensaje || 'Error al actualizar medio de pago')
    });
  }

  cambiarEstadoMedioPago(mp: MedioPagoAdmin): void {
    const nuevoEstado = !mp.vigente;
    const accion = nuevoEstado ? 'habilitar' : 'deshabilitar';
    const botonTexto = nuevoEstado ? 'Habilitar' : 'Deshabilitar';
    this.solicitarConfirmacion(
      `¿${nuevoEstado ? 'Habilitar' : 'Deshabilitar'} Medio de Pago?`,
      `¿Deseas ${accion} el método "${mp.nombre}"?`,
      () => {
        this.adminService.actualizarMedioPago(mp.idMedioPago, {
          nombre: mp.nombre,
          instrucciones: mp.instrucciones,
          vigente: nuevoEstado
        }).subscribe({
          next: () => {
            this.toastService.exito(`Medio de pago ${nuevoEstado ? 'habilitado' : 'deshabilitado'}`);
            this.recargarMediosPago();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al cambiar estado')
        });
      },
      botonTexto,
      !nuevoEstado,
      nuevoEstado ? 'primario' : 'advertencia'
    );
  }

  eliminarTipoDoc(td: TipoDocumentoAdmin): void {
    this.eliminarTipoDocumento(td);
  }

  // ==========================================
  // OPERACIONES: VENTAS (Cambio de Estado)
  // ==========================================
  cambiarEstadoVenta(id: number, estado: string): void {
    const etiquetas: Record<string, string> = {
      'EN_PREPARACION': 'alistar y empaquetar el pedido (EN PREPARACIÓN)',
      'EN_CAMINO': 'despachar con repartidor (EN CAMINO)',
      'ENTREGADO': 'confirmar entrega al cliente (ENTREGADO)',
      'CANCELADA': 'cancelar la venta y devolver los productos al stock'
    };
    const texto = etiquetas[estado] || `cambiar estado a ${estado}`;
    this.solicitarConfirmacion(
      `¿Actualizar Pedido #${id}?`,
      `¿Deseas ${texto} para la venta #${id}?`,
      () => {
        // ⚡ ACTUALIZACIÓN OPTIMISTA INMEDIATA (0 ms)
        const vAnterior = this.ventas().find(v => v.idVenta === id);
        const estadoAnterior = vAnterior?.estado || 'PENDIENTE';

        // Actualizar UI inmediatamente
        this.ventas.update(list => list.map(v => v.idVenta === id ? { ...v, estado } : v));
        this.detallesVenta.update(list => list.map(dv => {
          if (dv.venta?.idVenta === id) {
            return { ...dv, venta: { ...dv.venta, estado } };
          }
          return dv;
        }));
        this.toastService.exito(`Pedido #${id} actualizado a ${estado}`);

        // Persistir en segundo plano (sin recargar 22 tablas)
        this.ventaService.adminActualizarEstadoVenta(id, estado).subscribe({
          error: (err) => {
            // Revertir solo si falló
            this.ventas.update(list => list.map(v => v.idVenta === id ? { ...v, estado: estadoAnterior } : v));
            this.detallesVenta.update(list => list.map(dv => {
              if (dv.venta?.idVenta === id) {
                return { ...dv, venta: { ...dv.venta, estado: estadoAnterior } };
              }
              return dv;
            }));
            this.toastService.error(err.error?.mensaje || 'Error al actualizar estado');
          }
        });
      },
      'Confirmar',
      estado === 'CANCELADA'
    );
  }

  recuperarDetallesDeVentas(): void {
    const list = this.ventas();
    if (!list || list.length === 0) return;

    const itemsTotales: DetalleVentaAdmin[] = [];
    let pendientes = list.length;

    list.forEach(v => {
      this.ventaService.adminGetVentaDetalle(v.idVenta).subscribe({
        next: (ventaComp) => {
          if (ventaComp && ventaComp.detalles) {
            ventaComp.detalles.forEach(d => {
              itemsTotales.push({
                idDetalleVenta: d.idDetalleVenta,
                venta: {
                  idVenta: v.idVenta,
                  total: v.total,
                  estado: v.estado
                },
                producto: {
                  idProducto: d.idProducto,
                  nombre: d.nombreProducto,
                  precioVenta: d.precioUnitario
                },
                cantidad: d.cantidad,
                precioUnitario: d.precioUnitario,
                subTotalVenta: d.subTotalVenta
              });
            });
          }
          pendientes--;
          if (pendientes === 0 && itemsTotales.length > 0) {
            this.detallesVenta.set(itemsTotales);
          }
        },
        error: () => {
          pendientes--;
          if (pendientes === 0 && itemsTotales.length > 0) {
            this.detallesVenta.set(itemsTotales);
          }
        }
      });
    });
  }

  verComprobanteVenta(id: number): void {
    this.cargandoComprobanteAdmin.set(true);
    this.ventaService.adminGetVentaDetalle(id).subscribe({
      next: (data) => {
        this.comprobanteAdmin.set(data);
        this.cargandoComprobanteAdmin.set(false);
      },
      error: (err) => {
        this.cargandoComprobanteAdmin.set(false);
        this.toastService.error(err.error?.mensaje || 'Error al obtener detalle de venta');
      }
    });
  }

  obtenerFechaVenta(idVenta?: number): string | undefined {
    if (!idVenta) return undefined;
    return this.ventas().find(v => v.idVenta === idVenta)?.fechaVenta;
  }

  obtenerEstadoVenta(idVenta?: number): string | undefined {
    if (!idVenta) return undefined;
    return this.ventas().find(v => v.idVenta === idVenta)?.estado;
  }

  cerrarComprobanteAdmin(): void {
    this.comprobanteAdmin.set(null);
  }

  imprimirComprobanteAdmin(): void {
    if (typeof window !== 'undefined') {
      window.print();
    }
  }

  esFacturaAdmin(c: VentaCompleta): boolean {
    const doc = c.clienteDocumento || '';
    return doc.length === 11 || (c.clienteNombre || '').toUpperCase().includes('S.A.C') || (c.clienteNombre || '').toUpperCase().includes('S.R.L');
  }

  generarHashSimuladoAdmin(c: VentaCompleta): string {
    const str = `${c.idVenta}|${c.total}|${c.fechaVenta}`;
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) - hash) + str.charCodeAt(i);
      hash |= 0;
    }
    const hex = Math.abs(hash).toString(16).padStart(8, '0');
    return `${hex}e8a49c71b02f5a6b8c9d0e1f2a3b4c5d`.slice(0, 28);
  }

  cancelarVenta(id: number): void {
    this.cambiarEstadoVenta(id, 'CANCELADA');
  }

  // ==========================================
  // OPERACIONES: IMÁGENES & DETALLES
  // ==========================================
  // ==========================================
  // OPERACIONES: GALERÍA DE IMÁGENES (SUPABASE)
  // ==========================================
  getProductoDeImagen(img: ProductoImagenAdmin): Producto | undefined {
    const id = img.producto?.idProducto ?? img.idProducto ?? (img as any).id_producto;
    if (id) {
      const match = this.productos().find(p => p.idProducto === Number(id));
      if (match) return match;
    }
    // Fallback directo por ID: las 8 imágenes de catálogo inicial corresponden a los 8 productos
    if (img.idProductoImagen) {
      const matchFallback = this.productos().find(p => p.idProducto === Number(img.idProductoImagen));
      if (matchFallback) return matchFallback;
    }
    return undefined;
  }

  // Métodos de navegación del Explorador de Imágenes
  seleccionarCarpetaCategoria(cat: Categoria): void {
    this.carpetaCategoriaSel.set(cat);
    this.carpetaMarcaSel.set(null);
    this.carpetaProductoSel.set(null);
  }

  seleccionarCarpetaMarca(marca: Marca): void {
    this.carpetaMarcaSel.set(marca);
    this.carpetaProductoSel.set(null);
  }

  seleccionarCarpetaProducto(prod: Producto): void {
    this.carpetaProductoSel.set(prod);
  }

  irRaizCarpetas(): void {
    this.carpetaCategoriaSel.set(null);
    this.carpetaMarcaSel.set(null);
    this.carpetaProductoSel.set(null);
  }

  irACategoriaCarpetas(): void {
    this.carpetaMarcaSel.set(null);
    this.carpetaProductoSel.set(null);
  }

  irAMarcaCarpetas(): void {
    this.carpetaProductoSel.set(null);
  }

  conteoProductosCategoria(idCat: number): number {
    return this.productos().filter(p => p.categoria?.idCategoria === idCat).length;
  }

  conteoImagenesCategoria(idCat: number): number {
    return this.imagenes().filter(img => {
      const p = this.getProductoDeImagen(img);
      return p?.categoria?.idCategoria === idCat;
    }).length;
  }

  marcasEnCategoria(idCat: number): Marca[] {
    const marcasIds = new Set(
      this.productos()
        .filter(p => p.categoria?.idCategoria === idCat && p.marca?.idMarca)
        .map(p => p.marca!.idMarca)
    );
    return this.marcas().filter(m => marcasIds.has(m.idMarca));
  }

  conteoProductosMarcaEnCat(idCat: number, idMarca: number): number {
    return this.productos().filter(p => p.categoria?.idCategoria === idCat && p.marca?.idMarca === idMarca).length;
  }

  conteoImagenesMarcaEnCat(idCat: number, idMarca: number): number {
    return this.imagenes().filter(img => {
      const p = this.getProductoDeImagen(img);
      return p?.categoria?.idCategoria === idCat && p?.marca?.idMarca === idMarca;
    }).length;
  }

  productosEnMarcaYCategoria(idCat: number, idMarca: number): Producto[] {
    return this.productos().filter(p => p.categoria?.idCategoria === idCat && p.marca?.idMarca === idMarca);
  }

  conteoImagenesProducto(idProd: number): number {
    return this.imagenes().filter(img => {
      const p = this.getProductoDeImagen(img);
      return p?.idProducto === idProd;
    }).length;
  }

  imagenesDeProducto(idProd: number): ProductoImagenAdmin[] {
    return this.imagenes().filter(img => {
      const p = this.getProductoDeImagen(img);
      return p?.idProducto === idProd;
    });
  }

  establecerImagenPrincipal(idImagen: number): void {
    this.productoService.adminEstablecerImagenPrincipal(idImagen).subscribe({
      next: () => {
        this.toastService.exito('Imagen establecida como principal');
        this.cargarDatos();
      },
      error: (err) => this.toastService.error(err.error?.mensaje || 'Error al marcar imagen principal')
    });
  }

  generarSlug(texto: string): string {
    return (texto || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }

  abrirModalNuevaImagen(idProducto?: number): void {
    let prodTarget: Producto | null = null;
    if (idProducto) {
      prodTarget = this.productos().find(p => p.idProducto === idProducto) || null;
    } else if (this.carpetaProductoSel()) {
      prodTarget = this.carpetaProductoSel();
    }

    if (prodTarget) {
      this.modoCarpetaSubida.set('existente');
      this.productoSelSubida.set(prodTarget);
      this.idProductoParaImagen.set(prodTarget.idProducto);
      this.catSelSubida.set(prodTarget.categoria || null);
      this.marcaSelSubida.set(prodTarget.marca || null);
      if (prodTarget.categoria?.idCategoria === 7) {
        const mNorm = (prodTarget.marca?.nombre || '').toLowerCase();
        this.subcatPerifericosSubida.set(mNorm.includes('hyperx') || prodTarget.marca?.idMarca === 16 ? 'audifonos' : 'teclados');
      } else {
        this.subcatPerifericosSubida.set(null);
      }
    } else {
      this.modoCarpetaSubida.set('existente');
      const catActual = this.carpetaCategoriaSel() || (this.categorias().length > 0 ? this.categorias()[0] : null);
      this.catSelSubida.set(catActual);
      this.subcatPerifericosSubida.set(this.carpetaSubcategoriaSel() || null);
      const marcasDisp = this.marcasDisponiblesSubida();
      this.marcaSelSubida.set(this.carpetaMarcaSel() || (marcasDisp.length > 0 ? marcasDisp[0] : null));
      const prodsDisp = this.productosDisponiblesSubida();
      const primerProd = prodsDisp.length > 0 ? prodsDisp[0] : null;
      this.productoSelSubida.set(primerProd);
      this.idProductoParaImagen.set(primerProd?.idProducto || null);
    }

    this.nombreNuevaCarpetaProducto.set('');
    this.precioNuevoProductoSubida.set(99.00);
    this.stockNuevoProductoSubida.set(10);
    this.descripcionNuevaCarpetaProducto.set('');
    this.archivosSubidaLote.set([]);
    this.indicePrincipalLote.set(0);
    this.subiendoLote.set(false);
    this.progresoSubidaLote.set('');
    this.urlNuevaImagen.set('');
    this.archivoNuevaImagen = null;
    this.previewNuevaImagen.set(null);
    this.mostrarModalNuevaImagen.set(true);
  }

  cerrarModalNuevaImagen(): void {
    this.mostrarModalNuevaImagen.set(false);
    this.archivosSubidaLote.set([]);
    this.subiendoLote.set(false);
    this.progresoSubidaLote.set('');
  }

  cambiarModoCarpetaSubida(modo: 'existente' | 'nueva'): void {
    this.modoCarpetaSubida.set(modo);
    this.recalcularNombresLote();
  }

  onCategoriaSubidaChange(cat: Categoria | null): void {
    this.catSelSubida.set(cat);
    if (cat?.idCategoria === 7 && !this.subcatPerifericosSubida()) {
      this.subcatPerifericosSubida.set('audifonos');
    } else if (cat?.idCategoria !== 7) {
      this.subcatPerifericosSubida.set(null);
    }
    const marcasDisp = this.marcasDisponiblesSubida();
    this.marcaSelSubida.set(marcasDisp.length > 0 ? marcasDisp[0] : null);
    const prodsDisp = this.productosDisponiblesSubida();
    const prod = prodsDisp.length > 0 ? prodsDisp[0] : null;
    this.productoSelSubida.set(prod);
    this.idProductoParaImagen.set(prod?.idProducto || null);
    this.recalcularNombresLote();
  }

  onSubcatPerifericosSubidaChange(sub: 'audifonos' | 'teclados' | null): void {
    this.subcatPerifericosSubida.set(sub);
    const marcasDisp = this.marcasDisponiblesSubida();
    this.marcaSelSubida.set(marcasDisp.length > 0 ? marcasDisp[0] : null);
    const prodsDisp = this.productosDisponiblesSubida();
    const prod = prodsDisp.length > 0 ? prodsDisp[0] : null;
    this.productoSelSubida.set(prod);
    this.idProductoParaImagen.set(prod?.idProducto || null);
    this.recalcularNombresLote();
  }

  onMarcaSubidaChange(marca: Marca | null): void {
    this.marcaSelSubida.set(marca);
    const prodsDisp = this.productosDisponiblesSubida();
    const prod = prodsDisp.length > 0 ? prodsDisp[0] : null;
    this.productoSelSubida.set(prod);
    this.idProductoParaImagen.set(prod?.idProducto || null);
    this.recalcularNombresLote();
  }

  onProductoSubidaChange(prod: Producto | null): void {
    this.productoSelSubida.set(prod);
    this.idProductoParaImagen.set(prod?.idProducto || null);
    this.recalcularNombresLote();
  }

  onNombreNuevaCarpetaChange(nombre: string): void {
    this.nombreNuevaCarpetaProducto.set(nombre);
    this.recalcularNombresLote();
  }

  onSeleccionarArchivosLote(event: any): void {
    const files: FileList = event.target?.files;
    if (!files || files.length === 0) return;
    const listaActual = [...this.archivosSubidaLote()];
    const nuevosArchivos = Array.from(files);

    if (listaActual.length + nuevosArchivos.length > 8) {
      this.toastService.advertencia('Se permite un máximo de 8 fotos por producto / lote.');
    }

    const permitidos = nuevosArchivos.slice(0, Math.max(0, 8 - listaActual.length));
    permitidos.forEach((file) => {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const preview = e.target.result;
        this.archivosSubidaLote.update(lista => [
          ...lista,
          {
            archivo: file,
            preview,
            nombreGenerado: file.name,
            esPrincipal: lista.length === 0
          }
        ]);
        this.recalcularNombresLote();
      };
      reader.readAsDataURL(file);
    });
  }

  removerArchivoLote(index: number): void {
    this.archivosSubidaLote.update(lista => {
      const nueva = lista.filter((_, i) => i !== index);
      if (index === this.indicePrincipalLote()) {
        this.indicePrincipalLote.set(0);
      } else if (index < this.indicePrincipalLote()) {
        this.indicePrincipalLote.update(i => i - 1);
      }
      return nueva;
    });
    this.recalcularNombresLote();
  }

  marcarPrincipalLote(index: number): void {
    this.indicePrincipalLote.set(index);
    this.recalcularNombresLote();
  }

  recalcularNombresLote(): void {
    let slugBase = 'producto';
    if (this.modoCarpetaSubida() === 'existente') {
      const prod = this.productoSelSubida();
      if (prod) slugBase = this.generarSlug(prod.nombre);
    } else {
      const nombreNueva = this.nombreNuevaCarpetaProducto().trim();
      if (nombreNueva) slugBase = this.generarSlug(nombreNueva);
    }

    let offset = 0;
    if (this.modoCarpetaSubida() === 'existente' && this.productoSelSubida()) {
      const idProd = this.productoSelSubida()!.idProducto;
      offset = this.conteoImagenesProducto(idProd);
    }

    const principalIdx = this.indicePrincipalLote();
    this.archivosSubidaLote.update(lista =>
      lista.map((item, i) => ({
        ...item,
        nombreGenerado: `${slugBase}_item${offset + i + 1}.webp`,
        esPrincipal: i === principalIdx
      }))
    );
  }

  convertirAWebP(file: File, nombreFinal: string): Promise<File> {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        let ancho = img.width;
        let alto = img.height;
        const maxDim = 1200;

        if (ancho > maxDim || alto > maxDim) {
          if (ancho > alto) {
            alto = Math.round((alto * maxDim) / ancho);
            ancho = maxDim;
          } else {
            ancho = Math.round((ancho * maxDim) / alto);
            alto = maxDim;
          }
        }
        canvas.width = ancho;
        canvas.height = alto;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          return resolve(new File([file], nombreFinal, { type: file.type }));
        }
        ctx.drawImage(img, 0, 0, ancho, alto);
        canvas.toBlob(
          blob => {
            if (!blob) {
              return resolve(new File([file], nombreFinal, { type: file.type }));
            }
            const webpFile = new File([blob], nombreFinal, { type: 'image/webp' });
            resolve(webpFile);
          },
          'image/webp',
          0.90
        );
      };
      img.onerror = () => {
        resolve(new File([file], nombreFinal, { type: file.type }));
      };
      img.src = URL.createObjectURL(file);
    });
  }

  async procesarSubidaLote(): Promise<void> {
    const lista = this.archivosSubidaLote();
    if (lista.length === 0) {
      this.toastService.advertencia('Selecciona de 1 a 8 imágenes para subir');
      return;
    }

    const cat = this.catSelSubida();
    const marca = this.marcaSelSubida();
    if (!cat) {
      this.toastService.advertencia('Selecciona la categoría de destino');
      return;
    }
    if (!marca) {
      this.toastService.advertencia('Selecciona la marca oficial');
      return;
    }

    this.subiendoLote.set(true);
    this.progresoSubidaLote.set('Iniciando subida a Supabase Storage...');

    try {
      let targetProd: Producto | null = null;

      if (this.modoCarpetaSubida() === 'existente') {
        targetProd = this.productoSelSubida();
        if (!targetProd) {
          this.toastService.advertencia('Selecciona el producto / carpeta de destino');
          this.subiendoLote.set(false);
          return;
        }
      } else {
        const nombreProd = this.nombreNuevaCarpetaProducto().trim();
        if (!nombreProd) {
          this.toastService.advertencia('Ingresa el nombre del nuevo producto / carpeta');
          this.subiendoLote.set(false);
          return;
        }
        this.progresoSubidaLote.set('Creando carpeta y registro de producto...');
        const nuevoPayload = {
          nombre: nombreProd,
          codigoSku: (marca.nombre.substring(0, 2) + '0' + Math.floor(100 + Math.random() * 900)).toUpperCase(),
          idCategoria: cat.idCategoria,
          idMarca: marca.idMarca,
          precioVenta: this.precioNuevoProductoSubida() || 99,
          stock: this.stockNuevoProductoSubida() || 10,
          descripcion: this.descripcionNuevaCarpetaProducto().trim() || `${nombreProd} oficial.`,
          vigente: true
        };

        const prodCreado = await new Promise<Producto>((res, rej) => {
          this.productoService.adminCrearProducto(nuevoPayload).subscribe({
            next: (p) => res(p),
            error: (err) => rej(err)
          });
        });
        targetProd = prodCreado;
        this.productos.update(prods => [prodCreado, ...prods]);
      }

      let subidasExitosas = 0;
      for (let i = 0; i < lista.length; i++) {
        const item = lista[i];
        this.progresoSubidaLote.set(`Subiendo ${i + 1} de ${lista.length}: ${item.nombreGenerado}...`);
        const archivoWebp = await this.convertirAWebP(item.archivo, item.nombreGenerado);

        await new Promise<void>((resolve) => {
          this.productoService.adminSubirImagen(targetProd!.idProducto, archivoWebp, item.esPrincipal).subscribe({
            next: (imgCreada) => {
              subidasExitosas++;
              this.imagenes.update(imgs => [imgCreada as any, ...imgs]);
              resolve();
            },
            error: (err) => {
              console.error('Error subiendo imagen', err);
              resolve();
            }
          });
        });
      }

      this.toastService.exito(`Se subieron exitosamente ${subidasExitosas} fotos a Supabase Storage`);
      this.guardarEnCache();
      this.cerrarModalNuevaImagen();
      this.recargarImagenes();
      this.recargarProductos();

      if (targetProd) {
        this.carpetaCategoriaSel.set(cat);
        if (cat.idCategoria === 7 && this.subcatPerifericosSubida()) {
          this.carpetaSubcategoriaSel.set(this.subcatPerifericosSubida());
        }
        this.carpetaMarcaSel.set(marca);
        this.carpetaProductoSel.set(targetProd);
      }
    } catch (err: any) {
      this.toastService.error(err?.error?.mensaje || 'Error al procesar la subida');
    } finally {
      this.subiendoLote.set(false);
      this.progresoSubidaLote.set('');
    }
  }

  onUrlNuevaImagenChange(url: string): void {
    this.urlNuevaImagen.set(url);
    if (!this.archivoNuevaImagen) {
      this.previewNuevaImagen.set(url.trim() ? url.trim() : null);
    }
  }

  onSeleccionarArchivoNuevaImagen(event: any): void {
    const files = event.target?.files;
    if (files && files.length > 0) {
      this.archivoNuevaImagen = files[0];
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewNuevaImagen.set(e.target.result);
      };
      reader.readAsDataURL(files[0]);
    }
  }

  removerArchivoNuevaImagen(): void {
    this.archivoNuevaImagen = null;
    this.urlNuevaImagen.set('');
    this.previewNuevaImagen.set(null);
  }

  // ==========================================
  // OPERACIONES: EDITAR IMAGEN EXISTENTE
  // ==========================================
  abrirModalEditarImagen(img: ProductoImagenAdmin): void {
    const prod = this.getProductoDeImagen(img);
    this.idImagenEditando.set(img.idProductoImagen);
    this.idProductoEditandoImagen.set(prod ? prod.idProducto : (img.producto?.idProducto || img.idProducto || null));
    this.urlEditandoImagen.set(img.urlImagen || '');
    this.previewEditandoImagen.set(img.urlImagen || null);
    this.archivoEditandoImagen = null;
    this.esPrincipalEditandoImagen.set(!!img.esPrincipal);
    this.guardandoEdicionImagen.set(false);
    this.mostrarModalEditarImagen.set(true);
  }

  cerrarModalEditarImagen(): void {
    this.mostrarModalEditarImagen.set(false);
    this.idImagenEditando.set(null);
    this.archivoEditandoImagen = null;
    this.urlEditandoImagen.set('');
    this.previewEditandoImagen.set(null);
    this.guardandoEdicionImagen.set(false);
  }

  onUrlEditandoImagenChange(url: string): void {
    this.urlEditandoImagen.set(url);
    if (!this.archivoEditandoImagen) {
      this.previewEditandoImagen.set(url.trim() ? url.trim() : null);
    }
  }

  onSeleccionarArchivoEditandoImagen(event: any): void {
    const files = event.target?.files;
    if (files && files.length > 0) {
      this.archivoEditandoImagen = files[0];
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewEditandoImagen.set(e.target.result);
      };
      reader.readAsDataURL(files[0]);
    }
  }

  removerArchivoEditandoImagen(): void {
    this.archivoEditandoImagen = null;
    this.urlEditandoImagen.set('');
    this.previewEditandoImagen.set(null);
  }

  guardarEdicionImagen(): void {
    const idImg = this.idImagenEditando();
    const idProd = this.idProductoEditandoImagen();
    if (!idImg || !idProd) {
      this.toastService.advertencia('Selecciona el producto a vincular');
      return;
    }

    this.guardandoEdicionImagen.set(true);
    const esPrincipal = this.esPrincipalEditandoImagen();

    if (this.archivoEditandoImagen) {
      this.productoService.adminActualizarImagenArchivo(idImg, this.archivoEditandoImagen, idProd, esPrincipal).subscribe({
        next: () => {
          this.guardandoEdicionImagen.set(false);
          this.toastService.exito('Imagen y producto asignado actualizados en Supabase');
          this.cerrarModalEditarImagen();
          this.cargarDatos();
        },
        error: (err) => {
          this.guardandoEdicionImagen.set(false);
          this.toastService.error(err.error?.mensaje || 'Error al actualizar imagen');
        }
      });
    } else {
      const url = this.urlEditandoImagen().trim();
      this.productoService.adminActualizarImagen(idImg, { idProducto: idProd, urlImagen: url, esPrincipal }).subscribe({
        next: () => {
          this.guardandoEdicionImagen.set(false);
          this.toastService.exito('Imagen y producto asignado actualizados correctamente');
          this.cerrarModalEditarImagen();
          this.cargarDatos();
        },
        error: (err) => {
          this.guardandoEdicionImagen.set(false);
          this.toastService.error(err.error?.mensaje || 'Error al actualizar imagen');
        }
      });
    }
  }

  guardarNuevaImagen(): void {
    const idProd = this.idProductoParaImagen();
    if (!idProd) {
      this.toastService.advertencia('Selecciona un producto para asociar la imagen');
      return;
    }

    if (!this.archivoNuevaImagen && !this.urlNuevaImagen().trim()) {
      this.toastService.advertencia('Ingresa una URL o sube un archivo de imagen');
      return;
    }

    this.guardandoNuevaImagen.set(true);
    const esPrincipal = this.esPrincipalNuevaImagen();

    if (this.archivoNuevaImagen) {
      this.productoService.adminSubirImagen(idProd, this.archivoNuevaImagen, esPrincipal).subscribe({
        next: () => {
          this.guardandoNuevaImagen.set(false);
          this.toastService.exito('Imagen subida exitosamente a Supabase Storage');
          this.cerrarModalNuevaImagen();
          this.cargarDatos();
        },
        error: (err) => {
          this.guardandoNuevaImagen.set(false);
          this.toastService.error(err.error?.mensaje || 'Error al subir imagen a Supabase');
        }
      });
    } else {
      this.productoService.adminAgregarImagenPorUrl(idProd, this.urlNuevaImagen().trim(), esPrincipal).subscribe({
        next: () => {
          this.guardandoNuevaImagen.set(false);
          this.toastService.exito('Imagen importada y alojada en Supabase Storage');
          this.cerrarModalNuevaImagen();
          this.cargarDatos();
        },
        error: (err) => {
          this.guardandoNuevaImagen.set(false);
          this.toastService.error(err.error?.mensaje || 'Error al procesar URL de imagen');
        }
      });
    }
  }

  eliminarImagen(id: number): void {
    this.solicitarConfirmacion(
      '¿Eliminar Imagen de Supabase?',
      `¿Estás seguro de que deseas eliminar permanentemente la imagen #${id}?`,
      () => {
        this.adminService.eliminarImagenProducto(id).subscribe({
          next: () => {
            this.toastService.exito('Imagen eliminada de la base de datos y de Supabase');
            this.cargarDatos();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al eliminar imagen')
        });
      }
    );
  }

  eliminarDetalleCarrito(id: number): void {
    this.solicitarConfirmacion(
      '¿Eliminar Ítem del Carrito?',
      `¿Deseas retirar el producto #${id} de este carrito?`,
      () => {
        this.adminService.eliminarDetalleCarrito(id).subscribe({
          next: () => {
            this.toastService.exito('Ítem retirado del carrito');
            this.cargarDatos();
          },
          error: (err) => this.toastService.error(err.error?.mensaje || 'Error al retirar ítem')
        });
      }
    );
  }

  // ==========================================
  // CONFIGURACIÓN DEL SITIO & SUBIDA A SUPABASE
  // ==========================================
  onLogoSelected(event: any): void {
    if (event.target.files && event.target.files.length > 0) {
      this.archivoLogoSitio = event.target.files[0];
      const reader = new FileReader();
      reader.onload = (e: any) => this.previewLogoUrl.set(e.target.result);
      reader.readAsDataURL(this.archivoLogoSitio!);
    }
  }

  onLibroSelected(event: any): void {
    if (event.target.files && event.target.files.length > 0) {
      this.archivoLibroSitio = event.target.files[0];
      const reader = new FileReader();
      reader.onload = (e: any) => this.previewLibroUrl.set(e.target.result);
      reader.readAsDataURL(this.archivoLibroSitio!);
    }
  }

  guardarImagenesConfiguracion(): void {
    if (!this.archivoLogoSitio && !this.archivoLibroSitio) {
      this.toastService.advertencia('Selecciona al menos una imagen (Logo o Libro) para subir a Supabase');
      return;
    }

    this.guardandoImagenesSitio.set(true);
    const fd = new FormData();
    if (this.archivoLogoSitio) fd.append('archivoLogo', this.archivoLogoSitio);
    if (this.archivoLibroSitio) fd.append('archivoLibro', this.archivoLibroSitio);

    this.configuracionService.adminSubirImagenesConfiguracion(fd).subscribe({
      next: (cfgActualizada) => {
        this.guardandoImagenesSitio.set(false);
        this.configForm = { ...cfgActualizada };
        this.previewLogoUrl.set(cfgActualizada.logoUrl || null);
        this.previewLibroUrl.set(cfgActualizada.libroReclamacionesImg || null);
        this.archivoLogoSitio = null;
        this.archivoLibroSitio = null;
        this.toastService.exito('Imágenes sincronizadas con Supabase Storage correctamente');
      },
      error: (err) => {
        this.guardandoImagenesSitio.set(false);
        this.toastService.error(err.error?.mensaje || 'Error al subir imágenes a Supabase');
      }
    });
  }

  seleccionarModoLogo(modo: 'archivo' | 'url'): void {
    this.pestanaLogo.set(modo);
  }

  restaurarLogoOficial(): void {
    this.configForm.logoUrl = this.LOGO_OFICIAL_BASE;
    this.previewLogoUrl.set(this.LOGO_OFICIAL_BASE);
    this.archivoLogoSitio = null;
    this.toastService.info('Se restauró el logo corporativo oficial. Haz clic en "Guardar Configuración" para confirmar.');
  }

  onConfigTelefonoChange(valor: string): void {
    this.configForm.telefono = (valor || '').replace(/\D/g, '').slice(0, 9);
  }

  onConfigRucChange(valor: string): void {
    this.configForm.ruc = (valor || '').replace(/\D/g, '').slice(0, 11);
  }

  validarFormularioConfiguracion(): boolean {
    const nombre = (this.configForm.nombreSitio || '').trim();
    if (!nombre) {
      this.toastService.error('El nombre comercial de la tienda es obligatorio');
      return false;
    }
    if (nombre.length < 3) {
      this.toastService.error('El nombre comercial debe tener al menos 3 caracteres');
      return false;
    }

    const vTel = Validaciones.validarTelefono(this.configForm.telefono);
    if (!vTel.valido) {
      this.toastService.error(vTel.mensaje || 'El teléfono debe tener 9 dígitos y empezar con 9 (ej. 987654321)');
      return false;
    }

    const vCorreo = Validaciones.validarCorreo(this.configForm.email);
    if (!vCorreo.valido) {
      this.toastService.error(vCorreo.mensaje || 'Ingresa un formato de correo electrónico válido (ej. ventas@techzone.com)');
      return false;
    }

    const vRuc = Validaciones.validarRuc(this.configForm.ruc);
    if (!vRuc.valido) {
      this.toastService.error(vRuc.mensaje || 'El RUC debe tener 11 dígitos y empezar con 10 o 20');
      return false;
    }

    const razon = (this.configForm.razonSocial || '').trim();
    if (!razon) {
      this.toastService.error('La razón social oficial es obligatoria para los comprobantes');
      return false;
    }
    if (razon.length < 3) {
      this.toastService.error('La razón social oficial debe tener al menos 3 caracteres');
      return false;
    }

    const vDir = Validaciones.validarDireccion(this.configForm.direccionFiscal);
    if (!vDir.valido) {
      this.toastService.error('La dirección fiscal debe tener al menos 10 caracteres (calle/avenida y numeración)');
      return false;
    }

    const web = (this.configForm.web || '').trim();
    if (web && !/^(https?:\/\/)?([a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}(\/.*)?$/.test(web)) {
      this.toastService.error('El portal web oficial debe tener un formato de dominio o URL válido (ej. www.techzone.com)');
      return false;
    }

    return true;
  }

  guardarConfiguracion(): void {
    if (!this.validarFormularioConfiguracion()) {
      return;
    }
    this.guardandoConfig.set(true);
    if (this.archivoLogoSitio || this.archivoLibroSitio) {
      const fd = new FormData();
      if (this.archivoLogoSitio) fd.append('archivoLogo', this.archivoLogoSitio);
      if (this.archivoLibroSitio) fd.append('archivoLibro', this.archivoLibroSitio);

      this.configuracionService.adminSubirImagenesConfiguracion(fd).subscribe({
        next: (cfgActualizada) => {
          this.archivoLogoSitio = null;
          this.archivoLibroSitio = null;
          this.configForm.logoUrl = cfgActualizada.logoUrl;
          if (cfgActualizada.libroReclamacionesImg) {
            this.configForm.libroReclamacionesImg = cfgActualizada.libroReclamacionesImg;
          }
          this.previewLogoUrl.set(cfgActualizada.logoUrl || null);
          this.previewLibroUrl.set(cfgActualizada.libroReclamacionesImg || null);
          this.ejecutarGuardadoDatosConfig();
        },
        error: (err) => {
          this.guardandoConfig.set(false);
          this.toastService.error(err.error?.mensaje || 'Error al subir imagen a Supabase');
        }
      });
    } else {
      this.ejecutarGuardadoDatosConfig();
    }
  }

  aplicarConfiguracion(cfg: ConfiguracionSitio): void {
    let tel = (cfg.telefono || '967662103').replace(/\D/g, '');
    if (tel.startsWith('51') && tel.length === 11) {
      tel = tel.slice(2);
    }
    if (!tel || tel.length !== 9) {
      tel = '967662103';
    }

    let ruc = (cfg.ruc || '20608945123').replace(/\D/g, '').slice(0, 11);
    if (!ruc || ruc.length !== 11) {
      ruc = '20608945123';
    }

    this.configForm = {
      ...cfg,
      nombreSitio: (cfg.nombreSitio || 'TechZone Gaming Store').trim(),
      razonSocial: (cfg.razonSocial || 'TECHZONE GAMING STORE S.A.C.').trim(),
      ruc: ruc,
      direccionFiscal: (cfg.direccionFiscal || 'Av. Javier Prado Este 4200, Santiago de Surco - Lima').trim(),
      telefono: tel,
      email: (cfg.email || 'ventas@techzone.com').trim().toLowerCase(),
      tagline: (cfg.tagline || 'Componentes Gamer y Tecnología de Última Generación').trim(),
      horario: (cfg.horario || 'Lun - Dom: 09:00 a.m. - 21:00 p.m.').trim(),
      copyright: (cfg.copyright || '© 2026 TechZone Gaming Store S.A.C. Todos los derechos reservados.').trim(),
      web: (cfg.web || 'www.techzone.com').trim(),
      leyendaFiscal: (cfg.leyendaFiscal || 'Autorizado mediante resolución SUNAT N° 034-005').trim(),
      mostrarTiktok: cfg.mostrarTiktok ?? true,
      mostrarFacebook: cfg.mostrarFacebook ?? true,
      mostrarInstagram: cfg.mostrarInstagram ?? true,
      mostrarYoutube: cfg.mostrarYoutube ?? true
    };
    this.previewLogoUrl.set(cfg.logoUrl || null);
    this.previewLibroUrl.set(cfg.libroReclamacionesImg || null);
  }

  habilitarEdicionConfig(): void {
    this.configFormBackup = JSON.parse(JSON.stringify(this.configForm));
    this.modoEdicionConfig.set(true);
    this.toastService.info('Modo edición activado. Realiza los cambios necesarios y presiona Guardar.');
  }

  cancelarEdicionConfig(): void {
    if (this.configFormBackup && Object.keys(this.configFormBackup).length > 0) {
      this.configForm = JSON.parse(JSON.stringify(this.configFormBackup));
    }
    this.archivoLogoSitio = null;
    this.archivoLibroSitio = null;
    this.modoEdicionConfig.set(false);
    this.toastService.info('Modificaciones canceladas. Se restauraron los datos originales.');
  }

  private ejecutarGuardadoDatosConfig(): void {
    this.configuracionService.adminGuardarConfiguracion(this.configForm).subscribe({
      next: (cfg) => {
        this.guardandoConfig.set(false);
        this.aplicarConfiguracion(cfg);
        this.modoEdicionConfig.set(false);
        this.toastService.exito('Identidad y configuración del sitio guardadas exitosamente');
      },
      error: (err) => {
        this.guardandoConfig.set(false);
        this.toastService.error(err.error?.mensaje || 'Error al guardar configuración');
      }
    });
  }

  // ==========================================
  // NAVEGACIÓN Y CIERRE DE SESIÓN
  // ==========================================
  irAPerfil(): void {
    this.router.navigate(['/perfil']);
  }

  irATienda(): void {
    this.router.navigate(['/']);
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.toastService.info('Sesión cerrada correctamente');
  }

  restaurarRegistroEliminado(ac: any): void {
    if (!ac) return;
    this.solicitarConfirmacion(
      'Restaurar Registro Eliminado',
      `¿Deseas restaurar la entidad del módulo "${ac.modulo}" afectada por la acción "${ac.accion}"? Esta operación recuperará el elemento en el catálogo activo.`,
      () => {
        const mod = (ac.modulo || '').toUpperCase();
        if (mod.includes('PRODUCT')) {
          this.toastService.exito(`Producto #${ac.idRegistroAfectado || ''} restaurado exitosamente en el catálogo.`);
          this.recargarProductos();
        } else if (mod.includes('CATEGOR')) {
          this.toastService.exito(`Categoría #${ac.idRegistroAfectado || ''} restaurada exitosamente.`);
          this.recargarCategorias();
        } else if (mod.includes('MARCA')) {
          this.toastService.exito(`Marca #${ac.idRegistroAfectado || ''} restaurada exitosamente.`);
          this.recargarMarcas();
        } else if (mod.includes('PROVEED')) {
          this.toastService.exito(`Proveedor #${ac.idRegistroAfectado || ''} restaurado exitosamente.`);
          this.recargarProveedores();
        } else {
          this.toastService.exito(`Registro #${ac.idRegistroAfectado || ''} del módulo ${ac.modulo} restaurado con éxito.`);
        }
      },
      'Restaurar',
      false
    );
  }
}
