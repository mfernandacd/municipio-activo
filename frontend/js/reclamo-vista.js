document.addEventListener('DOMContentLoaded', async () => {
    const tbody = document.getElementById('tbodyReclamos');
    const tabla = document.getElementById('tablaReclamos');
    const btnRefrescar = document.getElementById('btnRefrescar');
    const greetingEl = document.getElementById('adminUserGreeting');
    const btnCerrarSesion = document.getElementById('btnCerrarSesion');

    if (!tbody || !tabla || !btnRefrescar) return;

    try {
        const response = await window.municipioFetch(resolverApiUrl('/api/auth/session'), {
            credentials: 'include'
        });

        if (response.status === 401) {
            localStorage.removeItem('usuarioMunicipio');
            await mostrarAviso('warning', 'Acceso restringido', 'Iniciá sesión para acceder a este panel.');
            window.location.href = 'login.html';
            return;
        }
        if (!response.ok) {
            throw new Error(`No se pudo validar la sesión (código ${response.status}).`);
        }

        const usuario = await response.json();
        if (String(usuario.rol || '').trim().toLowerCase() !== 'administrativo') {
            await mostrarAviso('error', 'Permisos insuficientes', 'Este panel es exclusivo para usuarios administrativos.');
            window.location.href = 'index.html';
            return;
        }

        document.querySelectorAll('[data-admin-only]').forEach((element) => {
            element.hidden = false;
        });

        if (greetingEl) {
            greetingEl.textContent = `Administrativo: ${usuario.nombre || usuario.email}`;
        }
    } catch (error) {
        console.error('No se pudo validar el acceso al panel.', error);
        await mostrarAviso('error', 'No se pudo validar el acceso', 'Verificá la conexión con el servidor e intentá nuevamente.');
        window.location.href = 'login.html';
        return;
    }

    btnCerrarSesion?.addEventListener('click', cerrarSesion);
    btnRefrescar.addEventListener('click', cargarReclamos);
    tbody.addEventListener('click', guardarEstadoDesdeFila);

    await cargarReclamos();

    async function cargarReclamos() {
        tbody.setAttribute('aria-busy', 'true');
        btnRefrescar.disabled = true;
        mostrarMensaje('Cargando reclamos...', 'text-muted');

        try {
            const response = await window.municipioFetch(resolverApiUrl('/api/reclamos'), {
                method: 'GET',
                credentials: 'include',
                headers: { Accept: 'application/json' }
            });

            if (response.status === 401 || response.status === 403) {
                throw new Error('La sesión no tiene permisos para consultar los reclamos. Volvé a iniciar sesión con una cuenta administrativa.');
            }
            if (!response.ok) {
                throw new Error(`No se pudieron obtener los reclamos (código ${response.status}).`);
            }

            const reclamos = await response.json();
            if (!Array.isArray(reclamos)) {
                throw new Error('El servidor devolvió una respuesta inválida para la lista de reclamos.');
            }
            renderizarTabla(reclamos);
        } catch (error) {
            console.error('Error al cargar los reclamos.', error);
            mostrarMensaje(error.message || 'No se pudieron cargar los reclamos del servidor.', 'text-danger');
        } finally {
            tbody.setAttribute('aria-busy', 'false');
            btnRefrescar.disabled = false;
        }
    }

    function renderizarTabla(reclamos) {
        tbody.replaceChildren();
        if (reclamos.length === 0) {
            mostrarMensaje('No hay reclamos registrados en el sistema actualmente.', 'text-muted');
            return;
        }

        reclamos.forEach((reclamo) => {
            const fila = document.createElement('tr');
            const estado = normalizarEstado(reclamo.estado);

            const id = document.createElement('td');
            id.className = 'text-center fw-bold text-primary';
            id.textContent = `#${reclamo.id ?? '—'}`;

            const categoria = document.createElement('td');
            const categoriaBadge = document.createElement('span');
            categoriaBadge.className = 'badge bg-secondary';
            categoriaBadge.textContent = reclamo.categoria || 'General';
            categoria.appendChild(categoriaBadge);

            const direccion = document.createElement('td');
            direccion.textContent = reclamo.direccion || 'Sin dirección especificada';

            const celdaEstado = document.createElement('td');
            celdaEstado.className = 'text-center';
            const estadoBadge = document.createElement('span');
            estadoBadge.className = `badge badge-estado ${claseEstado(estado)}`;
            estadoBadge.textContent = etiquetaEstado(estado);
            celdaEstado.appendChild(estadoBadge);

            const acciones = document.createElement('td');
            acciones.className = 'text-center';
            const controles = document.createElement('div');
            controles.className = 'd-flex align-items-center justify-content-center gap-2';

            const selector = document.createElement('select');
            selector.className = 'form-select form-select-sm select-estado';
            selector.dataset.id = reclamo.id;
            selector.setAttribute('aria-label', `Nuevo estado para el reclamo ${reclamo.id}`);
            [
                ['PENDIENTE', 'Pendiente'],
                ['EN_PROCESO', 'En proceso'],
                ['RESUELTO', 'Resuelto']
            ].forEach(([value, label]) => {
                const opcion = document.createElement('option');
                opcion.value = value;
                opcion.textContent = label;
                opcion.selected = value === estado || (estado === 'REALIZADO' && value === 'RESUELTO');
                selector.appendChild(opcion);
            });

            const botonGuardar = document.createElement('button');
            botonGuardar.type = 'button';
            botonGuardar.className = 'btn btn-sm btn-success btn-guardar-estado';
            botonGuardar.dataset.action = 'save';
            botonGuardar.dataset.id = reclamo.id;
            botonGuardar.title = 'Guardar cambio de estado';
            botonGuardar.setAttribute('aria-label', `Guardar estado del reclamo ${reclamo.id}`);
            const icono = document.createElement('i');
            icono.className = 'fa-solid fa-check';
            icono.setAttribute('aria-hidden', 'true');
            botonGuardar.appendChild(icono);

            controles.append(selector, botonGuardar);
            acciones.appendChild(controles);
            fila.append(id, categoria, direccion, celdaEstado, acciones);
            tbody.appendChild(fila);
        });
    }

    async function guardarEstadoDesdeFila(event) {
        const boton = event.target.closest('button[data-action="save"]');
        if (!boton || !tbody.contains(boton)) return;

        const id = boton.dataset.id;
        const selector = boton.closest('tr')?.querySelector('.select-estado');
        if (!id || !selector) return;

        const estado = selector.value;
        const controles = boton.closest('tr').querySelectorAll('button, select');
        controles.forEach((control) => { control.disabled = true; });

        try {
            const response = await window.municipioFetch(resolverApiUrl(`/api/reclamos/${encodeURIComponent(id)}/estado`), {
                method: 'PUT',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                body: JSON.stringify({ estado })
            });

            if (response.status === 401 || response.status === 403) {
                throw new Error('No tenés permisos para modificar reclamos. Iniciá sesión con una cuenta administrativa.');
            }
            if (!response.ok) {
                throw new Error(`No se pudo guardar el nuevo estado (código ${response.status}).`);
            }

            await mostrarAviso('success', 'Estado actualizado', `El reclamo #${id} ahora figura como ${etiquetaEstado(estado)}.`);
            await cargarReclamos();
        } catch (error) {
            console.error('Error al actualizar el estado del reclamo.', error);
            await mostrarAviso('error', 'Error de actualización', error.message || 'No se pudo actualizar el estado del reclamo.');
            controles.forEach((control) => { control.disabled = false; });
        }
    }

    async function cerrarSesion() {
        btnCerrarSesion.disabled = true;
        try {
            const response = await window.municipioFetch(resolverApiUrl('/api/auth/logout'), {
                method: 'POST',
                credentials: 'include'
            });
            if (!response.ok) {
                throw new Error(`No se pudo cerrar la sesión (código ${response.status}).`);
            }
            localStorage.removeItem('usuarioMunicipio');
            window.location.href = 'login.html';
        } catch (error) {
            console.error('Error al cerrar sesión.', error);
            await mostrarAviso('error', 'No se pudo cerrar la sesión', 'Verificá la conexión con el servidor e intentá nuevamente.');
            btnCerrarSesion.disabled = false;
        }
    }

    function mostrarMensaje(mensaje, clase) {
        const fila = document.createElement('tr');
        const celda = document.createElement('td');
        celda.colSpan = 5;
        celda.className = `text-center py-4 ${clase}`;
        celda.textContent = mensaje;
        fila.appendChild(celda);
        tbody.replaceChildren(fila);
    }

    function normalizarEstado(estado) {
        return String(estado || 'PENDIENTE').trim().toUpperCase();
    }

    function claseEstado(estado) {
        if (estado === 'EN_PROCESO') return 'badge-en-proceso';
        if (estado === 'RESUELTO' || estado === 'REALIZADO') return 'badge-resuelto';
        return 'badge-pendiente';
    }

    function etiquetaEstado(estado) {
        return ({
            PENDIENTE: 'Pendiente',
            EN_PROCESO: 'En proceso',
            RESUELTO: 'Resuelto',
            REALIZADO: 'Resuelto'
        })[estado] || estado.replaceAll('_', ' ');
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
