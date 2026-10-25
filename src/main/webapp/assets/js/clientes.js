// confirmar para eliminar usaurio

document.addEventListener("submit", function (evento) {
    const formulario = evento.target;

    if (!formulario.matches("form[data-confirmar]")) {
        return;
    }

    const mensaje = formulario.dataset.confirmar;

    if (!window.confirm(mensaje)) {
        evento.preventDefault();
    }
});