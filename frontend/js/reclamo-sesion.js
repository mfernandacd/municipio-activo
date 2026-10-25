(() => {
    const mensaje = 'Iniciá sesión con una cuenta registrada para presentar un reclamo.';

    function redirigirALogin() {
        try {
            window.sessionStorage.setItem('mensajeAccesoLogin', mensaje);
        } catch (error) {
            console.warn('No se pudo guardar el aviso de acceso.', error);
        }
        window.location.replace('login.html');
    }

    function leerUsuario(storage) {
        try {
            const datos = storage.getItem('usuarioMunicipio');
            if (!datos) return null;

            const usuario = JSON.parse(datos);
            if (!usuario || typeof usuario !== 'object') return null;

            const nombre = typeof usuario.nombre === 'string' ? usuario.nombre.trim() : '';
            const email = typeof usuario.email === 'string' ? usuario.email.trim() : '';
            if (!nombre || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return null;

            return { ...usuario, nombre, email };
        } catch {
            return null;
        }
    }

    let usuario = null;
    try {
        usuario = leerUsuario(window.localStorage) || leerUsuario(window.sessionStorage);
    } catch {
        redirigirALogin();
        return;
    }

    if (!usuario) {
        redirigirALogin();
        return;
    }

    window.usuarioMunicipioLocal = usuario;
})();
