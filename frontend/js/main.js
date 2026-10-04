document.addEventListener('DOMContentLoaded', () => {
    const authLink = document.querySelector('[data-auth-link]');
    const authContainer = authLink?.parentElement;

    if (!authLink || !authContainer) return;

    sincronizarSesion();

    async function sincronizarSesion() {
        try {
            const response = await fetch(resolverApiUrl('/api/auth/session'), {
                credentials: 'include'
            });

            if (response.status === 401) {
                localStorage.removeItem('usuarioMunicipio');
                return;
            }
            if (!response.ok) {
                throw new Error(`La API respondió con el error ${response.status}.`);
            }

            const usuario = await response.json();
            usuario.privilegios = normalizarPrivilegios(usuario.privilegios);
            localStorage.setItem('usuarioMunicipio', JSON.stringify(usuario));
            mostrarSesionIniciada(usuario);
        } catch (error) {
            console.error('No se pudo comprobar la sesión del usuario.', error);
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
            const response = await fetch(resolverApiUrl('/api/auth/logout'), {
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
        const configuredBaseUrl = document.querySelector('meta[name="municipio-api-base-url"]')
            ?.content.trim();
        const baseUrl = configuredBaseUrl || (
            ['localhost', '127.0.0.1'].includes(window.location.hostname)
                ? `${window.location.protocol}//${window.location.hostname}:8080`
                : window.location.origin
        );
        return new URL(endpoint, `${baseUrl.replace(/\/+$/, '')}/`).toString();
    }

    function normalizarPrivilegios(privilegios) {
        if (Array.isArray(privilegios)) return privilegios;
        return typeof privilegios === 'string'
            ? privilegios.split(',').map((privilegio) => privilegio.trim()).filter(Boolean)
            : [];
    }
});
