// Pide confirmación antes de enviar un formulario que vaya a eliminar un cliente.
document.addEventListener('submit', function (evento) {
    const mensaje = evento.target.dataset ? evento.target.dataset.confirmar : null;
    if (mensaje && !window.confirm(mensaje)) {
        evento.preventDefault();
    }
});
