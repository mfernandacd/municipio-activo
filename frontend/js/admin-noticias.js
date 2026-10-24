document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-noticia');
    const listado = document.getElementById('listado-admin-noticias');
    const estado = document.getElementById('estado-noticias-admin');
    const tituloFormulario = document.getElementById('titulo-formulario-noticia');
    const botonGuardar = document.getElementById('guardar-noticia');
    const botonCancelar = document.getElementById('cancelar-edicion');
    let noticiaEditando = null;
    let noticias = [];

    if (!form || !listado || !estado || !botonGuardar || !botonCancelar) return;

    inicializar().catch((error) => {
        console.error('No se pudo iniciar la administración de noticias.', error);
        mostrarEstado('No se pudo cargar el panel. Iniciá sesión con una cuenta administrativa.', true);
    });

    async function inicializar() {
        const usuario = await window.municipioSessionReady;
        if (usuario?.rol?.toLowerCase() !== 'administrativo') return;

        form.addEventListener('submit', guardarNoticia);
        botonCancelar.addEventListener('click', cancelarEdicion);
        await cargarNoticias();
    }

    async function cargarNoticias() {
        const resultado = await llamarApi('/api/noticias');
        noticias = resultado;
        listado.replaceChildren(...noticias.map(crearFilaNoticia));
    }

    async function guardarNoticia(event) {
        event.preventDefault();
        const datos = new FormData(form);
        const archivoImagen = datos.get('imageFile');
        const noticia = {
            category: datos.get('category'),
            title: datos.get('title'),
            summary: datos.get('summary'),
            content: datos.get('content'),
            image: datos.get('image'),
            imageAlt: datos.get('imageAlt'),
            featured: form.elements.featured.checked
        };

        try {
            if (archivoImagen instanceof File && archivoImagen.size > 5 * 1024 * 1024) {
                throw new Error('La imagen no puede superar los 5 MB.');
            }

            const cuerpo = new FormData();
            cuerpo.append('noticia', new Blob([JSON.stringify(noticia)], { type: 'application/json' }), 'noticia.json');
            if (archivoImagen instanceof File && archivoImagen.size > 0) {
                cuerpo.append('imageFile', archivoImagen);
            }

            await llamarApi(noticiaEditando
                ? `/api/noticias/${encodeURIComponent(noticiaEditando)}`
                : '/api/noticias', {
                method: noticiaEditando ? 'PUT' : 'POST',
                body: cuerpo
            });
            mostrarEstado(noticiaEditando ? 'Noticia actualizada.' : 'Noticia publicada.');
            cancelarEdicion();
            await cargarNoticias();
        } catch (error) {
            console.error('No se pudo guardar la noticia.', error);
            mostrarEstado(error.message, true);
        }
    }

    function crearFilaNoticia(noticia) {
        const tarjeta = document.createElement('article');
        tarjeta.className = 'card mb-3';
        const cuerpo = document.createElement('div');
        cuerpo.className = 'card-body d-flex flex-wrap justify-content-between align-items-center gap-3';

        const informacion = document.createElement('div');
        const titulo = document.createElement('h3');
        titulo.className = 'h5 mb-1';
        titulo.textContent = noticia.title;
        const descripcion = document.createElement('p');
        descripcion.className = 'mb-0 text-muted';
        descripcion.textContent = `${noticia.category}${noticia.featured ? ' · Destacada' : ''}`;
        informacion.append(titulo, descripcion);

        const acciones = document.createElement('div');
        acciones.className = 'd-flex gap-2';
        const editar = document.createElement('button');
        editar.type = 'button';
        editar.className = 'btn btn-outline-primary';
        editar.textContent = 'Modificar';
        editar.addEventListener('click', () => iniciarEdicion(noticia));

        const eliminar = document.createElement('button');
        eliminar.type = 'button';
        eliminar.className = 'btn btn-outline-danger';
        eliminar.textContent = 'Eliminar';
        eliminar.addEventListener('click', () => eliminarNoticia(noticia));

        acciones.append(editar, eliminar);
        cuerpo.append(informacion, acciones);
        tarjeta.append(cuerpo);
        return tarjeta;
    }

    function iniciarEdicion(noticia) {
        noticiaEditando = noticia.id;
        form.elements.category.value = noticia.category;
        form.elements.title.value = noticia.title;
        form.elements.summary.value = noticia.summary;
        form.elements.content.value = noticia.content;
        form.elements.image.value = noticia.image || '';
        form.elements.imageFile.value = '';
        form.elements.imageAlt.value = noticia.imageAlt || '';
        form.elements.featured.checked = noticia.featured;
        tituloFormulario.textContent = 'Modificar noticia';
        botonGuardar.textContent = 'Guardar cambios';
        botonCancelar.classList.remove('d-none');
        form.scrollIntoView({ behavior: 'smooth', block: 'start' });
        form.elements.title.focus();
    }

    function cancelarEdicion() {
        noticiaEditando = null;
        form.reset();
        tituloFormulario.textContent = 'Crear una noticia';
        botonGuardar.textContent = 'Publicar noticia';
        botonCancelar.classList.add('d-none');
    }

    async function eliminarNoticia(noticia) {
        if (!window.confirm(`¿Querés eliminar la noticia "${noticia.title}"?`)) return;

        try {
            await llamarApi(`/api/noticias/${encodeURIComponent(noticia.id)}`, { method: 'DELETE' });
            mostrarEstado('Noticia eliminada.');
            if (noticiaEditando === noticia.id) cancelarEdicion();
            await cargarNoticias();
        } catch (error) {
            console.error('No se pudo eliminar la noticia.', error);
            mostrarEstado(error.message, true);
        }
    }

    async function llamarApi(endpoint, options = {}) {
        const headers = {
            Accept: 'application/json',
            ...options.headers
        };
        if (options.body && !(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        const response = await window.municipioFetch(resolverApiUrl(endpoint), {
            ...options,
            headers,
            credentials: 'include'
        });

        if (response.status === 204) return null;
        const body = await response.json().catch(() => ({}));
        if (!response.ok) {
            let errorMsg = body.mensaje;
            if (!errorMsg) {
                if (response.status === 403) {
                    errorMsg = 'Acceso denegado (Error 403). Tu sesión puede haber expirado o el servidor rechazó el origen de la petición. Cerrá sesión y volvé a ingresar.';
                } else if (response.status === 401) {
                    errorMsg = 'Sesión requerida (Error 401). Iniciá sesión con una cuenta administrativa.';
                } else {
                    errorMsg = `La API respondió con el error ${response.status}.`;
                }
            }
            throw new Error(errorMsg);
        }
        return body;
    }

    function mostrarEstado(mensaje, esError = false) {
        estado.textContent = mensaje;
        estado.classList.toggle('text-danger', esError);
        estado.classList.toggle('text-success', !esError);
    }

    function resolverApiUrl(endpoint) {
        return window.municipioApiUrl(endpoint, 'municipio-api-base-url');
    }
});
