document.addEventListener('DOMContentLoaded', () => {
    const formReclamo = document.getElementById('formReclamo');
    const categoriaSelect = document.getElementById('categoria');
    const descripcionInput = document.getElementById('descripcion');
    const MIN_CARACTERES_DESCRIPCION = 15;

    if (!formReclamo) return;

    formReclamo.addEventListener('submit', async function (e) {
        e.preventDefault(); // Detener el envío por defecto

        const categoriaVal = categoriaSelect.value.trim();
        const descripcionVal = descripcionInput.value.trim();

        // 1. Validar categoría seleccionada
        if (!categoriaVal) {
            Swal.fire({
                icon: 'warning',
                title: 'Categoría requerida',
                text: 'Por favor, selecciona una categoría para clasificar tu reclamo.',
                confirmButtonColor: '#0C2136'
            });
            categoriaSelect.focus();
            return;
        }

        // 2. Validar longitud mínima de la descripción
        if (descripcionVal.length < MIN_CARACTERES_DESCRIPCION) {
            Swal.fire({
                icon: 'warning',
                title: 'Descripción insuficiente',
                text: `La descripción debe contener al menos ${MIN_CARACTERES_DESCRIPCION} caracteres. Actualmente tiene ${descripcionVal.length}.`,
                confirmButtonColor: '#0C2136'
            });
            descripcionInput.focus();
            return;
        }

        // 3. Disparar alerta de confirmación con SweetAlert2
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

        // 4. Si el vecino confirma, enviamos los datos al backend Spring Boot
        if (confirmacion.isConfirmed) {
            enviarReclamoAlServidor(new FormData(formReclamo));
        }
    });

    /**
     * Envía la petición POST con los datos del reclamo al backend en Spring Boot
     */
    async function enviarReclamoAlServidor(formData) {
        try {
            // Mostrar loader mientras se procesa la petición
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
                body: formData
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
                throw new Error('Error al guardar en el servidor');
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