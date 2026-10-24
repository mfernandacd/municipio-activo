document.addEventListener('DOMContentLoaded', () => {
    const formReclamo = document.getElementById('formReclamo');
    const categoriaInput = document.getElementById('categoria');
    const direccionInput = document.getElementById('direccion');
    const googleMapsInput = document.getElementById('googleMapsUrl');
    const descripcionInput = document.getElementById('descripcion');
    const imagenInput = document.getElementById('imagen');
    const charCounter = document.getElementById('charCounter');
    const submitButton = formReclamo?.querySelector('button[type="submit"]');
    const apiBaseUrlInput = document.querySelector('meta[name="reclamos-api-base-url"]');

    const minimumDescriptionLength = 15;
    const maximumImageSize = 5 * 1024 * 1024;

    if (!formReclamo) return;

    if (descripcionInput && charCounter) {
        descripcionInput.addEventListener('input', () => {
            charCounter.textContent = `${descripcionInput.value.length} caracteres`;
        });
    }

    formReclamo.addEventListener('submit', async (event) => {
        event.preventDefault();

        const categoria = categoriaInput?.value.trim() ?? '';
        const direccion = direccionInput?.value.trim() ?? '';
        const googleMapsUrl = googleMapsInput?.value.trim() ?? '';
        const descripcion = descripcionInput?.value.trim() ?? '';
        const imagen = imagenInput?.files?.[0];

        if (!categoria) {
            await mostrarError('Categoría requerida', 'Seleccioná una categoría para tu reclamo.', 'warning');
            categoriaInput?.focus();
            return;
        }

        if (!direccion) {
            await mostrarError('Dirección requerida', 'Ingresá la dirección o ubicación del problema.', 'warning');
            direccionInput?.focus();
            return;
        }

        if (descripcion.length < minimumDescriptionLength) {
            await mostrarError(
                'Descripción insuficiente',
                `La descripción debe contener al menos ${minimumDescriptionLength} caracteres.`,
                'warning'
            );
            descripcionInput?.focus();
            return;
        }

        if (googleMapsUrl && !esUrlHttpValida(googleMapsUrl)) {
            await mostrarError('Enlace inválido', 'Ingresá un enlace válido de Google Maps.', 'warning');
            googleMapsInput?.focus();
            return;
        }

        const allowedImageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
        if (imagen && (!allowedImageTypes.includes(imagen.type) || imagen.size > maximumImageSize)) {
            await mostrarError(
                'Imagen no válida',
                'Seleccioná una imagen JPG, PNG, GIF o WebP de hasta 5 MB.',
                'warning'
            );
            imagenInput?.focus();
            return;
        }

        const confirmacion = await Swal.fire({
            title: '¿Confirmar envío del reclamo?',
            text: 'Tu solicitud será enviada al municipio para su procesamiento.',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#0C2136',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Sí, enviar reclamo',
            cancelButtonText: 'Cancelar',
            customClass: { popup: 'swal2-popup-serranoble' }
        });

        if (!confirmacion.isConfirmed) return;

        if (submitButton) submitButton.disabled = true;
        Swal.fire({
            title: 'Guardando reclamo...',
            text: 'Por favor, aguardá un momento.',
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });

        try {
            const baseUrl = obtenerApiBaseUrl(apiBaseUrlInput?.content);
            const formData = new FormData();
            formData.append('reclamo', new Blob([JSON.stringify({
                categoria,
                direccion,
                googleMapsUrl,
                descripcion
            })], { type: 'application/json' }));
            if (imagen) formData.append('imagen', imagen);

            const response = await fetch(new URL('api/reclamos', `${baseUrl}/`), {
                method: 'POST',
                body: formData
            });
            const responseBody = await response.text();
            let reclamoRegistrado = null;

            if (responseBody) {
                try {
                    reclamoRegistrado = JSON.parse(responseBody);
                } catch {
                    if (response.ok) {
                        throw new Error('El servidor devolvió una respuesta inválida.');
                    }
                }
            }

            if (!response.ok) {
                const errorBody = reclamoRegistrado && typeof reclamoRegistrado === 'object'
                    ? reclamoRegistrado
                    : {};
                throw new Error(
                    errorBody.detail
                    || errorBody.message
                    || `No se pudo guardar el reclamo (error ${response.status}).`
                );
            }

            if (!reclamoRegistrado || typeof reclamoRegistrado !== 'object') {
                throw new Error('El servidor devolvió una respuesta inválida.');
            }

            await Swal.fire({
                icon: 'success',
                title: '¡Reclamo registrado!',
                text: reclamoRegistrado.codigoSeguimiento
                    ? `Guardamos tu solicitud. Código de seguimiento: ${reclamoRegistrado.codigoSeguimiento}.`
                    : 'Tu solicitud se guardó correctamente.',
                confirmButtonColor: '#0C2136'
            });
            formReclamo.reset();
            if (charCounter) charCounter.textContent = '0 caracteres';
        } catch (error) {
            await mostrarError(
                'No se pudo registrar el reclamo',
                error instanceof Error
                    ? error instanceof TypeError
                        ? 'No se pudo conectar con la API. Verificá la URL del backend y su configuración CORS.'
                        : error.message
                    : 'No se pudo conectar con el servidor. Reintentá más tarde.'
            );
        } finally {
            if (submitButton) submitButton.disabled = false;
        }
    });

    function obtenerApiBaseUrl(configuredBaseUrl) {
        if (window.location.protocol === 'file:') {
            throw new Error('Abrí el sitio desde un servidor web, no como archivo local.');
        }

        const configuredUrl = configuredBaseUrl?.trim();
        if (configuredUrl) {
            let url;
            try {
                url = new URL(configuredUrl);
            } catch {
                throw new Error('La URL configurada de la API no es válida.');
            }
            if (!['http:', 'https:'].includes(url.protocol)) {
                throw new Error('La dirección de la API debe comenzar con http:// o https://.');
            }
            return url.toString().replace(/\/+$/, '');
        }

        if (window.location.port === '8080') return window.location.origin;
        if (['localhost', '127.0.0.1'].includes(window.location.hostname)) {
            return `${window.location.protocol}//${window.location.hostname}:8080`;
        }
        return window.location.origin;
    }

    function esUrlHttpValida(value) {
        try {
            return ['http:', 'https:'].includes(new URL(value).protocol);
        } catch {
            return false;
        }
    }

    function mostrarError(title, text, icon = 'error') {
        return Swal.fire({
            icon,
            title,
            text,
            confirmButtonColor: '#0C2136'
        });
    }
});
