// Pide confirmación antes de enviar cualquier formulario que tenga el atributo data-confirmar

document.addEventListener('submit', function (evento) {
    const mensaje = evento.target.dataset ? evento.target.dataset.confirmar : null;
    if (mensaje && !window.confirm(mensaje)) {
        evento.preventDefault();
    }
});
