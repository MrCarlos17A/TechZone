document.addEventListener('DOMContentLoaded', function () {
    function claveCarrito() {
        const drawer = document.getElementById('drawerCarrito');
        const usuario = drawer && drawer.dataset.usuario ? drawer.dataset.usuario : '';
        return 'techzone-carrito-' + usuario;
    }

    function obtenerCarrito() {
        try {
            const datos = localStorage.getItem(claveCarrito());
            return datos ? JSON.parse(datos) : [];
        } catch (e) {
            return [];
        }
    }

    function guardarCarrito(carrito) {
        localStorage.setItem(claveCarrito(), JSON.stringify(carrito));
        actualizarInsignia();
    }

    function actualizarInsignia() {
        const carrito = obtenerCarrito();
        const totalItems = carrito.reduce((sum, item) => sum + (parseInt(item.cantidad, 10) || 0), 0);
        const insignia = document.getElementById('insigniaCarrito');
        if (insignia) {
            if (totalItems > 0) {
                insignia.textContent = totalItems;
                insignia.style.display = 'inline';
            } else {
                insignia.style.display = 'none';
            }
        }
        const contador = document.getElementById('contadorDrawer');
        if (contador) {
            contador.textContent = totalItems;
        }
    }

    function calcularTotal() {
        const carrito = obtenerCarrito();
        return carrito.reduce((sum, item) => sum + (parseFloat(item.precio) * parseInt(item.cantidad, 10)), 0);
    }

    function mostrarToast(mensaje) {
        const texto = document.getElementById('textoToast');
        const caja = document.getElementById('toastCarrito');
        if (!texto || !caja || typeof bootstrap === 'undefined') return;
        texto.textContent = mensaje;
        bootstrap.Toast.getOrCreateInstance(caja).show();
    }

    function abrirDrawer() {
        const drawer = document.getElementById('drawerCarrito');
        if (drawer && typeof bootstrap !== 'undefined') {
            bootstrap.Offcanvas.getOrCreateInstance(drawer).show();
        }
    }

    function cerrarDrawer() {
        const drawer = document.getElementById('drawerCarrito');
        if (drawer && typeof bootstrap !== 'undefined') {
            const instancia = bootstrap.Offcanvas.getInstance(drawer);
            if (instancia) instancia.hide();
        }
    }

    function mostrarAlerta(mensaje, tipo) {
        const alerta = document.getElementById('alertaDrawer');
        if (!alerta) return;
        alerta.innerHTML = '<div class="alert alert-' + (tipo || 'danger') + ' alert-dismissible fade show" role="alert">'
            + mensaje
            + '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button></div>';
    }

    function agregarAlCarrito(id, nombre, precio, imagen) {
        id = String(id);
        const carrito = obtenerCarrito();
        const existente = carrito.find(item => String(item.id) === id);

        if (existente) {
            existente.cantidad = parseInt(existente.cantidad, 10) + 1;
        } else {
            carrito.push({
                id: id,
                nombre: nombre,
                precio: parseFloat(precio),
                imagen: imagen || '',
                cantidad: 1
            });
        }

        guardarCarrito(carrito);
        renderizarDrawer();
        mostrarToast('"' + nombre + '" agregado al carrito');
        abrirDrawer();
    }

    function eliminarDelCarrito(id) {
        id = String(id);
        let carrito = obtenerCarrito();
        carrito = carrito.filter(item => String(item.id) !== id);
        guardarCarrito(carrito);
        renderizarDrawer();
    }

    function actualizarCantidad(id, nuevaCantidad) {
        id = String(id);
        const carrito = obtenerCarrito();
        const item = carrito.find(entry => String(entry.id) === id);

        if (item) {
            if (nuevaCantidad <= 0) {
                eliminarDelCarrito(id);
                return;
            }
            item.cantidad = nuevaCantidad;
            guardarCarrito(carrito);
            renderizarDrawer();
        }
    }

    function vaciarCarrito() {
        localStorage.removeItem(claveCarrito());
        actualizarInsignia();
        renderizarDrawer();
    }

    function renderizarDrawer() {
        const carrito = obtenerCarrito();
        const vacio = document.getElementById('vacioDrawer');
        const items = document.getElementById('itemsDrawer');
        const pie = document.getElementById('pieDrawer');
        const subtotalEl = document.getElementById('subtotalDrawer');

        if (!items) return;

        const total = calcularTotal();

        if (carrito.length === 0) {
            if (vacio) vacio.style.display = 'block';
            if (pie) pie.style.display = 'none';
            items.innerHTML = '';
            return;
        }

        if (vacio) vacio.style.display = 'none';
        if (pie) pie.style.display = 'block';

        items.innerHTML = '';
        carrito.forEach(item => {
            const subtotal = parseFloat(item.precio) * parseInt(item.cantidad, 10);
            const fila = document.createElement('div');
            fila.className = 'd-flex gap-2 mb-3 pb-3 border-bottom';
            fila.innerHTML =
                (item.imagen ? '<img src="' + item.imagen + '" alt="" style="width:60px;height:60px;object-fit:contain;">' : '') +
                '<div class="flex-grow-1">' +
                    '<p class="mb-1 fw-bold small">' + item.nombre + '</p>' +
                    '<p class="mb-1 small text-muted">S/ ' + parseFloat(item.precio).toFixed(2) + ' c/u</p>' +
                    '<div class="input-group input-group-sm" style="width:120px;">' +
                        '<button class="btn btn-outline-secondary" onclick="techZoneCantidad(\'' + item.id + '\',' + (parseInt(item.cantidad, 10) - 1) + ')">-</button>' +
                        '<input type="text" class="form-control text-center" value="' + item.cantidad + '" readonly>' +
                        '<button class="btn btn-outline-secondary" onclick="techZoneCantidad(\'' + item.id + '\',' + (parseInt(item.cantidad, 10) + 1) + ')">+</button>' +
                    '</div>' +
                '</div>' +
                '<div class="text-end">' +
                    '<p class="mb-2 fw-bold small">S/ ' + subtotal.toFixed(2) + '</p>' +
                    '<button class="btn btn-sm btn-outline-danger" title="Eliminar producto" onclick="techZoneEliminar(\'' + item.id + '\')">' +
                        '<i class="bi bi-trash"></i>' +
                    '</button>' +
                '</div>';
            items.appendChild(fila);
        });

        if (subtotalEl) {
            subtotalEl.textContent = 'S/ ' + total.toFixed(2);
        }
    }

    function estaAutenticado() {
        const drawer = document.getElementById('drawerCarrito');
        return drawer && drawer.dataset.autenticado === 'true';
    }

    function abrirLogin() {
        try {
            sessionStorage.setItem('checkoutPendiente', '1');
        } catch (e) { /* sin almacenamiento */ }
        cerrarDrawer();
        const modal = document.getElementById('modalLogin');
        if (modal && typeof bootstrap !== 'undefined') {
            bootstrap.Modal.getOrCreateInstance(modal).show();
        } else {
            window.location.href = '/login';
        }
    }

    function finalizarCompra() {
        const carrito = obtenerCarrito();
        if (carrito.length === 0) {
            mostrarAlerta('El carrito esta vacio', 'warning');
            return;
        }
        fetch('/api/mis-datos', { credentials: 'same-origin' })
            .then(function (r) {
                if (r.ok) {
                    return r.json();
                }
                throw new Error('no-auth');
            })
            .then(function (data) {
                if (data && data.ok) {
                    if (typeof window.techZoneAbrirCheckout === 'function') {
                        window.techZoneAbrirCheckout();
                    }
                } else {
                    abrirLogin();
                }
            })
            .catch(function () {
                abrirLogin();
            });
    }

    window.techZoneEliminar = eliminarDelCarrito;
    window.techZoneCantidad = actualizarCantidad;
    window.techZoneFinalizar = finalizarCompra;
    window.techZoneEliminarDelCarrito = eliminarDelCarrito;
    window.techZoneActualizarCantidad = actualizarCantidad;

    document.querySelectorAll('.agregar-carrito').forEach(btn => {
        btn.addEventListener('click', function () {
            agregarAlCarrito(this.dataset.id, this.dataset.nombre, this.dataset.precio, this.dataset.imagen);
        });
    });

    const btnVaciar = document.getElementById('btnVaciarDrawer');
    if (btnVaciar) {
        btnVaciar.addEventListener('click', vaciarCarrito);
    }

    const btnFinalizar = document.getElementById('btnFinalizar');
    if (btnFinalizar) {
        btnFinalizar.addEventListener('click', finalizarCompra);
    }

    // Inicializar
    actualizarInsignia();
    renderizarDrawer();
});
