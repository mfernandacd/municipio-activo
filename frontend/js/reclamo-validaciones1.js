document.addEventListener('DOMContentLoaded', async () => {
    const form = document.getElementById('claimForm');
    const submitButton = document.getElementById('submitBtn');
    const nameInput = document.getElementById('userName');
    const emailInput = document.getElementById('userEmail');

    if (!form || !submitButton || !nameInput || !emailInput) return;

    const usuario = await window.municipioSessionReady;
    if (!usuario) return;

    nameInput.value = usuario.nombre;
    emailInput.value = usuario.email;
    nameInput.readOnly = true;
    emailInput.readOnly = true;

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!form.reportValidity()) return;

        const payload = {
            categoria: document.getElementById('claimCategory').value.trim(),
            direccion: document.getElementById('claimAddress').value.trim(),
            barrio: document.getElementById('claimNeighborhood').value.trim(),
            zona: document.getElementById('claimZone').value,
            googleMapsUrl: document.getElementById('googleMapsUrl').value.trim(),
            descripcion: document.getElementById('claimDescription').value.trim()
        };

        setLoading(true);
        try {
            const response = await window.municipioFetch(resolverApiUrl('/api/reclamos'), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const responseBody = await leerRespuesta(response);
            if (!response.ok) {
                if (response.status === 401) {
                    window.localStorage.removeItem('usuarioMunicipio');
                    window.sessionStorage.removeItem('usuarioMunicipio');
                    window.sessionStorage.setItem(
                        'mensajeAccesoLogin',
                        'Tu sesión venció. Iniciá sesión nuevamente para presentar el reclamo.'
                    );
                    window.location.replace('login.html');
                    return;
                }
                throw new Error(responseBody.message || responseBody.detail || `Error del servidor (${response.status}).`);
            }

            const codigo = responseBody.codigoSeguimiento;
            if (!codigo) {
                throw new Error('El reclamo se registró, pero el servidor no devolvió el código de seguimiento.');
            }

            await mostrarAviso(
                'success',
                'Reclamo registrado',
                `Guardamos tu reclamo. Tu código de seguimiento es ${codigo}.`
            );
            form.reset();
            nameInput.value = usuario.nombre;
            emailInput.value = usuario.email;
        } catch (error) {
            console.error('No se pudo registrar el reclamo.', error);
            const mensaje = error instanceof TypeError
                ? 'No se pudo conectar con el servidor. Revisá la conexión e intentá nuevamente.'
                : error.message || 'Ocurrió un error al registrar el reclamo.';
            await mostrarAviso('error', 'No se pudo registrar el reclamo', mensaje);
        } finally {
            setLoading(false);
        }
    });

    function setLoading(loading) {
        submitButton.disabled = loading;
        submitButton.innerHTML = loading
            ? '<i class="fa-solid fa-spinner fa-spin me-2" aria-hidden="true"></i>Enviando reclamo…'
            : '<i class="fa-solid fa-paper-plane me-2" aria-hidden="true"></i>Enviar reclamo';
    }

    async function leerRespuesta(response) {
        const responseText = await response.text();
        if (!responseText) return {};

        try {
            return JSON.parse(responseText);
        } catch {
            throw new Error('El servidor devolvió una respuesta no válida.');
        }
    }

    function resolverApiUrl(endpoint) {
        return window.municipioApiUrl(endpoint, 'municipio-api-base-url');
    }

    async function mostrarAviso(icon, title, text) {
        if (window.Swal?.fire) {
            await window.Swal.fire({ icon, title, text, confirmButtonColor: '#0A2240' });
            return;
        }
        window.alert(`${title}: ${text}`);
    }
});
