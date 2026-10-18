document.addEventListener('DOMContentLoaded', () => {
    cargarNoticias().catch((error) => {
        console.error('No se pudieron cargar las noticias.', error);
        const lista = document.getElementById('lista-noticias');
        const detalle = document.getElementById('noticia-detalle-contenido');
        const aviso = document.createElement('p');
        aviso.className = 'alert alert-warning';
        aviso.textContent = 'No se pudieron cargar las noticias. Intentá nuevamente más tarde.';
        (lista || detalle)?.replaceChildren(aviso);
    });

    async function cargarNoticias() {
        const response = await fetch(resolverApiUrl('/api/noticias'));
        if (!response.ok) {
            throw new Error(`La API respondió con el error ${response.status}.`);
        }

        const noticias = await response.json();
        const noticiaDestacada = noticias.find((noticia) => noticia.featured) || noticias[0];
        mostrarDestacada(noticiaDestacada);
        mostrarListado(noticias);
        mostrarDetalle(noticias);
    }

    function mostrarDestacada(noticia) {
        if (!noticia) return;

        const imagen = document.getElementById('noticia-destacada-imagen');
        const titulo = document.getElementById('noticia-destacada-titulo');
        const enlace = document.getElementById('noticia-destacada-enlace');
        if (imagen) {
            imagen.src = noticia.image || 'assets/imagenes/Logo.jpeg';
            imagen.alt = noticia.imageAlt || noticia.title;
        }
        if (titulo) titulo.textContent = noticia.title;
        if (enlace) enlace.href = `noticia-detalle.html?id=${encodeURIComponent(noticia.id)}`;
    }

    function mostrarListado(noticias) {
        const lista = document.getElementById('lista-noticias');
        if (lista) {
            lista.replaceChildren(...noticias.map(crearTarjetaNoticia));
        }

        const secundarias = document.getElementById('noticias-secundarias');
        if (secundarias) {
            const destacada = noticias.find((noticia) => noticia.featured) || noticias[0];
            secundarias.replaceChildren(...noticias
                .filter((noticia) => noticia !== destacada)
                .map(crearTarjetaNoticia));
        }
    }

    function crearTarjetaNoticia(noticia) {
        const columna = document.createElement('article');
        columna.className = 'col';

        const tarjeta = document.createElement('div');
        tarjeta.className = 'card h-100 shadow-sm';

        const imagen = document.createElement('img');
        imagen.className = 'card-img-top';
        imagen.src = noticia.image || 'assets/imagenes/Logo.jpeg';
        imagen.alt = noticia.imageAlt || noticia.title;

        const cuerpo = document.createElement('div');
        cuerpo.className = 'card-body';

        const categoria = document.createElement('p');
        categoria.className = 'noticias-etiqueta mb-1';
        categoria.textContent = noticia.category;

        const titulo = document.createElement('h3');
        titulo.className = 'card-title h5';
        titulo.textContent = noticia.title;

        const resumen = document.createElement('p');
        resumen.className = 'card-text';
        resumen.textContent = noticia.summary;

        const enlace = document.createElement('a');
        enlace.className = 'btn btn-outline-primary';
        enlace.href = `noticia-detalle.html?id=${encodeURIComponent(noticia.id)}`;
        enlace.textContent = 'Leer noticia';
        enlace.setAttribute('aria-label', `Leer noticia: ${noticia.title}`);

        cuerpo.append(categoria, titulo, resumen, enlace);
        tarjeta.append(imagen, cuerpo);
        columna.append(tarjeta);
        return columna;
    }

    function mostrarDetalle(noticias) {
        const contenedor = document.getElementById('noticia-detalle-contenido');
        if (!contenedor) return;

        const id = new URLSearchParams(window.location.search).get('id');
        const noticia = noticias.find((item) => item.id === id);
        if (!noticia) {
            const aviso = document.createElement('p');
            aviso.className = 'alert alert-info';
            aviso.textContent = 'No se encontró la noticia solicitada.';
            contenedor.replaceChildren(aviso);
            return;
        }

        const articulo = document.createElement('article');
        const imagen = document.createElement('img');
        imagen.className = 'img-fluid rounded mb-4';
        imagen.src = noticia.image || 'assets/imagenes/Logo.jpeg';
        imagen.alt = noticia.imageAlt || noticia.title;

        const categoria = document.createElement('p');
        categoria.className = 'noticias-etiqueta mb-1';
        categoria.textContent = noticia.category;

        const titulo = document.createElement('h1');
        titulo.className = 'fw-bold mb-3';
        titulo.textContent = noticia.title;

        const resumen = document.createElement('p');
        resumen.className = 'lead';
        resumen.textContent = noticia.summary;

        const contenido = document.createElement('div');
        contenido.className = 'noticia-contenido';
        noticia.content.split(/\r?\n\s*\r?\n/).forEach((texto) => {
            if (!texto.trim()) return;
            const parrafo = document.createElement('p');
            parrafo.textContent = texto;
            contenido.append(parrafo);
        });

        articulo.append(imagen, categoria, titulo, resumen, contenido);
        contenedor.replaceChildren(articulo);
    }

    function resolverApiUrl(endpoint) {
        const configuredBaseUrl = document.querySelector('meta[name="municipio-api-base-url"]')
            ?.content.trim();
        const baseUrl = configuredBaseUrl || (
            ['localhost', '127.0.0.1'].includes(window.location.hostname)
                ? `${window.location.protocol}//${window.location.hostname}:8080`
                : window.location.origin
        );
        return new URL(endpoint, `${baseUrl.replace(/\/+$/, '')}/`).toString();
    }
});
