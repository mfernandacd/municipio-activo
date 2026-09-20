document.addEventListener('DOMContentLoaded', () => {
    const formReclamo = document.getElementById('formReclamo');
    const categoriaSelect = document.getElementById('categoria');
    const descripcionInput = document.getElementById('descripcion');
    const direccionInput = document.getElementById('direccion');
    const googleMapsInput = document.getElementById('googleMapsUrl');
    const imagenPathInput = document.getElementById('imagenPath');

    const MIN_CARACTERES_DESCRIPCION = 15;

    if (!formReclamo) return;

    formReclamo.addEventListener('submit', async function (e) {
        e.preventDefault(); // Detener el envío predeterminado

        const categoriaVal = categoriaSelect ? categoriaSelect.value.trim() : '';
        const descripcionVal = descripcionInput ? descripcionInput.value.trim() : '';

        // 1. Validar que se haya seleccionado una categoría
        if (!categoriaVal) {
            Swal.fire({
                icon: 'warning',
                title: 'Categoría requerida',
                text: 'Por favor, selecciona una categoría para clasificar tu reclamo.',
                confirmButtonColor: '#0C2136'
            });
            if (categoriaSelect) categoriaSelect.focus();
            return;
        }

        // 2. Validar que la descripción tenga al menos el mínimo de caracteres requeridos
        if (descripcionVal.length < MIN_CARACTERES_DESCRIPCION) {
            Swal.fire({
                icon: 'warning',
                title: 'Descripción insuficiente',
                text: `La descripción debe contener al menos ${MIN_CARACTERES_DESCRIPCION} caracteres. Actualmente tiene ${descripcionVal.length}.`,
                confirmButtonColor: '#0C2136'
            });
            if (descripcionInput) descripcionInput.focus();
            return;
        }

        // 3. Disparar alerta de confirmación previa con SweetAlert2
        const confirmacion = await Swal.fire({
            title: '¿Confirmar envío del reporte?',
            text: 'Tu solicitud será registrada y enviada al municipio para su procesamiento.',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#0C2136',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Sí, enviar reclamo',
            cancelButtonText: 'Cancelar'
        });

        // 4. Si el vecino confirma, armar JSON y enviar al backend
        if (confirmacion.isConfirmed) {
            const payload = {
                categoria: categoriaVal,
                direccion: direccionInput ? direccionInput.value.trim() : '',
                googleMapsUrl: googleMapsInput ? googleMapsInput.value.trim() : '',
                descripcion: descripcionVal,
                imagenPath: imagenPathInput ? imagenPathInput.value.trim() : ''
            };

            enviarReclamoAlServidor(payload);
        }
    });

    /**
     * Envía la petición POST en formato JSON al endpoint REST de Spring Boot
     */
    async function enviarReclamoAlServidor(data) {
        try {
            Swal.fire({
                title: 'Guardando reclamo...',
                text: 'Por favor aguarde un momento.',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });

            const response = await fetch('/api/reclamos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                Swal.fire({
                    icon: 'success',
                    title: '¡Reclamo Registrado!',
                    text: 'Tu solicitud ha sido guardada exitosamente en el servidor.',
                    confirmButtonColor: '#0C2136'
                });
                formReclamo.reset();
            } else {
                throw new Error('Error en la respuesta del servidor');
            }
        } catch (error) {
            Swal.fire({
                icon: 'error',
                title: 'Error de servidor',
                text: 'No se pudo registrar el reclamo. Por favor, reintente más tarde.',
                confirmButtonColor: '#0C2136'
            });
        }
    }
});