(function () {
    'use strict';

    let pasoActual = 1;
    let listaUbigeos = [];

    function $(id) { return document.getElementById(id); }

    function claveCarrito() {
        const drawer = document.getElementById('drawerCarrito');
        const usuario = drawer && drawer.dataset.usuario ? drawer.dataset.usuario : '';
        return 'techzone-carrito-' + usuario;
    }

    function obtenerCarrito() {
        try {
            const datos = localStorage.getItem(claveCarrito());
            return datos ? JSON.parse(datos) : [];
        } catch (e) { return []; }
    }

    function obtenerCsrf() {
        const t = document.querySelector('meta[name="_csrf"]');
        const h = document.querySelector('meta[name="_csrf_header"]');
        return (t && h) ? { [h.content]: t.content } : {};
    }

    function alerta(mensaje, tipo) {
        const caja = $('alertaCheckout');
        if (!caja) return;
        caja.innerHTML = '<div class="alert alert-' + (tipo || 'danger') + ' alert-dismissible fade show" role="alert">'
            + mensaje
            + '<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>';
    }

    function limpiarAlerta() {
        const caja = $('alertaCheckout');
        if (caja) caja.innerHTML = '';
    }

    // ── PASOS ──

    function mostrarPaso(n) {
        for (let i = 1; i <= 4; i++) {
            const p = $('pasoCheckout' + i);
            if (p) p.style.display = (i === n) ? 'block' : 'none';
        }
        document.querySelectorAll('.checkout-paso').forEach(el => {
            const ind = parseInt(el.getAttribute('data-ind'), 10);
            el.classList.remove('activo', 'completado');
            if (ind === n) el.classList.add('activo');
            else if (ind < n) el.classList.add('completado');
        });
        pasoActual = n;
        $('btnCheckoutAtras').style.display = (n === 1) ? 'none' : 'inline-block';
        $('btnCheckoutSiguiente').style.display = (n === 4) ? 'none' : 'inline-block';
        $('btnCheckoutConfirmar').style.display = (n === 4) ? 'inline-block' : 'none';
        limpiarAlerta();
        if (n === 4) renderizarResumen();
    }

    function validarPaso(n) {
        if (n === 1) {
            const dni = $('dniCheckout').value.trim();
            const nombres = $('nombresCheckout').value.trim();
            const apPat = $('apPaternoCheckout').value.trim();
            const apMat = $('apMaternoCheckout').value.trim();
            const tel = $('telefonoCheckout').value.trim();
            const correo = $('correoCheckout').value.trim();
            if (!/^\d{8}$/.test(dni)) { alerta('El DNI debe tener 8 digitos'); return false; }
            if (!nombres) { alerta('Los nombres son obligatorios'); return false; }
            if (!apPat) { alerta('El apellido paterno es obligatorio'); return false; }
            if (!apMat) { alerta('El apellido materno es obligatorio'); return false; }
            if (!/^9\d{8}$/.test(tel)) { alerta('El telefono debe tener 9 digitos (empieza con 9)'); return false; }
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) { alerta('El correo no es valido'); return false; }
            return true;
        }
        if (n === 2) {
            if (!$('departamentoCheckout').value) { alerta('Selecciona el departamento'); return false; }
            if (!$('provinciaCheckout').value) { alerta('Selecciona la provincia'); return false; }
            if (!$('distritoCheckout').value) { alerta('Selecciona el distrito'); return false; }
            const dir = $('direccionCheckout').value.trim();
            if (dir.length < 10) { alerta('La direccion debe tener al menos 10 caracteres'); return false; }
            return true;
        }
        if (n === 3) {
            const sel = document.querySelector('input[name="medioPagoCheckout"]:checked');
            if (!sel) { alerta('Selecciona un medio de pago'); return false; }
            const tipo = tipoDesdeNombre(sel.getAttribute('data-nombre'));
            if (tipo === 'yape' || tipo === 'plin') {
                const cel = $('celularPago').value.trim();
                const cod = $('codOpPago').value.trim();
                if (!/^9\d{8}$/.test(cel)) { alerta('El numero de Yape/Plin debe tener 9 digitos (empieza con 9)'); return false; }
                if (!/^\d{6}$/.test(cod)) { alerta('El codigo de confirmacion debe tener 6 digitos numericos'); return false; }
            }
            if (tipo === 'tarjeta') {
                const num = $('numTarjeta').value.replace(/\s/g, '');
                const vence = $('fechaVence').value.trim();
                const cvv = $('cvvTarjeta').value.trim();
                const titular = $('titularTarjeta').value.trim();
                if (!/^\d{16}$/.test(num)) { alerta('El numero de tarjeta debe tener 16 digitos'); return false; }
                if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(vence)) { alerta('La fecha de vence debe tener formato MM/AA'); return false; }
                const partes = vence.split('/');
                const mes = parseInt(partes[0], 10);
                const anio = 2000 + parseInt(partes[1], 10);
                const finMes = new Date(anio, mes, 0);
                if (finMes < new Date()) { alerta('La tarjeta esta vencida'); return false; }
                if (!/^\d{3}$/.test(cvv)) { alerta('El CVV debe tener 3 digitos'); return false; }
                if (titular.length < 5) { alerta('El nombre del titular es invalido'); return false; }
            }
            if (tipo === 'transferencia') {
                const cod = $('codOpTransferencia').value.trim();
                const banco = $('bancoOrigen').value;
                if (cod.length < 8) { alerta('El codigo de operacion debe tener al menos 8 caracteres'); return false; }
                if (!banco) { alerta('Selecciona el banco de origen'); return false; }
            }
            if (tipo === 'pagoefectivo') {
                const ppi = $('codigoPPI').value.trim();
                if (!/^[A-Za-z0-9]{8,12}$/.test(ppi)) { alerta('El codigo PPI debe tener 8-12 caracteres alfanumericos'); return false; }
            }
            return true;
        }
        return true;
    }

    // ── MEDIOS DE PAGO ──

    function tipoDesdeNombre(nombre) {
        if (!nombre) return 'efectivo';
        const n = nombre.toLowerCase().trim();
        if (n.includes('yape')) return 'yape';
        if (n.includes('plin')) return 'plin';
        if (n.includes('tarjeta')) return 'tarjeta';
        if (n.includes('transferencia')) return 'transferencia';
        if (n.includes('pagoefectivo') || n.includes('pago efectivo')) return 'pagoefectivo';
        return 'efectivo';
    }

    function actualizarCamposPago() {
        const sel = document.querySelector('input[name="medioPagoCheckout"]:checked');
        if (!sel) return;
        const tipo = tipoDesdeNombre(sel.getAttribute('data-nombre'));
        $('grupoEfectivo').style.display = tipo === 'efectivo' ? 'block' : 'none';
        $('grupoYapePlin').style.display = (tipo === 'yape' || tipo === 'plin') ? 'block' : 'none';
        $('grupoTarjeta').style.display = tipo === 'tarjeta' ? 'block' : 'none';
        $('grupoTransferencia').style.display = tipo === 'transferencia' ? 'block' : 'none';
        $('grupoPagoEfectivo').style.display = tipo === 'pagoefectivo' ? 'block' : 'none';

        if (tipo === 'yape' || tipo === 'plin') {
            const lblYape = $('labelNumeroYape');
            const lblCod = $('labelCodConfirmacion');
            const inpCel = $('celularPago');
            const inpCod = $('codOpPago');
            if (lblYape) lblYape.innerHTML = 'Numero de Yape/Plin (9 digitos) <span class="text-danger">*</span>';
            if (lblCod) lblCod.innerHTML = 'Codigo de confirmacion (6 digitos) <span class="text-danger">*</span>';
            if (inpCel) inpCel.placeholder = 'Ej: 987654321';
            if (inpCod) inpCod.placeholder = 'Ej: 123456';
        }
    }

    // ── API DNI: la consulta ahora la hace /js/documento-lookup.js ──

    // ── DATOS GUARDADOS ──

    function cargarDatosGuardados() {
        fetch('/api/mis-datos')
            .then(r => r.json())
            .then(data => {
                if (!data.ok) return;
                if (data.dni) $('dniCheckout').value = data.dni;
                if (data.nombres) $('nombresCheckout').value = data.nombres;
                if (data.apellidoPaterno) $('apPaternoCheckout').value = data.apellidoPaterno;
                if (data.apellidoMaterno) $('apMaternoCheckout').value = data.apellidoMaterno;
                if (data.telefono) $('telefonoCheckout').value = data.telefono;
                if (data.correo) $('correoCheckout').value = data.correo;
                if (data.direccionExacta) $('direccionCheckout').value = data.direccionExacta;
                if (data.referencia) $('referenciaCheckout').value = data.referencia;

                if (data.idUbigeo) {
                    const idSel = String(data.idUbigeo);
                    const u = listaUbigeos.find(x => String(x.id) === idSel);
                    if (u) {
                        poblarDepartamentos();
                        $('departamentoCheckout').value = u.departamento;
                        poblarProvincias(u.departamento);
                        $('provinciaCheckout').value = u.provincia;
                        poblarDistritos(u.departamento, u.provincia);
                        $('distritoCheckout').value = u.distrito;
                    }
                }
            })
            .catch(() => { });
    }

    // ── UBIGEOS ──

    function cargarUbigeos() {
        if (listaUbigeos.length > 0) { poblarDepartamentos(); return; }
        fetch('/api/ubigeos')
            .then(r => r.json())
            .then(data => {
                listaUbigeos = data || [];
                poblarDepartamentos();
            })
            .catch(() => { });
    }

    function poblarDepartamentos() {
        const sel = $('departamentoCheckout');
        const departamentos = [...new Set(listaUbigeos.map(u => u.departamento))].sort();
        sel.innerHTML = '<option value="">Selecciona...</option>';
        departamentos.forEach(d => {
            sel.innerHTML += '<option value="' + d + '">' + d + '</option>';
        });
    }

    function poblarProvincias(departamento) {
        const sel = $('provinciaCheckout');
        sel.innerHTML = '<option value="">Selecciona...</option>';
        sel.disabled = true;
        $('distritoCheckout').innerHTML = '<option value="">Selecciona...</option>';
        $('distritoCheckout').disabled = true;
        if (!departamento) return;
        const provincias = [...new Set(
            listaUbigeos.filter(u => u.departamento === departamento).map(u => u.provincia)
        )].sort();
        provincias.forEach(p => {
            sel.innerHTML += '<option value="' + p + '">' + p + '</option>';
        });
        sel.disabled = false;
    }

    function poblarDistritos(departamento, provincia) {
        const sel = $('distritoCheckout');
        sel.innerHTML = '<option value="">Selecciona...</option>';
        sel.disabled = true;
        if (!departamento || !provincia) return;
        const distritos = listaUbigeos
            .filter(u => u.departamento === departamento && u.provincia === provincia)
            .sort((a, b) => a.distrito.localeCompare(b.distrito));
        distritos.forEach(d => {
            sel.innerHTML += '<option value="' + d.distrito + '" data-id="' + d.id + '">' + d.distrito + '</option>';
        });
        sel.disabled = false;
    }

    // ── RESUMEN ──

    function renderizarResumen() {
        const carrito = obtenerCarrito();
        let html = '<table class="table table-sm"><thead><tr><th>Producto</th><th class="text-center">Cant.</th><th class="text-end">Subtotal</th></tr></thead><tbody>';
        let total = 0;
        carrito.forEach(item => {
            const sub = parseFloat(item.precio) * parseInt(item.cantidad, 10);
            total += sub;
            html += '<tr><td>' + item.nombre + '</td><td class="text-center">' + item.cantidad + '</td><td class="text-end">S/ ' + sub.toFixed(2) + '</td></tr>';
        });
        const igv = total * 0.18;
        const totalFinal = total + igv;
        html += '</tbody></table>';
        html += '<div class="text-end">';
        html += '<p class="mb-1">Subtotal: <strong>S/ ' + total.toFixed(2) + '</strong></p>';
        html += '<p class="mb-1">IGV 18%: <strong>S/ ' + igv.toFixed(2) + '</strong></p>';
        html += '<p class="mb-0 fs-5">TOTAL: <strong class="text-primary">S/ ' + totalFinal.toFixed(2) + '</strong></p>';
        html += '</div>';
        $('resumenProductos').innerHTML = html;

        const dni = $('dniCheckout').value.trim();
        const nombres = $('nombresCheckout').value.trim();
        const apPat = $('apPaternoCheckout').value.trim();
        const apMat = $('apMaternoCheckout').value.trim();
        const tel = $('telefonoCheckout').value.trim();
        const correo = $('correoCheckout').value.trim();
        const depto = $('departamentoCheckout').value;
        const prov = $('provinciaCheckout').value;
        const dist = $('distritoCheckout').value;
        const dir = $('direccionCheckout').value.trim();
        const ref = $('referenciaCheckout').value.trim();

        const sel = document.querySelector('input[name="medioPagoCheckout"]:checked');
        const nombreMedio = sel ? (sel.getAttribute('data-nombre') || '') : '';
        const tipo = tipoDesdeNombre(nombreMedio);
        let detallePago = nombreMedio;
        if (tipo === 'yape' || tipo === 'plin') {
            const cod = $('codOpPago').value.trim();
            if (cod) detallePago += ' (Cod: ' + cod + ')';
        } else if (tipo === 'tarjeta') {
            const num = $('numTarjeta').value.replace(/\s/g, '');
            if (num.length >= 4) detallePago += ' ****' + num.slice(-4);
        } else if (tipo === 'transferencia') {
            const cod = $('codOpTransferencia').value.trim();
            const banco = $('bancoOrigen').value;
            if (cod) detallePago += ' (' + banco + ': ' + cod + ')';
        } else if (tipo === 'pagoefectivo') {
            const ppi = $('codigoPPI').value.trim();
            if (ppi) detallePago += ' (PPI: ' + ppi + ')';
        }

        let datosHtml = '<div class="row">';
        datosHtml += '<div class="col-md-6"><p class="mb-1"><i class="bi bi-person"></i> <strong>' + nombres + ' ' + apPat + ' ' + apMat + '</strong></p>';
        datosHtml += '<p class="mb-1 small text-muted">DNI: ' + dni + '</p>';
        datosHtml += '<p class="mb-1 small"><i class="bi bi-telephone"></i> ' + tel + '</p>';
        datosHtml += '<p class="mb-1 small"><i class="bi bi-envelope"></i> ' + correo + '</p></div>';
        datosHtml += '<div class="col-md-6"><p class="mb-1"><i class="bi bi-geo-alt"></i> <strong>' + depto + ', ' + prov + ', ' + dist + '</strong></p>';
        datosHtml += '<p class="mb-1 small">' + dir + '</p>';
        if (ref) datosHtml += '<p class="mb-1 small text-muted">Ref: ' + ref + '</p>';
        datosHtml += '<p class="mb-0 small"><i class="bi bi-credit-card"></i> ' + detallePago + '</p></div>';
        datosHtml += '</div>';
        $('resumenDatos').innerHTML = datosHtml;
    }

    // ── CONFIRMAR ──

    function confirmarCompra() {
        const btn = $('btnCheckoutConfirmar');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Procesando...';

        const carrito = obtenerCarrito();
        const items = carrito.map(item => ({
            idProducto: parseInt(item.id, 10),
            cantidad: parseInt(item.cantidad, 10)
        }));

        const sel = document.querySelector('input[name="medioPagoCheckout"]:checked');
        const idMedioPago = sel ? parseInt(sel.value, 10) : 1;
        const nombreMedio = sel ? (sel.getAttribute('data-nombre') || '') : '';
        const tipo = tipoDesdeNombre(nombreMedio);

        const distritoSel = $('distritoCheckout');
        const opcionDistrito = distritoSel.options[distritoSel.selectedIndex];
        const idUbigeo = opcionDistrito ? parseInt(opcionDistrito.getAttribute('data-id'), 10) : null;

        let pago = { idMedioPago: idMedioPago };
        if (tipo === 'yape' || tipo === 'plin') {
            pago.codigoOperacion = $('codOpPago').value.trim();
            pago.numeroCelular = $('celularPago').value.trim();
        } else if (tipo === 'tarjeta') {
            pago.numeroTarjeta = $('numTarjeta').value.replace(/\s/g, '');
            pago.fechaVence = $('fechaVence').value.trim();
            pago.cvv = $('cvvTarjeta').value.trim();
            pago.titular = $('titularTarjeta').value.trim();
            const num = pago.numeroTarjeta;
            pago.codigoOperacion = num.length >= 4 ? '****-****-****-' + num.slice(-4) : '';
        } else if (tipo === 'transferencia') {
            pago.codigoOperacion = $('codOpTransferencia').value.trim();
            pago.bancoOrigen = $('bancoOrigen').value;
        } else if (tipo === 'pagoefectivo') {
            pago.codigoPPI = $('codigoPPI').value.trim();
            pago.codigoOperacion = pago.codigoPPI;
        }

        const cuerpo = {
            items: items,
            datosPersonales: {
                dni: $('dniCheckout').value.trim(),
                nombres: $('nombresCheckout').value.trim(),
                apellidoPaterno: $('apPaternoCheckout').value.trim(),
                apellidoMaterno: $('apMaternoCheckout').value.trim(),
                telefono: $('telefonoCheckout').value.trim(),
                correo: $('correoCheckout').value.trim()
            },
            direccion: {
                idUbigeo: idUbigeo,
                direccionExacta: $('direccionCheckout').value.trim(),
                referencia: $('referenciaCheckout').value.trim()
            },
            pago: pago
        };

        const cabeceras = Object.assign({ 'Content-Type': 'application/json' }, obtenerCsrf());

        fetch('/checkout', {
            method: 'POST',
            headers: cabeceras,
            body: JSON.stringify(cuerpo)
        })
            .then(r => r.json())
            .then(data => {
                if (data.ok) {
                    localStorage.removeItem(claveCarrito());
                    const insignia = document.getElementById('insigniaCarrito');
                    if (insignia) insignia.style.display = 'none';
                    const contador = document.getElementById('contadorDrawer');
                    if (contador) contador.textContent = '0';
                    const itemsDrawer = document.getElementById('itemsDrawer');
                    if (itemsDrawer) itemsDrawer.innerHTML = '';
                    const pie = document.getElementById('pieDrawer');
                    if (pie) pie.style.display = 'none';
                    const vacio = document.getElementById('vacioDrawer');
                    if (vacio) vacio.style.display = 'block';

                    const modal = bootstrap.Modal.getInstance($('modalCheckout'));
                    if (modal) modal.hide();

                    window.location.href = '/mis-pedidos/' + data.idPedido;
                } else {
                    alerta(data.mensaje || 'No se pudo crear el pedido');
                    btn.disabled = false;
                    btn.innerHTML = '<i class="bi bi-bag-check"></i> Confirmar Pedido';
                }
            })
            .catch(() => {
                alerta('Error de conexion. Intentalo de nuevo.');
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-bag-check"></i> Confirmar Pedido';
            });
    }

    // ── ABRIR MODAL ──

    function abrirCheckout() {
        const carrito = obtenerCarrito();
        if (carrito.length === 0) return;

        const drawer = document.getElementById('drawerCarrito');
        if (drawer && typeof bootstrap !== 'undefined') {
            const inst = bootstrap.Offcanvas.getInstance(drawer);
            if (inst) inst.hide();
        }

        listaUbigeos = [];
        fetch('/api/ubigeos')
            .then(r => r.json())
            .then(data => {
                listaUbigeos = data || [];
                poblarDepartamentos();
                cargarDatosGuardados();
            })
            .catch(() => { });

        mostrarPaso(1);
        const modal = $('modalCheckout');
        if (modal && typeof bootstrap !== 'undefined') {
            bootstrap.Modal.getOrCreateInstance(modal).show();
        }
    }

    // ── EVENTOS ──

    window.techZoneAbrirCheckout = abrirCheckout;

    document.addEventListener('DOMContentLoaded', function () {
        // Consulta de documento (local primero, API despues) reutilizable
        if (typeof initDocumentoLookup === 'function') {
            initDocumentoLookup({
                tipoId: null,
                numeroId: 'dniCheckout',
                btnId: 'btnBuscarDni',
                msgId: 'mensajeDni',
                bloquearNombres: false,
                campos: {
                    nombres: 'nombresCheckout',
                    apellidoPaterno: 'apPaternoCheckout',
                    apellidoMaterno: 'apMaternoCheckout',
                    telefono: 'telefonoCheckout'
                }
            });
        }

        const btnSiguiente = $('btnCheckoutSiguiente');
        if (btnSiguiente) {
            btnSiguiente.addEventListener('click', function () {
                if (validarPaso(pasoActual)) {
                    mostrarPaso(Math.min(pasoActual + 1, 4));
                }
            });
        }

        const btnAtras = $('btnCheckoutAtras');
        if (btnAtras) {
            btnAtras.addEventListener('click', function () {
                mostrarPaso(Math.max(pasoActual - 1, 1));
            });
        }

        const btnConfirmar = $('btnCheckoutConfirmar');
        if (btnConfirmar) {
            btnConfirmar.addEventListener('click', confirmarCompra);
        }

        const selDepto = $('departamentoCheckout');
        if (selDepto) {
            selDepto.addEventListener('change', function () {
                poblarProvincias(this.value);
            });
        }

        const selProv = $('provinciaCheckout');
        if (selProv) {
            selProv.addEventListener('change', function () {
                poblarDistritos($('departamentoCheckout').value, this.value);
            });
        }

        document.querySelectorAll('input[name="medioPagoCheckout"]').forEach(radio => {
            radio.addEventListener('change', actualizarCamposPago);
        });

        [['celularPago', 9], ['codOpPago', 6]].forEach(function (par) {
            var inp = $(par[0]);
            if (inp) {
                inp.addEventListener('input', function () {
                    this.value = this.value.replace(/\D/g, '').slice(0, par[1]);
                });
            }
        });
    });
})();
