document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-noticia');
    const list = document.getElementById('listado-admin-noticias');
    const status = document.getElementById('estado-noticias-admin');
    const heading = document.getElementById('titulo-formulario-noticia');
    const submitButton = document.getElementById('guardar-noticia');
    const cancelButton = document.getElementById('cancelar-edicion');
    let editingId = null;

    if (!form || !list || !status || !heading || !submitButton || !cancelButton || !window.NoticiasStore) {
        console.error('No se pudo inicializar el administrador de noticias.');
        return;
    }

    const field = (name) => form.elements.namedItem(name);

    function showStatus(message, isError = false) {
        status.textContent = message;
        status.className = isError ? 'mt-3 mb-0 text-danger' : 'mt-3 mb-0 text-success';
    }

    function renderList() {
        const news = window.NoticiasStore.read();
        list.replaceChildren();
        if (!news.length) {
            const empty = document.createElement('p');
            empty.textContent = 'No hay noticias publicadas.';
            list.append(empty);
            return;
        }

        news.forEach((item) => {
            const row = document.createElement('article');
            row.className = 'admin-noticia-fila';
            const info = document.createElement('div');
            const title = document.createElement('h3');
            title.className = 'h5 fw-bold mb-1';
            title.textContent = item.title;
            const description = document.createElement('p');
            description.className = 'mb-0';
            description.textContent = `${item.category}${item.featured ? ' · Noticia principal' : ''}`;
            info.append(title, description);

            const actions = document.createElement('div');
            actions.className = 'd-flex flex-wrap gap-2';
            const edit = document.createElement('button');
            edit.type = 'button';
            edit.className = 'btn btn-outline-primary btn-sm';
            edit.dataset.action = 'edit';
            edit.dataset.id = item.id;
            edit.textContent = 'Editar';
            const remove = document.createElement('button');
            remove.type = 'button';
            remove.className = 'btn btn-outline-danger btn-sm';
            remove.dataset.action = 'delete';
            remove.dataset.id = item.id;
            remove.textContent = 'Eliminar';
            actions.append(edit, remove);
            row.append(info, actions);
            list.append(row);
        });
    }

    function resetForm() {
        form.reset();
        editingId = null;
        heading.textContent = 'Crear una noticia';
        submitButton.textContent = 'Publicar noticia';
        cancelButton.classList.add('d-none');
    }

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        const title = field('title').value.trim();
        const category = field('category').value;
        const summary = field('summary').value.trim();
        const content = field('content').value.trim();
        const image = field('image').value.trim();
        const imageAlt = field('imageAlt').value.trim();
        const featured = field('featured').checked;

        if (!title || !summary || !content) {
            showStatus('Completá el título, el resumen y el contenido de la noticia.', true);
            return;
        }
        if (!window.NoticiasStore.isValidImageSource(image)) {
            showStatus('La imagen debe ser una URL HTTP/HTTPS o una ruta dentro de assets/.', true);
            field('image').focus();
            return;
        }

        try {
            const news = window.NoticiasStore.read();
            const wasEditing = Boolean(editingId);
            const id = editingId || `noticia-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
            const updated = {
                id,
                title,
                category,
                summary,
                content,
                image: window.NoticiasStore.imageSource(image),
                imageAlt: imageAlt || title,
                featured
            };
            let next;
            if (editingId) {
                next = news.map((item) => item.id === editingId ? updated : item);
            } else {
                next = [updated, ...news];
            }
            if (featured) {
                next = next.map((item) => ({ ...item, featured: item.id === id }));
            }

            window.NoticiasStore.write(next);
            resetForm();
            renderList();
            showStatus(wasEditing ? 'Noticia actualizada.' : 'Noticia publicada.');
        } catch (error) {
            console.error('No se pudo guardar la noticia.', error);
            showStatus('No se pudo guardar la noticia en este navegador. Revisá el almacenamiento disponible.', true);
        }
    });

    cancelButton.addEventListener('click', () => {
        resetForm();
        showStatus('Edición cancelada.');
    });

    list.addEventListener('click', (event) => {
        const button = event.target.closest('button[data-action][data-id]');
        if (!button) return;

        try {
            const news = window.NoticiasStore.read();
            const item = news.find((candidate) => candidate.id === button.dataset.id);
            if (!item) {
                showStatus('La noticia ya no existe. Actualizá el listado e intentá nuevamente.', true);
                renderList();
                return;
            }

            if (button.dataset.action === 'edit') {
                editingId = item.id;
                field('title').value = item.title;
                field('category').value = item.category;
                field('summary').value = item.summary;
                field('content').value = item.content;
                field('image').value = item.image === 'assets/imagenes/Logo.jpeg' ? '' : item.image;
                field('imageAlt').value = item.imageAlt || '';
                field('featured').checked = Boolean(item.featured);
                heading.textContent = 'Modificar noticia';
                submitButton.textContent = 'Guardar cambios';
                cancelButton.classList.remove('d-none');
                field('title').focus();
                return;
            }

            if (button.dataset.action === 'delete' && window.confirm(`¿Eliminar la noticia “${item.title}”?`)) {
                const remainingNews = news.filter((candidate) => candidate.id !== item.id);
                if (item.featured && remainingNews.length && !remainingNews.some((candidate) => candidate.featured)) {
                    remainingNews[0].featured = true;
                }
                window.NoticiasStore.write(remainingNews);
                if (editingId === item.id) resetForm();
                renderList();
                showStatus('Noticia eliminada.');
            }
        } catch (error) {
            console.error('No se pudo modificar el listado de noticias.', error);
            showStatus('No se pudo actualizar el listado de noticias. Revisá el almacenamiento de este navegador.', true);
        }
    });

    try {
        renderList();
    } catch (error) {
        console.error('No se pudieron cargar las noticias para administrar.', error);
        showStatus('No se pudieron cargar las noticias de este navegador.', true);
    }
});
