(() => {
    const metodosSeguros = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE']);
    let origenToken = null;
    let tokenCsrfPromise = null;

    window.municipioApiUrl = (endpoint, metaName = 'municipio-api-base-url') => {
        if (window.location.protocol === 'file:') {
            throw new Error('Abrí el sitio desde el backend HTTP, no como archivo local.');
        }

        const configuredBaseUrl = [metaName, 'municipio-api-base-url']
            .map((name) => document.querySelector(`meta[name="${name}"]`)?.content.trim())
            .find(Boolean);
        const baseUrl = configuredBaseUrl || (
            ['localhost', '127.0.0.1'].includes(window.location.hostname)
                ? `${window.location.protocol}//${window.location.hostname}:8080`
                : window.location.origin
        );

        let parsedBaseUrl;
        try {
            parsedBaseUrl = new URL(baseUrl);
        } catch {
            throw new Error('La URL configurada del backend no es válida.');
        }
        if (!['http:', 'https:'].includes(parsedBaseUrl.protocol)) {
            throw new Error('La URL del backend debe comenzar con http:// o https://.');
        }

        return new URL(endpoint, `${parsedBaseUrl.toString().replace(/\/+$/, '')}/`).toString();
    };

    async function obtenerTokenCsrf(apiUrl) {
        if (origenToken !== apiUrl.origin) {
            origenToken = apiUrl.origin;
            tokenCsrfPromise = null;
        }

        if (!tokenCsrfPromise) {
            tokenCsrfPromise = fetch(new URL('/api/auth/csrf', apiUrl.origin), {
                credentials: 'include',
                headers: { Accept: 'application/json' }
            }).then(async (response) => {
                if (!response.ok) {
                    throw new Error(`No se pudo obtener el token de seguridad (${response.status}).`);
                }
                return response.json();
            }).catch((error) => {
                tokenCsrfPromise = null;
                throw error;
            });
        }

        return tokenCsrfPromise;
    }

    window.municipioFetch = async (input, options = {}) => {
        const apiUrl = new URL(input, window.location.href);
        if (!['http:', 'https:'].includes(apiUrl.protocol)) {
            throw new Error('Abrí el sitio desde el backend HTTP, no como archivo local.');
        }
        const method = (options.method || 'GET').toUpperCase();
        const headers = new Headers(options.headers || {});

        if (!metodosSeguros.has(method)) {
            const csrfToken = await obtenerTokenCsrf(apiUrl);
            headers.set(csrfToken.headerName || 'X-CSRF-TOKEN', csrfToken.token);
        }

        const response = await fetch(apiUrl, {
            ...options,
            headers,
            credentials: options.credentials || 'include'
        });

        if (method === 'POST' && apiUrl.pathname === '/api/auth/logout' && response.ok) {
            origenToken = null;
            tokenCsrfPromise = null;
        }

        return response;
    };
})();
