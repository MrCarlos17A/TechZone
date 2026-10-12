(function () {
    'use strict';

    function initCascadaUbigeo(prefijo) {
        var depto = document.getElementById('departamento' + prefijo);
        var prov = document.getElementById('provincia' + prefijo);
        var dist = document.getElementById('distrito' + prefijo);
        var codigo = document.getElementById('codigoUbigeo' + prefijo);
        if (!depto || !prov || !dist) return;

        var lista = [];

        fetch('/api/ubigeos')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                lista = data || [];
                poblarDepartamentos();
                restaurarValores();
            })
            .catch(function () { });

        function poblarDepartamentos() {
            depto.innerHTML = '<option value="">Seleccionar...</option>';
            var deps = [...new Set(lista.map(function (u) { return u.departamento; }))].sort();
            deps.forEach(function (d) {
                depto.innerHTML += '<option value="' + d + '">' + d + '</option>';
            });
        }

        function poblarProvincias(departamento, provActual) {
            prov.innerHTML = '<option value="">Seleccionar...</option>';
            dist.innerHTML = '<option value="">Seleccionar...</option>';
            dist.disabled = true;
            if (!departamento) { prov.disabled = true; return; }
            prov.disabled = false;
            var provs = [...new Set(
                lista.filter(function (u) { return u.departamento === departamento; })
                    .map(function (u) { return u.provincia; })
            )].sort();
            provs.forEach(function (p) {
                prov.innerHTML += '<option value="' + p + '">' + p + '</option>';
            });
            if (provActual) prov.value = provActual;
        }

        function poblarDistritos(departamento, provincia, distActual) {
            dist.innerHTML = '<option value="">Seleccionar...</option>';
            if (!departamento || !provincia) { dist.disabled = true; return; }
            dist.disabled = false;
            var dists = lista
                .filter(function (u) { return u.departamento === departamento && u.provincia === provincia; })
                .sort(function (a, b) { return a.distrito.localeCompare(b.distrito); });
            dists.forEach(function (d) {
                dist.innerHTML += '<option value="' + d.id + '">' + d.distrito + '</option>';
            });
            if (distActual) dist.value = distActual;
        }

        depto.addEventListener('change', function () {
            poblarProvincias(this.value);
            if (codigo) codigo.value = '';
        });
        prov.addEventListener('change', function () {
            poblarDistritos(depto.value, this.value);
            if (codigo) codigo.value = '';
        });
        dist.addEventListener('change', function () {
            if (codigo) codigo.value = this.value || '';
        });

        if (codigo) {
            codigo.addEventListener('input', function () {
                var cod = this.value.replace(/\D/g, '');
                this.value = cod;
                if (cod.length < 6) return;
                var u = lista.find(function (x) { return String(x.id) === cod; });
                if (u) {
                    depto.value = u.departamento;
                    poblarProvincias(u.departamento, u.provincia);
                    poblarDistritos(u.departamento, u.provincia, String(u.id));
                }
            });
        }

        function restaurarValores() {
            var valDepto = depto.getAttribute('data-actual');
            var valProv = prov.getAttribute('data-actual');
            var valDist = dist.getAttribute('data-actual');
            if (!valDepto) return;
            depto.value = valDepto;
            poblarProvincias(valDepto, valProv);
            if (valProv) {
                poblarDistritos(valDepto, valProv, valDist);
                if (valDist && codigo) codigo.value = valDist;
            }
        }
    }

    window.initCascadaUbigeo = initCascadaUbigeo;
})();
