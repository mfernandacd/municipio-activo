document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const usuarioInput = document.getElementById('usuario');
    const passwordInput = document.getElementById('password');
    const registroForm = document.getElementById('registro-form');
    const registroNombreInput = document.getElementById('registroNombre');
    const registroEmailInput = document.getElementById('registroEmail');
    const registroPasswordInput = document.getElementById('registroPassword');
    const apiBaseUrlInput = document.querySelector('meta[name="municipio-api-base-url"]');

    const correoRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!loginForm || !usuarioInput || !passwordInput) return;

    const limpiarEstadoCampo = (field) => {
        field.classList.remove('is-invalid', 'is-valid');
        field.setCustomValidity('');
    };

    const setFieldState = (field, isValid) => {
        limpiarEstadoCampo(field);
        field.classList.add(isValid ? 'is-valid' : 'is-invalid');
        if (!isValid) field.setCustomValidity('Revisá este dato.');
    };

    const actualizarEstadoValidacion = (field, validator) => {
        const value = field.value.trim();
        if (!value) {
            limpiarEstadoCampo(field);
            return;
        }
        setFieldState(field, validator(value));
    };

    usuarioInput.addEventListener('input', () => {
        actualizarEstadoValidacion(usuarioInput, (value) => correoRegex.test(value));
    });

    passwordInput.addEventListener('input', () => {
        actualizarEstadoValidacion(passwordInput, esContrasenaValida);
    });

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const email = usuarioInput.value.trim().toLowerCase();
        const password = passwordInput.value;
        const emailValido = correoRegex.test(email);
        const passwordValida = esContrasenaValida(password);
        setFieldState(usuarioInput, emailValido);
        setFieldState(passwordInput, passwordValida);

        if (!emailValido || !passwordValida) {
            await mostrarError(
                'Datos incorrectos',
                'Verificá el correo y usá una contraseña de 6 a 8 letras o números, sin caracteres especiales.'
            );
            return;
        }

        try {
            const usuario = await llamarApi('/api/auth/login', { email, password });
            usuario.privilegios = normalizarPrivilegios(usuario.privilegios);
            localStorage.setItem('usuarioMunicipio', JSON.stringify(usuario));

            await Swal.fire({
                icon: 'success',
                title: usuario.rol === 'administrativo'
                    ? 'Acceso administrativo'
                    : usuario.rol === 'municipal' ? 'Acceso municipal' : '¡Bienvenido!',
                text: 'Iniciando sesión...',
                showConfirmButton: false,
                timer: 1200
            });
            window.location.href = loginForm.action;
        } catch (error) {
            await mostrarError('No se pudo iniciar sesión', obtenerMensajeError(error));
        }
    });

    if (!registroForm || !registroNombreInput || !registroEmailInput || !registroPasswordInput) return;

    registroNombreInput.addEventListener('input', () => {
        actualizarEstadoValidacion(registroNombreInput, (value) => value.length > 0 && value.length <= 120);
    });
    registroEmailInput.addEventListener('input', () => {
        actualizarEstadoValidacion(registroEmailInput, (value) => correoRegex.test(value));
    });
    registroPasswordInput.addEventListener('input', () => {
        actualizarEstadoValidacion(registroPasswordInput, esContrasenaValida);
    });

    registroForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const nombre = registroNombreInput.value.trim();
        const email = registroEmailInput.value.trim().toLowerCase();
        const password = registroPasswordInput.value;
        const nombreValido = nombre.length > 0 && nombre.length <= 120;
        const emailValido = correoRegex.test(email);
        const passwordValida = esContrasenaValida(password);

        setFieldState(registroNombreInput, nombreValido);
        setFieldState(registroEmailInput, emailValido);
        setFieldState(registroPasswordInput, passwordValida);

        if (!nombreValido || !emailValido || !passwordValida) {
            await mostrarError(
                'No se pudo registrar',
                'Completá los datos correctamente. La contraseña debe tener 6 a 8 letras o números, sin caracteres especiales.'
            );
            return;
        }

        try {
            await llamarApi('/api/auth/registro', { nombre, email, password });
            bootstrap.Modal.getOrCreateInstance(document.getElementById('registroModal')).hide();
            registroForm.reset();
            [registroNombreInput, registroEmailInput, registroPasswordInput].forEach(limpiarEstadoCampo);

            usuarioInput.value = email;
            passwordInput.value = password;
            setFieldState(usuarioInput, true);
            setFieldState(passwordInput, true);

            await Swal.fire({
                icon: 'success',
                title: 'Usuario creado',
                text: 'La cuenta se creó correctamente. Iniciando sesión...',
                confirmButtonColor: '#0d6efd'
            });
            loginForm.requestSubmit();
        } catch (error) {
            await mostrarError('No se pudo registrar', obtenerMensajeError(error));
        }
    });

    async function llamarApi(endpoint, body) {
        const response = await fetch(resolverApiUrl(endpoint), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(body)
        });

        const responseText = await response.text();
        let responseBody;
        try {
            responseBody = responseText ? JSON.parse(responseText) : {};
        } catch {
            throw new Error(response.ok
                ? 'La API devolvió una respuesta inválida.'
                : `La API respondió con el error ${response.status}.`);
        }

        if (!response.ok) {
            throw new Error(responseBody.mensaje || `La API respondió con el error ${response.status}.`);
        }
        return responseBody;
    }

    function resolverApiUrl(endpoint) {
        if (window.location.protocol === 'file:') {
            throw new Error(
                'No abras esta página como archivo local. Iniciá Apache y el backend, y entrá desde http://localhost/municipio-activo/frontend/login.html.'
            );
        }

        const configuredBaseUrl = apiBaseUrlInput?.content.trim();
        if (configuredBaseUrl) {
            let configuredUrl;
            try {
                configuredUrl = new URL(configuredBaseUrl);
            } catch {
                throw new Error('La URL configurada del backend no es válida.');
            }
            if (!['http:', 'https:'].includes(configuredUrl.protocol)) {
                throw new Error('La URL del backend debe comenzar con http:// o https://.');
            }
            return new URL(endpoint, `${configuredUrl.toString().replace(/\/+$/, '')}/`).toString();
        }

        const backendUrl = ['localhost', '127.0.0.1'].includes(window.location.hostname)
            ? `${window.location.protocol}//${window.location.hostname}:8080`
            : window.location.origin;
        return new URL(endpoint, `${backendUrl}/`).toString();
    }

    function esContrasenaValida(password) {
        return /^[A-Za-z0-9]{6,8}$/.test(password);
    }

    function normalizarPrivilegios(privilegios) {
        if (Array.isArray(privilegios)) return privilegios;
        return typeof privilegios === 'string'
            ? privilegios.split(',').map((privilegio) => privilegio.trim()).filter(Boolean)
            : [];
    }

    function obtenerMensajeError(error) {
        if (error instanceof TypeError) {
            return 'No se pudo conectar con la API. Verificá que el backend esté iniciado, su URL y la configuración CORS.';
        }
        return error instanceof Error ? error.message : 'Ocurrió un error al comunicarse con el servidor.';
    }

    function mostrarError(title, text) {
        return Swal.fire({
            icon: 'error',
            title,
            text,
            confirmButtonColor: '#0d6efd'
        });
    }
});
