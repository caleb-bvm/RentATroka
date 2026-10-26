// confirmar para eliminar alquiler
document.addEventListener("submit", function (evento) {
    const formulario = evento.target;

    if (!formulario.matches("form[data-confirmar]")) {
        return;
    }

    if (!window.confirm(formulario.dataset.confirmar)) {
        evento.preventDefault();
    }
});

// importe estimado en el formulario (solo es una vista previa; el servidor lo vuelve a calcular)
document.addEventListener("DOMContentLoaded", function () {
    const resumen = document.getElementById("resumen-alquiler");
    const vehiculo = document.getElementById("vehiculoId");
    const inicio = document.getElementById("fechaInicio");
    const fin = document.getElementById("fechaFin");

    if (!resumen || !vehiculo || !inicio || !fin) {
        return;
    }

    const vehiculoGuardado = resumen.dataset.vehiculoGuardado;
    const tarifaGuardada = resumen.dataset.tarifaGuardada;

    function aFecha(texto) {
        const partes = texto.split("-").map(Number);
        return partes.length === 3 && !partes.includes(NaN)
            ? Date.UTC(partes[0], partes[1] - 1, partes[2])
            : null;
    }

    function dinero(valor) {
        return "$" + valor.toFixed(2);
    }

    function tarifaAplicable() {
        const opcion = vehiculo.options[vehiculo.selectedIndex];

        if (!opcion || !opcion.value) {
            return null;
        }

        // mismo vehículo que ya tenía el alquiler: se conserva su tarifa guardada
        if (tarifaGuardada && opcion.value === vehiculoGuardado) {
            return parseFloat(tarifaGuardada);
        }

        return parseFloat(opcion.dataset.tarifa);
    }

    function actualizar() {
        const desde = aFecha(inicio.value);
        const hasta = aFecha(fin.value);
        const tarifa = tarifaAplicable();

        // la fecha final debe ser al menos un día después de la inicial
        if (desde !== null) {
            const minimo = new Date(desde + 86400000).toISOString().slice(0, 10);
            fin.min = minimo;
        }

        const tarifaTexto = document.getElementById("resumen-tarifa");
        const diasTexto = document.getElementById("resumen-dias");
        const totalTexto = document.getElementById("resumen-total");

        tarifaTexto.textContent = tarifa === null ? "—" : dinero(tarifa);

        if (desde === null || hasta === null || hasta <= desde) {
            diasTexto.textContent = "—";
            totalTexto.textContent = "—";
            return;
        }

        const dias = Math.round((hasta - desde) / 86400000);
        diasTexto.textContent = String(dias);
        totalTexto.textContent = tarifa === null ? "—" : dinero(dias * tarifa);
    }

    [vehiculo, inicio, fin].forEach(function (campo) {
        campo.addEventListener("input", actualizar);
        campo.addEventListener("change", actualizar);
    });

    actualizar();
});