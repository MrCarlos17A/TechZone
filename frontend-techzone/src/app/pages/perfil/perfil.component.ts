import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PerfilService, PerfilData, ActualizarPerfilDto } from '../../services/perfil.service';
import { AuthService } from '../../services/auth.service';
import { ConfiguracionService } from '../../services/configuracion.service';
import { ToastService } from '../../services/toast.service';
import { Ubigeo, TipoDocumento } from '../../models/configuracion.models';
import { Validaciones } from '../../utils/validaciones';

type TabPerfil = 'datos' | 'ubicacion' | 'seguridad';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './perfil.component.html'
})
export class PerfilComponent implements OnInit {
  private readonly perfilService = inject(PerfilService);
  readonly authService = inject(AuthService);
  private readonly configService = inject(ConfiguracionService);
  private readonly toastService = inject(ToastService);
  readonly router = inject(Router);

  tabActiva = signal<TabPerfil>('datos');
  modoEdicion = signal(false);
  cargando = signal(true);
  guardando = signal(false);

  // Tipos de Documento oficiales
  tiposDoc = signal<TipoDocumento[]>([]);

  // Datos de Perfil
  perfil = signal<PerfilData>({});

  // Formulario editable
  formNombres = signal('');
  formApellidoPaterno = signal('');
  formApellidoMaterno = signal('');
  formTelefono = signal('');
  formTipoDoc = signal<number>(1);
  formNumeroDoc = signal('');
  formFechaNacimiento = signal('');

  formDireccion = signal('');
  formReferencia = signal('');
  formIdUbigeo = signal<number | null>(null);

  // Cascadas de Ubigeo
  ubigeosCompletos: Ubigeo[] = [];
  departamentos = signal<string[]>([]);
  provincias = signal<string[]>([]);
  distritos = signal<Ubigeo[]>([]);

  deptoSel = signal<string>('');
  provSel = signal<string>('');

  // Seguridad
  passwordActual = signal('');
  passwordNuevo = signal('');
  mostrarPassActual = signal(false);
  mostrarPassNuevo = signal(false);
  readonly criteriosPasswordNuevo = computed(() => Validaciones.evaluarCriteriosContrasena(this.passwordNuevo()));

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.toastService.advertencia('Debes iniciar sesión para ver tu perfil');
      this.router.navigate(['/login']);
      return;
    }
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    // Carga inmediata del perfil sin bloquear la pantalla
    this.cargarPerfil();

    // Cargar tipos de documento oficiales
    this.configService.getTiposDocumento().subscribe({
      next: (docs) => this.tiposDoc.set(docs),
      error: () => {}
    });

    // Cargar solo la lista ligera de departamentos (~25 ítems)
    this.configService.getDepartamentos().subscribe({
      next: (deps: string[]) => {
        this.departamentos.set(deps);
      },
      error: () => {}
    });
  }

  cargarPerfil(): void {
    this.perfilService.obtenerPerfil().subscribe({
      next: (p) => {
        this.perfil.set(p);
        this.formNombres.set(p.nombres || '');
        this.formApellidoPaterno.set(p.apellidoPaterno || '');
        this.formApellidoMaterno.set(p.apellidoMaterno || '');
        this.formTelefono.set(p.telefono || '');
        this.formTipoDoc.set(p.idTipoDocumento || 1);
        this.formNumeroDoc.set(p.numeroDocumento || '');
        this.formFechaNacimiento.set(p.fechaNacimiento || '');

        this.formDireccion.set(p.direccion || '');
        const refVal = (p.referencia || '').trim();
        const esEjemplo = refVal.toUpperCase().includes('FRENTE AL PARQUE') || refVal.toUpperCase().startsWith('EJ.');
        this.formReferencia.set(esEjemplo ? '' : refVal);
        this.formIdUbigeo.set(p.idUbigeo || null);

        if (p.departamento) {
          this.deptoSel.set(p.departamento);
          this.onDepartamentoChange(p.departamento, p.provincia, p.idUbigeo);
        }

        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toastService.error(err.error?.mensaje || 'No se pudo cargar el perfil');
      }
    });
  }

  onDepartamentoChange(depto: string, provSeleccionar?: string, idUbiSeleccionar?: number): void {
    this.deptoSel.set(depto);
    if (!depto) {
      this.provincias.set([]);
      this.distritos.set([]);
      this.provSel.set('');
      this.formIdUbigeo.set(null);
      return;
    }

    this.configService.getProvincias(depto).subscribe({
      next: (provs) => {
        this.provincias.set(provs);
        const targetProv = provSeleccionar || (provs.length > 0 ? provs[0] : '');
        if (targetProv) {
          this.onProvinciaChange(targetProv, idUbiSeleccionar);
        }
      }
    });
  }

  onProvinciaChange(prov: string, idUbiSeleccionar?: number): void {
    this.provSel.set(prov);
    const depto = this.deptoSel();
    if (!prov || !depto) {
      this.distritos.set([]);
      this.formIdUbigeo.set(null);
      return;
    }

    this.configService.getDistritos(depto, prov).subscribe({
      next: (dists) => {
        this.distritos.set(dists);
        if (idUbiSeleccionar) {
          this.formIdUbigeo.set(idUbiSeleccionar);
        } else if (dists.length > 0) {
          this.formIdUbigeo.set(dists[0].idUbigeo);
        }
      }
    });
  }

  activarEdicion(): void {
    this.modoEdicion.set(true);
  }

  cancelarEdicion(): void {
    this.modoEdicion.set(false);
    this.cargarPerfil();
    this.passwordActual.set('');
    this.passwordNuevo.set('');
  }

  guardarCambios(): void {
    // Validaciones
    if (!this.formNombres().trim() || !this.formApellidoPaterno().trim() || !this.formApellidoMaterno().trim()) {
      this.toastService.advertencia('Nombres, Apellido Paterno y Apellido Materno son obligatorios');
      return;
    }

    if (!this.formNumeroDoc().trim()) {
      this.toastService.advertencia('El número de documento (DNI) es obligatorio');
      return;
    }

    if (this.formTelefono().trim()) {
      if (!/^9\d{8}$/.test(this.formTelefono().trim())) {
        this.toastService.advertencia('El teléfono debe tener 9 dígitos y empezar con 9');
        return;
      }
    }

    if (!this.formDireccion().trim()) {
      this.toastService.advertencia('La dirección exacta es obligatoria');
      return;
    }
    if (this.formDireccion().trim().length < 10) {
      this.toastService.advertencia('La dirección debe tener al menos 10 caracteres');
      return;
    }

    if (this.passwordNuevo().trim()) {
      const vPass = Validaciones.validarContrasena(this.passwordNuevo().trim());
      if (!vPass.valido) {
        this.toastService.advertencia(vPass.mensaje || 'La nueva contraseña no cumple los requisitos de seguridad');
        return;
      }
      if (!this.passwordActual().trim()) {
        this.toastService.advertencia('Ingresa tu contraseña actual para confirmar el cambio');
        return;
      }
    }

    this.guardando.set(true);

    const dto: ActualizarPerfilDto = {
      nombres: this.formNombres().trim().toUpperCase(),
      apellidoPaterno: this.formApellidoPaterno().trim().toUpperCase(),
      apellidoMaterno: this.formApellidoMaterno().trim().toUpperCase(),
      telefono: this.formTelefono().trim(),
      idTipoDocumento: this.formTipoDoc(),
      numeroDocumento: this.formNumeroDoc().trim(),
      direccion: this.formDireccion().trim().toUpperCase(),
      referencia: this.formReferencia().trim().toUpperCase(),
      idUbigeo: this.formIdUbigeo() || undefined,
      fechaNacimiento: this.formFechaNacimiento().trim() || undefined,
      passwordActual: this.passwordActual().trim() || undefined,
      passwordNuevo: this.passwordNuevo().trim() || undefined
    };

    this.perfilService.actualizarPerfil(dto).subscribe({
      next: () => {
        this.guardando.set(false);
        this.modoEdicion.set(false);
        this.passwordActual.set('');
        this.passwordNuevo.set('');
        const u = this.authService.currentUser();
        if (u) {
          const nuevoNombre = `${this.formNombres().trim()} ${this.formApellidoPaterno().trim()}`;
          const actualizado = { ...u, nombre: nuevoNombre };
          if (typeof window !== 'undefined') {
            localStorage.setItem('techzone_user', JSON.stringify(actualizado));
          }
          this.authService.currentUser.set(actualizado);
        }
        this.toastService.exito('Tus datos han sido actualizados con éxito');
        this.cargarPerfil();
      },
      error: (err) => {
        this.guardando.set(false);
        this.toastService.error(err.error?.mensaje || 'Error al guardar los cambios');
      }
    });
  }
}
