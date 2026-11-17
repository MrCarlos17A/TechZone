/**
 * Reglas de validación unificadas para TechZone (Frontend)
 * Estas reglas coinciden exactamente con las restricciones de la base de datos
 * y las validaciones del Backend.
 */

export interface ResultadoValidacion {
  valido: boolean;
  mensaje?: string;
}

export class Validaciones {
  /**
   * DNI peruano: exactamente 8 dígitos numéricos.
   */
  static validarDni(dni?: string | null): ResultadoValidacion {
    const valor = (dni || '').trim();
    if (!valor) {
      return { valido: false, mensaje: 'El número de DNI es obligatorio' };
    }
    if (!/^\d{8}$/.test(valor)) {
      return { valido: false, mensaje: 'El DNI debe tener exactamente 8 dígitos numéricos' };
    }
    return { valido: true };
  }

  /**
   * Teléfono celular de Perú: exactamente 9 dígitos numéricos empezando con 9.
   */
  static validarTelefono(telefono?: string | null): ResultadoValidacion {
    const valor = (telefono || '').trim();
    if (!valor) {
      return { valido: false, mensaje: 'El número de teléfono móvil es obligatorio' };
    }
    if (!/^9\d{8}$/.test(valor)) {
      return { valido: false, mensaje: 'El teléfono debe tener exactamente 9 dígitos y empezar con 9' };
    }
    return { valido: true };
  }

  /**
   * Correo electrónico: formato estándar RFC / web.
   */
  static validarCorreo(correo?: string | null): ResultadoValidacion {
    const valor = (correo || '').trim().toLowerCase();
    if (!valor) {
      return { valido: false, mensaje: 'El correo electrónico es obligatorio' };
    }
    const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!regex.test(valor)) {
      return { valido: false, mensaje: 'Ingresa un formato de correo electrónico válido' };
    }
    return { valido: true };
  }

  /**
   * Nombres o Apellidos: mínimo 2 caracteres, solo letras y espacios.
   */
  static validarTextoNombre(valor?: string | null, etiqueta: string = 'Este campo'): ResultadoValidacion {
    const val = (valor || '').trim();
    if (!val) {
      return { valido: false, mensaje: `${etiqueta} es obligatorio` };
    }
    if (val.length < 2) {
      return { valido: false, mensaje: `${etiqueta} debe tener al menos 2 caracteres` };
    }
    if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s'-]+$/.test(val)) {
      return { valido: false, mensaje: `${etiqueta} solo debe contener letras` };
    }
    return { valido: true };
  }

  /**
   * Texto general obligatorio con longitud mínima.
   */
  static validarTextoObligatorio(valor?: string | null, etiqueta: string = 'Este campo', minLen: number = 2): ResultadoValidacion {
    const val = (valor || '').trim();
    if (!val) {
      return { valido: false, mensaje: `${etiqueta} es obligatorio` };
    }
    if (val.length < minLen) {
      return { valido: false, mensaje: `${etiqueta} debe tener al menos ${minLen} caracteres` };
    }
    return { valido: true };
  }

  /**
   * Dirección: mínimo 5 caracteres.
   */
  static validarDireccion(direccion?: string | null): ResultadoValidacion {
    const valor = (direccion || '').trim();
    if (!valor) {
      return { valido: false, mensaje: 'La dirección exacta es obligatoria' };
    }
    if (valor.length < 5) {
      return { valido: false, mensaje: 'La dirección debe tener al menos 5 caracteres' };
    }
    return { valido: true };
  }

  /**
   * RUC peruano: exactamente 11 dígitos numéricos empezando con 10 o 20.
   */
  static validarRuc(ruc?: string | null): ResultadoValidacion {
    const valor = (ruc || '').trim();
    if (!valor) {
      return { valido: false, mensaje: 'El número de RUC es obligatorio' };
    }
    if (!/^(10|20)\d{9}$/.test(valor)) {
      return { valido: false, mensaje: 'El RUC debe tener 11 dígitos y empezar con 10 o 20' };
    }
    return { valido: true };
  }

  /**
   * Contraseña Segura: mínimo 8 caracteres, al menos 1 mayúscula, 1 número y 1 símbolo.
   */
  static validarContrasena(contrasena?: string | null, confirmacion?: string | null): ResultadoValidacion {
    const pass = contrasena || '';
    if (!pass) {
      return { valido: false, mensaje: 'La contraseña es obligatoria' };
    }
    if (pass.length < 8) {
      return { valido: false, mensaje: 'La contraseña debe tener al menos 8 caracteres' };
    }
    if (!/[A-Z]/.test(pass)) {
      return { valido: false, mensaje: 'La contraseña debe incluir al menos una letra mayúscula (A-Z)' };
    }
    if (!/[0-9]/.test(pass)) {
      return { valido: false, mensaje: 'La contraseña debe incluir al menos un número (0-9)' };
    }
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]/.test(pass)) {
      return { valido: false, mensaje: 'La contraseña debe incluir al menos un signo o carácter especial (ej. ! @ # $ % * -)' };
    }
    if (confirmacion !== undefined && pass !== confirmacion) {
      return { valido: false, mensaje: 'Las contraseñas no coinciden' };
    }
    return { valido: true };
  }

  /**
   * Criterios de fortaleza de contraseña para feedback visual
   */
  static evaluarCriteriosContrasena(pass: string = '') {
    return {
      longitud: pass.length >= 8,
      mayuscula: /[A-Z]/.test(pass),
      numero: /[0-9]/.test(pass),
      simbolo: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]/.test(pass)
    };
  }

  /**
   * Ubigeo: departamento, provincia y distrito seleccionados.
   */
  static validarUbigeo(idUbigeo?: number | null): ResultadoValidacion {
    if (!idUbigeo || idUbigeo <= 0) {
      return { valido: false, mensaje: 'Debes seleccionar el departamento, provincia y distrito' };
    }
    return { valido: true };
  }

  /**
   * Número positivo (> 0).
   */
  static validarNumeroPositivo(valor?: number | null, etiqueta: string = 'El valor'): ResultadoValidacion {
    if (valor === null || valor === undefined || isNaN(valor) || valor <= 0) {
      return { valido: false, mensaje: `${etiqueta} debe ser un número mayor a 0` };
    }
    return { valido: true };
  }

  /**
   * Entero no negativo (>= 0).
   */
  static validarEnteroNoNegativo(valor?: number | null, etiqueta: string = 'El valor'): ResultadoValidacion {
    if (valor === null || valor === undefined || isNaN(valor) || valor < 0 || !Number.isInteger(valor)) {
      return { valido: false, mensaje: `${etiqueta} debe ser un número entero mayor o igual a 0` };
    }
    return { valido: true };
  }

  /**
   * Fecha válida obligatoria.
   */
  static validarFechaObligatoria(fechaStr?: string | null, etiqueta: string = 'La fecha'): ResultadoValidacion {
    const val = (fechaStr || '').trim();
    if (!val) {
      return { valido: false, mensaje: `${etiqueta} es obligatoria` };
    }
    const d = new Date(val);
    if (isNaN(d.getTime())) {
      return { valido: false, mensaje: `${etiqueta} no tiene un formato válido` };
    }
    return { valido: true };
  }

  /**
   * Mayor de edad (>= 18 años).
   */
  static validarMayorDeEdad(fechaNacStr?: string | null): ResultadoValidacion {
    const valFecha = this.validarFechaObligatoria(fechaNacStr, 'La fecha de nacimiento');
    if (!valFecha.valido) return valFecha;

    const nac = new Date(fechaNacStr!.trim());
    const hoy = new Date();
    let edad = hoy.getFullYear() - nac.getFullYear();
    const m = hoy.getMonth() - nac.getMonth();
    if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) {
      edad--;
    }
    if (edad < 18) {
      return { valido: false, mensaje: 'El colaborador debe tener al menos 18 años de edad' };
    }
    if (edad > 90) {
      return { valido: false, mensaje: 'Ingresa una fecha de nacimiento válida' };
    }
    return { valido: true };
  }
}
