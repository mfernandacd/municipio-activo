document.addEventListener('DOMContentLoaded', () => {
    const authLink = document.querySelector('[data-auth-link]');
    const authContainer = authLink?.parentElement;

    window.municipioSessionReady = sincronizarSesion();

    async function sincronizarSesion() {
        try {
            const response = await window.municipioFetch(resolverApiUrl('/api/auth/session'), {
                credentials: 'include'
            });

            if (response.status === 401) {
                localStorage.removeItem('usuarioMunicipio');
                if (document.body.hasAttribute('data-auth-required')) {
                    redirigirALogin('Se requiere registrar para poner un reclamo');
                    return null;
                }
                protegerAdministracion(null);
                return null;
            }
            if (!response.ok) {
                throw new Error(`La API respondió con el error ${response.status}.`);
            }

            const usuario = await response.json();
            usuario.privilegios = normalizarPrivilegios(usuario.privilegios);
            localStorage.setItem('usuarioMunicipio', JSON.stringify(usuario));
            document.body.removeAttribute('data-auth-required');
            if (authLink && authContainer) {
                mostrarSesionIniciada(usuario);
            }
            protegerAdministracion(usuario);
            return usuario;
        } catch (error) {
            console.error('No se pudo comprobar la sesión del usuario.', error);
            protegerAdministracion(null);
            if (document.body.hasAttribute('data-auth-required')) {
                redirigirALogin('No se pudo verificar tu sesión. Iniciá sesión para poner un reclamo.');
            }
            return null;
        }
    }

    function redirigirALogin(mensaje) {
        sessionStorage.setItem('mensajeAccesoLogin', mensaje);
        window.location.replace('login.html');
    }

    function protegerAdministracion(usuario) {
        const esAdministrativo = usuario?.rol?.toLowerCase() === 'administrativo';
        document.querySelectorAll('[data-admin-only]').forEach((element) => {
            element.hidden = !esAdministrativo;
        });

        if (document.body.hasAttribute('data-admin-page') && !esAdministrativo) {
            window.location.replace('noticias.html');
        }
    }

    function mostrarSesionIniciada(usuario) {
        const saludo = document.createElement('span');
        saludo.className = 'navbar-text text-white ms-2';
        saludo.textContent = `Hola, ${usuario.nombre}`;

        const cerrarSesion = document.createElement('button');
        cerrarSesion.type = 'button';
        cerrarSesion.className = 'btn btn-outline-light ms-2';
        cerrarSesion.textContent = 'Cerrar sesión';
        cerrarSesion.addEventListener('click', cerrarSesionUsuario);

        authContainer.replaceChildren(saludo, cerrarSesion);
    }

    async function cerrarSesionUsuario() {
        try {
            const response = await window.municipioFetch(resolverApiUrl('/api/auth/logout'), {
                method: 'POST',
                credentials: 'include'
            });
            if (!response.ok) {
                throw new Error(`La API respondió con el error ${response.status}.`);
            }

            localStorage.removeItem('usuarioMunicipio');
            authContainer.replaceChildren(authLink);
            window.location.reload();
        } catch (error) {
            console.error('No se pudo cerrar la sesión del usuario.', error);
            window.alert('No se pudo cerrar la sesión. Revisá la conexión con el servidor e intentá nuevamente.');
        }
    }

    function resolverApiUrl(endpoint) {
        return window.municipioApiUrl(endpoint, 'municipio-api-base-url');
    }

    function normalizarPrivilegios(privilegios) {
        if (Array.isArray(privilegios)) return privilegios;
        return typeof privilegios === 'string'
            ? privilegios.split(',').map((privilegio) => privilegio.trim()).filter(Boolean)
            : [];
    }
});
