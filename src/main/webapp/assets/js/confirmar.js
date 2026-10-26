// Pide confirmación antes de enviar un formulario que tenga el atributo data-confirmar
// (los botones Eliminar de los listados de clientes, vehículos y alquileres)
document.addEventListener("submit", function (evento) {
    const mensaje = evento.target.dataset.confirmar;

    if (mensaje && !window.confirm(mensaje)) {
        evento.preventDefault();
    }
});
