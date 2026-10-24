document.addEventListener('DOMContentLoaded', () => {
    // Referencias principales del formulario y botones
    const claimForm = document.getElementById('claimForm') || document.getElementById('formReclamo');
    const submitBtn = document.getElementById('submitBtn');
    const btnFillExample = document.getElementById('btnFillExample');
    const btnResetAll = document.getElementById('btnResetAll');
    const btnCopyCode = document.getElementById('btnCopyCode');

    // Campos del formulario
    const userNameInput = document.getElementById('userName');
    const userEmailInput = document.getElementById('userEmail');
    const categoriaSelect = document.getElementById('claimCategory') || document.getElementById('categoria');
    const direccionInput = document.getElementById('claimAddress') || document.getElementById('direccion');
    const descripcionInput = document.getElementById('claimDescription') || document.getElementById('descripcion');
    const googleMapsInput = document.getElementById('googleMapsUrl');
    const imagenPathInput = document.getElementById('imagenPath');

    // Paneles de Servidor / BD
    const jsonOutput = document.getElementById('jsonOutput');
    const processingState = document.getElementById('processingState');
    const codeGeneratedBadge = document.getElementById('codeGeneratedBadge');

    // Bandeja de Entrada de Mail Simulado
    const mailEmptyState = document.getElementById('mailEmptyState');
    const mailContent = document.getElementById('mailContent');
    const mailTo = document.getElementById('mailTo');
    const mailSubject = document.getElementById('mailSubject');
    const mailTimestamp = document.getElementById('mailTimestamp');
    const mailBodyName = document.getElementById('mailBodyName');
    const mailTrackingCode = document.getElementById('mailTrackingCode');
    const mailBodyCategory = document.getElementById('mailBodyCategory');
    const mailBodyDesc = document.getElementById('mailBodyDesc');
    const mailCount = document.getElementById('mailCount');

    // Toast flotante
    const toast = document.getElementById('toast');
    const toastTitle = document.getElementById('toastTitle');
    const toastMessage = document.getElementById('toastMessage');

    const MIN_CARACTERES_DESCRIPCION = 15;

    // Conectar botón para cargar datos de ejemplo
    if (btnFillExample) {
        btnFillExample.addEventListener('click', (e) => {
            e.preventDefault();
            cargarDatosEjemplo();
        });
    }

    // Conectar botón para reiniciar simulación
    if (btnResetAll) {
        btnResetAll.addEventListener('click', (e) => {
            e.preventDefault();
            reiniciarFormulario();
        });
    }

    // Conectar botón para copiar código de seguimiento
    if (btnCopyCode) {
        btnCopyCode.addEventListener('click', (e) => {
            e.preventDefault();
            copiarCodigoSeguimiento();
        });
    }

    // Manejar el envío del formulario
    if (claimForm) {
        claimForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const nombreVal = userNameInput ? userNameInput.value.trim() : '';
            const emailVal = userEmailInput ? userEmailInput.value.trim() : '';
            const categoriaVal = categoriaSelect ? categoriaSelect.value.trim() : '';
            const direccionVal = direccionInput ? direccionInput.value.trim() : '';
            const descripcionVal = descripcionInput ? descripcionInput.value.trim() : '';

            // 1. Validar Categoría
            if (!categoriaVal) {
                await mostrarAlerta('Categoría requerida', 'Por favor, selecciona una categoría para clasificar tu reclamo.', 'warning');
                if (categoriaSelect) categoriaSelect.focus();
                return;
            }

            // 2. Validar Descripción mínima
            if (descripcionVal.length < MIN_CARACTERES_DESCRIPCION) {
                await mostrarAlerta(
                    'Descripción insuficiente',
                    `La descripción debe contener al menos ${MIN_CARACTERES_DESCRIPCION} caracteres. Actualmente tiene ${descripcionVal.length}.`,
                    'warning'
                );
                if (descripcionInput) descripcionInput.focus();
                return;
            }

            // 3. Validar Email si se ingresa
            if (emailVal && !esEmailValido(emailVal)) {
                await mostrarAlerta('Correo inválido', 'Por favor, ingresá un correo electrónico válido.', 'warning');
                if (userEmailInput) userEmailInput.focus();
                return;
            }

            // 4. Modal de confirmación SweetAlert2
            const confirmacion = typeof Swal !== 'undefined'
                ? await Swal.fire({
                    title: '¿Confirmar envío del reporte?',
                    text: 'Tu solicitud será registrada en el Municipio de Serranoble y se generará tu código de seguimiento.',
                    icon: 'question',
                    showCancelButton: true,
                    confirmButtonColor: '#0A2240',
                    cancelButtonColor: '#7C4328',
                    confirmButtonText: 'Sí, enviar reclamo',
                    cancelButtonText: 'Cancelar'
                })
                : { isConfirmed: window.confirm('¿Desea enviar el reclamo?') };

            if (!confirmacion.isConfirmed) return;

            // 5. Armar payload JSON para el backend
            const payload = {
                categoria: categoriaVal,
                direccion: direccionVal || 'Ubicación informada en descripción',
                googleMapsUrl: googleMapsInput ? googleMapsInput.value.trim() : '',
                descripcion: descripcionVal,
                imagenPath: imagenPathInput ? imagenPathInput.value.trim() : '',
                userName: nombreVal || 'Vecino Serranoble',
                userEmail: emailVal || 'vecino@serranoble.gob.ar'
            };

            await enviarReclamoAlServidor(payload);
        });
    }

    /**
     * Envía la petición POST en formato JSON al backend de Spring Boot
     */
    async function enviarReclamoAlServidor(payload) {
        establecerEstadoCarga(true);

        try {
            const endpoint = resolverApiUrl('/api/reclamos');
            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorTexto = await response.text();
                let errorMsg = `Error en el servidor (código ${response.status})`;
                try {
                    const errorJson = JSON.parse(errorTexto);
                    if (errorJson.message) errorMsg = errorJson.message;
                    if (errorJson.detail) errorMsg = errorJson.detail;
                } catch {
                    if (errorTexto && errorTexto.length < 120) errorMsg = errorTexto;
                }
                throw new Error(errorMsg);
            }

            const reclamoGuardado = await response.json();
            const trackingCode = reclamoGuardado.codigoSeguimiento || 'SER-2026-' + Math.floor(1000 + Math.random() * 9000);
            const fechaHora = reclamoGuardado.fechaCreacion
                ? new Date(reclamoGuardado.fechaCreacion).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

            // 1. Actualizar consola de Servidor / BD
            if (jsonOutput) {
                const dbRecord = {
                    municipio: "Serranoble",
                    tabla: "reclamos",
                    operacion: "INSERT",
                    id: reclamoGuardado.id || 1,
                    codigo_seguimiento: trackingCode,
                    vecino: {
                        nombre: payload.userName,
                        email: payload.userEmail
                    },
                    categoria: reclamoGuardado.categoria || payload.categoria,
                    direccion: reclamoGuardado.direccion || payload.direccion,
                    descripcion: reclamoGuardado.descripcion || payload.descripcion,
                    estado: reclamoGuardado.estado || "PENDIENTE",
                    creado_el: reclamoGuardado.fechaCreacion || new Date().toISOString(),
                    notificacion_mail_enviada: true
                };
                jsonOutput.textContent = JSON.stringify(dbRecord, null, 2);
            }

            if (codeGeneratedBadge) {
                codeGeneratedBadge.classList.remove('hidden');
            }

            // 2. Actualizar Bandeja de Entrada de Mail Simulado
            if (mailTo) mailTo.textContent = `${payload.userName} <${payload.userEmail}>`;
            if (mailSubject) mailSubject.textContent = `Confirmación Reclamo Serranoble [${trackingCode}]`;
            if (mailTimestamp) mailTimestamp.textContent = fechaHora;
            if (mailBodyName) mailBodyName.textContent = payload.userName;
            if (mailTrackingCode) mailTrackingCode.textContent = trackingCode;
            if (mailBodyCategory) mailBodyCategory.textContent = payload.categoria;
            if (mailBodyDesc) mailBodyDesc.textContent = `"${payload.descripcion}"`;

            if (mailEmptyState) mailEmptyState.classList.add('hidden');
            if (mailContent) mailContent.classList.remove('hidden');
            if (mailCount) mailCount.textContent = '1 mensaje recibido';

            // 3. Notificación SweetAlert2
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'success',
                    title: '¡Reclamo Registrado en Serranoble!',
                    html: `<p class="mb-2">Tu reclamo fue guardado exitosamente en el servidor.</p>
                           <p class="font-mono font-bold text-lg text-serranoble-navy">Código: <strong>${trackingCode}</strong></p>
                           <p class="text-xs text-slate-500 mt-1">Se ha generado la notificación oficial para <strong>${payload.userEmail}</strong>.</p>`,
                    confirmButtonColor: '#0A2240',
                    confirmButtonText: 'Aceptar'
                });
            }

            // 4. Toast flotante
            mostrarToast('¡Reclamo Registrado!', `Código ${trackingCode} generado y despachado.`);

        } catch (error) {
            console.error('Error al registrar reclamo:', error);
            await mostrarAlerta(
                'No se pudo registrar el reclamo',
                error instanceof TypeError
                    ? 'No se pudo conectar con el servidor. Verificá que la API esté activa y la conexión a red.'
                    : (error.message || 'Ocurrió un error inesperado al procesar la solicitud.'),
                'error'
            );
        } finally {
            establecerEstadoCarga(false);
        }
    }

    function establecerEstadoCarga(cargando) {
        if (submitBtn) {
            submitBtn.disabled = cargando;
            if (cargando) {
                submitBtn.classList.add('opacity-75', 'cursor-not-allowed');
                submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> <span>Registrando en Serranoble...</span>';
            } else {
                submitBtn.classList.remove('opacity-75', 'cursor-not-allowed');
                submitBtn.innerHTML = '<i class="fa-solid fa-paper-plane"></i> <span>Enviar Reclamo a Serranoble</span>';
            }
        }
        if (processingState) {
            if (cargando) {
                processingState.classList.remove('hidden');
            } else {
                processingState.classList.add('hidden');
            }
        }
    }

    function cargarDatosEjemplo() {
        if (userNameInput) userNameInput.value = 'Roberto Soria';
        if (userEmailInput) userEmailInput.value = 'roberto.soria@ejemplo.com';
        if (categoriaSelect) categoriaSelect.value = 'Alumbrado y Luminarias Serranas';
        if (direccionInput) direccionInput.value = 'Calle Los Cerros esquina Camino del Sol';
        if (descripcionInput) descripcionInput.value = 'Falta de iluminación pública en la calle Los Cerros esquina Camino del Sol. Varias luminarias LED fuera de servicio.';

        mostrarToast('Datos cargados', 'Se completó el formulario con un reclamo de ejemplo.');
    }

    function reiniciarFormulario() {
        if (claimForm) claimForm.reset();
        if (jsonOutput) {
            jsonOutput.textContent = '// Esperando ingreso de reclamo...\n{\n  "municipio": "Serranoble",\n  "status": "idle",\n  "message": "Ningún reclamo enviado aún."\n}';
        }
        if (codeGeneratedBadge) codeGeneratedBadge.classList.add('hidden');
        if (processingState) processingState.classList.add('hidden');

        if (mailEmptyState) mailEmptyState.classList.remove('hidden');
        if (mailContent) mailContent.classList.add('hidden');
        if (mailCount) mailCount.textContent = '0 mensajes';

        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.classList.remove('opacity-75', 'cursor-not-allowed');
            submitBtn.innerHTML = '<i class="fa-solid fa-paper-plane"></i> <span>Enviar Reclamo a Serranoble</span>';
        }

        mostrarToast('Simulador Reiniciado', 'Podés realizar un nuevo reclamo para Serranoble.');
    }

    function copiarCodigoSeguimiento() {
        const code = mailTrackingCode ? mailTrackingCode.textContent.trim() : '';
        if (!code || code === '---') return;

        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(code).then(() => {
                mostrarToast('Código Copiado', `${code} guardado en el portapapeles.`);
            }).catch(() => {
                copiarConFallback(code);
            });
        } else {
            copiarConFallback(code);
        }
    }

    function copiarConFallback(code) {
        const textarea = document.createElement('textarea');
        textarea.value = code;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand('copy');
            mostrarToast('Código Copiado', `${code} guardado en el portapapeles.`);
        } catch {
            mostrarToast('Atención', 'No se pudo copiar automáticamente.');
        } finally {
            document.body.removeChild(textarea);
        }
    }

    function mostrarToast(title, message) {
        if (!toast || !toastTitle || !toastMessage) return;
        toastTitle.textContent = title;
        toastMessage.textContent = message;

        toast.classList.remove('translate-y-20', 'opacity-0');
        toast.classList.add('translate-y-0', 'opacity-100');

        setTimeout(() => {
            toast.classList.remove('translate-y-0', 'opacity-100');
            toast.classList.add('translate-y-20', 'opacity-0');
        }, 4000);
    }

    function mostrarAlerta(title, text, icon = 'info') {
        if (typeof Swal !== 'undefined') {
            return Swal.fire({
                icon,
                title,
                text,
                confirmButtonColor: '#0A2240',
                confirmButtonText: 'Aceptar'
            });
        }
        alert(`${title}: ${text}`);
        return Promise.resolve();
    }

    function esEmailValido(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function resolverApiUrl(endpoint) {
        const configuredBaseUrl = document.querySelector('meta[name="reclamos-api-base-url"]')?.content.trim()
            || document.querySelector('meta[name="municipio-api-base-url"]')?.content.trim();

        if (window.location.protocol === 'file:' && !configuredBaseUrl) {
            console.warn('Ejecutando desde protocolo file://. Las llamadas a API pueden requerir un servidor web local.');
        }

        if (configuredBaseUrl) {
            return new URL(endpoint.replace(/^\/+/, ''), `${configuredBaseUrl.replace(/\/+$/, '')}/`).toString();
        }

        if (window.location.port === '8080') {
            return endpoint;
        }

        if (['localhost', '127.0.0.1'].includes(window.location.hostname)) {
            return `${window.location.protocol}//${window.location.hostname}:8080${endpoint}`;
        }

        return endpoint;
    }

    // Exponer helpers en window para accesibilidad si es necesario
    window.reclamoApp = {
        cargarDatosEjemplo,
        reiniciarFormulario,
        copiarCodigoSeguimiento
    };
});
