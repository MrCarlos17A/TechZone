(function () {
    'use strict';

    var LONGITUDES = { 'DNI': 8, 'RUC': 11 };

    function $(id) { return document.getElementById(id); }

    function longitudEsperada(nombreTipo) {
        if (!nombreTipo) return 8;
        var n = nombreTipo.toUpperCase();
        if (n.indexOf('RUC') >= 0) return 11;
        if (n.indexOf('DNI') >= 0) return 8;
        return 0;
    }

    function setLectura(entrada, soloLectura) {
        if (!entrada) return;
        entrada.readOnly = !!soloLectura;
        entrada.classList.toggle('bg-body-tertiary', !!soloLectura);
    }

    function initDocumentoLookup(config) {
        var tipo = $(config.tipoId);
        var numero = $(config.numeroId);
        var btn = config.btnId ? $(config.btnId) : null;
        var msg = config.msgId ? $(config.msgId) : null;
        var idPersona = config.idPersonaId ? $(config.idPersonaId) : null;
        var codigoUbi = config.codigoUbigeoId ? $(config.codigoUbigeoId) : null;
        var campos = config.campos || {};
        var bloquear = config.bloquearNombres !== false;

        if (!numero) return;

        var nombres = campos.nombres ? $(campos.nombres) : null;
        var apPaterno = campos.apellidoPaterno ? $(campos.apellidoPaterno) : null;
        var apMaterno = campos.apellidoMaterno ? $(campos.apellidoMaterno) : null;
        var editable = [nombres, apPaterno, apMaterno].filter(Boolean);

        function mensaje(texto, clase) {
            if (!msg) return;
            msg.textContent = texto || '';
            msg.className = clase || 'text-muted';
        }

        function limpiarBloqueo() {
            if (!bloquear) return;
            editable.forEach(function (el) {
                el.removeAttribute('readonly');
                el.classList.remove('bg-body-tertiary');
            });
        }

        function bloquearNombres() {
            if (!bloquear) return;
            editable.forEach(function (el) { setLectura(el, true); });
            // Al hacer clic sobre un nombre se vuelve editable
            editable.forEach(function (el) {
                el.addEventListener('click', function clic() {
                    setLectura(el, false);
                    el.removeEventListener('click', clic);
                });
            });
        }

        function aplicarUbigeo(idUbigeo) {
            if (!codigoUbi || !idUbigeo) return;
            codigoUbi.value = String(idUbigeo);
            codigoUbi.dispatchEvent(new Event('input', { bubbles: true }));
        }

        function llenar(datos) {
            if (!datos) return;
            if (nombres && datos.nombres) nombres.value = datos.nombres;
            if (apPaterno && datos.apellidoPaterno) apPaterno.value = datos.apellidoPaterno;
            if (apMaterno && datos.apellidoMaterno != null) apMaterno.value = datos.apellidoMaterno;
            if (campos.telefono && datos.telefono) { var t = $(campos.telefono); if (t && !t.value) t.value = datos.telefono; }
            if (campos.fechaNacimiento && datos.fechaNacimiento) { var f = $(campos.fechaNacimiento); if (f && !f.value) f.value = datos.fechaNacimiento; }
            if (campos.direccion && datos.direccion) { var d = $(campos.direccion); if (d && !d.value) d.value = datos.direccion; }
            if (campos.referencia && datos.referencia) { var r = $(campos.referencia); if (r && !r.value) r.value = datos.referencia; }
            aplicarUbigeo(datos.idUbigeo);
        }

        function consultar() {
            var valor = numero.value.trim();
            if (!valor) { mensaje('', 'text-muted'); return; }

            var nombreTipo = tipo && tipo.value !== ''
                ? tipo.options[tipo.selectedIndex].text
                : 'DNI';
            var longEsperada = longitudEsperada(nombreTipo);
            if (longEsperada && valor.length !== longEsperada) {
                mensaje('El documento debe tener ' + longEsperada + ' digitos', 'text-danger');
                return;
            }

            var original = btn ? btn.innerHTML : '';
            if (btn) { btn.disabled = true; btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>'; }
            mensaje('Consultando...', 'text-muted');

            fetch('/api/consulta-documento', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    tipoDocumento: tipo ? tipo.value : '',
                    numero: valor
                })
            })
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    limpiarBloqueo();
                    if (idPersona) idPersona.value = '';

                    if (data && data.ok && data.datos) {
                        llenar(data.datos);
                        if (idPersona && data.idPersona) idPersona.value = String(data.idPersona);

                        if (data.yaRegistrado) {
                            mensaje(data.mensaje || 'El documento ya esta registrado', 'text-warning fw-semibold');
                            if (typeof config.onYaRegistrado === 'function') {
                                config.onYaRegistrado(data);
                            }
                        } else if (data.origen === 'local') {
                            mensaje(data.mensaje || 'Datos encontrados en el sistema', 'text-success');
                        } else {
                            mensaje('Datos obtenidos del servicio de identidades', 'text-success');
                        }
                        bloquearNombres();
                    } else {
                        mensaje((data && data.mensaje) || 'Documento no encontrado', 'text-danger');
                    }
                })
                .catch(function () { mensaje('Error de conexion', 'text-danger'); })
                .finally(function () {
                    if (btn) { btn.disabled = false; btn.innerHTML = original; }
                });
        }

        function longitudActualValida() {
            if (!tipo || tipo.value === '') {
                return numero.value.trim().length === 8;
            }
            var longEsperada = longitudEsperada(tipo.options[tipo.selectedIndex].text);
            return longEsperada > 0 && numero.value.trim().length === longEsperada;
        }

        if (btn) btn.addEventListener('click', consultar);

        numero.addEventListener('input', function () {
            limpiarBloqueo();
            if (idPersona) idPersona.value = '';
            if (longitudActualValida()) consultar();
            else mensaje('', 'text-muted');
        });

        if (tipo) {
            tipo.addEventListener('change', function () {
                limpiarBloqueo();
                if (idPersona) idPersona.value = '';
                if (longitudActualValida()) consultar();
            });
        }
    }

    window.initDocumentoLookup = initDocumentoLookup;
})();
