import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ConfiguracionService } from '../../services/configuracion.service';
import { TipoDocumento, Ubigeo } from '../../models/configuracion.models';
import { Validaciones } from '../../utils/validaciones';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './registro.component.html'
})
export class RegistroComponent implements OnInit {
  readonly authService = inject(AuthService);
  readonly configuracionService = inject(ConfiguracionService);
  private readonly router = inject(Router);

  tiposDoc = signal<TipoDocumento[]>([]);
  idTipoDoc = signal<number | undefined>(undefined);
  numDoc = signal('');
  nombres = signal('');
  apellidoPaterno = signal('');
  apellidoMaterno = signal('');
  correo = signal('');
  telefono = signal('');
  direccion = signal('');
  referencia = signal('');
  password = signal('');
  confirmPassword = signal('');
  mostrarPassword = signal(false);
  mostrarConfirmPassword = signal(false);
  readonly criteriosPassword = computed(() => Validaciones.evaluarCriteriosContrasena(this.password()));

  departamentos = signal<string[]>([]);
  provincias = signal<string[]>([]);
  distritos = signal<Ubigeo[]>([]);
  departamentoSel = signal<string>('');
  provinciaSel = signal<string>('');
  idUbigeoSel = signal<number | undefined>(undefined);

  consultandoDni = signal(false);
  cargando = signal(false);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.configuracionService.getTiposDocumento().subscribe({
      next: (docs) => {
        this.tiposDoc.set(docs);
        const dni = docs.find(d => d.nombre.toUpperCase().includes('DNI'));
        if (dni) {
          this.idTipoDoc.set(dni.idTipoDocumento);
        }
      }
    });

    this.configuracionService.getDepartamentos().subscribe({
      next: (deps) => this.departamentos.set(deps)
    });
  }

  onNumDocChange(valor: string): void {
    const limpio = valor.replace(/\D/g, '').slice(0, 8);
    this.numDoc.set(limpio);
  }

  onTelefonoChange(valor: string): void {
    const limpio = valor.replace(/\D/g, '').slice(0, 9);
    this.telefono.set(limpio);
  }

  consultarDni(): void {
    const num = this.numDoc().trim();
    const vDni = Validaciones.validarDni(num);
    if (!vDni.valido) {
      this.error.set(vDni.mensaje || 'Ingresa un DNI de 8 dígitos válido');
      return;
    }

    this.consultandoDni.set(true);
    this.error.set(null);

    this.configuracionService.consultarDocumento(this.idTipoDoc(), num).subscribe({
      next: (resp) => {
        this.consultandoDni.set(false);
        if (resp.ok && resp.datos) {
          this.nombres.set(resp.datos.nombres || '');
          this.apellidoPaterno.set(resp.datos.apellidoPaterno || '');
          this.apellidoMaterno.set(resp.datos.apellidoMaterno || '');
          if (resp.datos.telefono) this.telefono.set(resp.datos.telefono.replace(/\D/g, '').slice(0, 9));
          if (resp.datos.direccion) this.direccion.set(resp.datos.direccion);
          if (resp.datos.referencia) this.referencia.set(resp.datos.referencia);
        } else {
          this.error.set(resp.mensaje || 'No se encontraron datos automáticos. Puedes ingresarlos manualmente.');
        }
      },
      error: () => {
        this.consultandoDni.set(false);
        this.error.set('No se pudo consultar el servicio de DNI. Completa tus nombres manualmente.');
      }
    });
  }

  cambioDepartamento(dep: string): void {
    this.departamentoSel.set(dep);
    this.provinciaSel.set('');
    this.idUbigeoSel.set(undefined);
    this.distritos.set([]);
    if (dep) {
      this.configuracionService.getProvincias(dep).subscribe(provs => this.provincias.set(provs));
    } else {
      this.provincias.set([]);
    }
  }

  cambioProvincia(prov: string): void {
    this.provinciaSel.set(prov);
    this.idUbigeoSel.set(undefined);
    if (prov && this.departamentoSel()) {
      this.configuracionService.getDistritos(this.departamentoSel(), prov).subscribe(dists => this.distritos.set(dists));
    } else {
      this.distritos.set([]);
    }
  }

  onSubmit(): void {
    // 1. Validar DNI (Obligatorio)
    if (!this.numDoc().trim()) {
      this.error.set('El número de documento (DNI) es obligatorio');
      return;
    }
    const vDni = Validaciones.validarDni(this.numDoc());
    if (!vDni.valido) {
      this.error.set(vDni.mensaje || 'DNI inválido');
      return;
    }

    // 2. Validar Nombres y Apellidos
    const vNombres = Validaciones.validarTextoNombre(this.nombres(), 'Los nombres');
    if (!vNombres.valido) {
      this.error.set(vNombres.mensaje || 'Nombres inválidos');
      return;
    }

    const vApPat = Validaciones.validarTextoNombre(this.apellidoPaterno(), 'El apellido paterno');
    if (!vApPat.valido) {
      this.error.set(vApPat.mensaje || 'Apellido paterno inválido');
      return;
    }

    const vApMat = Validaciones.validarTextoNombre(this.apellidoMaterno(), 'El apellido materno');
    if (!vApMat.valido) {
      this.error.set(vApMat.mensaje || 'El apellido materno es obligatorio');
      return;
    }

    // 3. Validar Correo
    const vCorreo = Validaciones.validarCorreo(this.correo());
    if (!vCorreo.valido) {
      this.error.set(vCorreo.mensaje || 'Correo inválido');
      return;
    }

    // 4. Validar Teléfono (9 dígitos empezando en 9)
    const vTel = Validaciones.validarTelefono(this.telefono());
    if (!vTel.valido) {
      this.error.set(vTel.mensaje || 'Teléfono inválido');
      return;
    }

    // 5. Validar Dirección (Obligatoria, mínimo 10 caracteres)
    const vDir = Validaciones.validarDireccion(this.direccion());
    if (!vDir.valido) {
      this.error.set(vDir.mensaje || 'La dirección exacta es obligatoria');
      return;
    }

    // 6. Validar Contraseña
    const vPass = Validaciones.validarContrasena(this.password(), this.confirmPassword());
    if (!vPass.valido) {
      this.error.set(vPass.mensaje || 'Contraseña inválida');
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.authService.registro({
      nombres: this.nombres().trim().toUpperCase(),
      apellidos: this.apellidoPaterno().trim().toUpperCase(),
      apellidoMaterno: this.apellidoMaterno().trim().toUpperCase() || undefined,
      correo: this.correo().trim().toLowerCase(),
      telefono: this.telefono().trim(),
      password: this.password(),
      idTipoDocumento: this.idTipoDoc(),
      numeroDocumento: this.numDoc().trim() || undefined,
      direccion: this.direccion().trim().toUpperCase() || undefined,
      referencia: this.referencia().trim().toUpperCase() || undefined,
      idUbigeo: this.idUbigeoSel()
    }).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err.error?.mensaje || 'Error al registrar la cuenta');
      }
    });
  }
}
