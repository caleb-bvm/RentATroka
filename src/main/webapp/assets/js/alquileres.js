// Importe estimado en el formulario de alquiler.
// Solo es una vista previa: el servidor vuelve a calcular el importe al guardar.

const UN_DIA = 24 * 60 * 60 * 1000; // milisegundos

const resumen = document.getElementById("resumen-alquiler");
const vehiculo = document.getElementById("vehiculoId");
const inicio = document.getElementById("fechaInicio");
const fin = document.getElementById("fechaFin");

// Tarifa del vehículo elegido. Al editar sin cambiar de vehículo se usa la tarifa guardada.
function obtenerTarifa() {
    const opcion = vehiculo.options[vehiculo.selectedIndex];
    if (!opcion || opcion.value === "") {
        return null;
    }
    if (resumen.dataset.tarifaGuardada && opcion.value === resumen.dataset.vehiculoGuardado) {
        return parseFloat(resumen.dataset.tarifaGuardada);
    }
    return parseFloat(opcion.dataset.tarifa);
}

function actualizarResumen() {
    // valueAsNumber da la fecha en milisegundos, o NaN si el campo está vacío
    const desde = inicio.valueAsNumber;
    const hasta = fin.valueAsNumber;
    const tarifa = obtenerTarifa();

    // la fecha final debe ser al menos un día después de la inicial
    if (!isNaN(desde)) {
        fin.min = new Date(desde + UN_DIA).toISOString().slice(0, 10);
    }

    let textoTarifa = "—";
    let textoDias = "—";
    let textoTotal = "—";

    if (tarifa !== null) {
        textoTarifa = "$" + tarifa.toFixed(2);
    }

    if (!isNaN(desde) && !isNaN(hasta) && hasta > desde) {
        const dias = Math.round((hasta - desde) / UN_DIA);
        textoDias = String(dias);
        if (tarifa !== null) {
            textoTotal = "$" + (dias * tarifa).toFixed(2);
        }
    }

    document.getElementById("resumen-tarifa").textContent = textoTarifa;
    document.getElementById("resumen-dias").textContent = textoDias;
    document.getElementById("resumen-total").textContent = textoTotal;
}

if (resumen && vehiculo && inicio && fin) {
    vehiculo.addEventListener("change", actualizarResumen);
    inicio.addEventListener("change", actualizarResumen);
    fin.addEventListener("change", actualizarResumen);
    actualizarResumen();
}
