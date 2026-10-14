(() => {
    const storageKey = 'municipio-activo-noticias-v1';
    const defaultImage = 'assets/imagenes/Logo.jpeg';
    const categories = {
        'La Muni Más Cerca': 'proximidad',
        Actividades: 'actividades',
        Gestión: 'gestion',
        'Gestión municipal': 'gestion',
        Comunidad: 'comunidad'
    };
    const seedNews = [
        {
            id: 'agenda-semanal',
            category: 'Gestión municipal',
            title: 'La Municipalidad despliega una agenda semanal con capacitaciones y acciones en terreno',
            summary: 'La agenda incluye capacitaciones, servicios en terreno y nuevas acciones de fortalecimiento barrial para toda la comunidad.',
            content: 'La Municipalidad despliega una agenda semanal con capacitaciones, servicios en terreno y nuevas acciones de fortalecimiento barrial para toda la comunidad.',
            image: defaultImage,
            imageAlt: 'Emblema del Municipio Serranoble',
            featured: true
        },
        {
            id: 'muni-mas-cerca-los-fresnos',
            category: 'La Muni Más Cerca',
            title: 'La Muni Más Cerca llegó al barrio Los Fresnos en su edición 111',
            summary: 'El operativo acercó servicios municipales y atención a las familias del barrio.',
            content: 'La edición 111 de La Muni Más Cerca llegó al barrio Los Fresnos. El operativo acercó servicios municipales y atención a las familias del barrio.',
            image: defaultImage,
            imageAlt: 'Emblema del Municipio Serranoble',
            featured: false
        },
        {
            id: 'agenda-actividades',
            category: 'Actividades',
            title: 'La Municipalidad presenta la agenda de actividades para esta semana',
            summary: 'Conocé las propuestas y actividades abiertas para disfrutar en la comunidad.',
            content: 'La Municipalidad presenta nuevas propuestas y actividades abiertas para compartir esta semana en la comunidad.\n\nConsultá los canales oficiales del Municipio para conocer las novedades de la agenda.',
            image: defaultImage,
            imageAlt: 'Emblema del Municipio Serranoble',
            featured: false
        },
        {
            id: 'convenio-viviendas',
            category: 'Gestión',
            title: 'La Municipalidad firmó un convenio para construir 100 viviendas',
            summary: 'El acuerdo impulsa un nuevo proyecto habitacional para vecinos de Serranoble.',
            content: 'La Municipalidad firmó un convenio para avanzar con la construcción de 100 viviendas, como parte de un nuevo proyecto habitacional para vecinos de Serranoble.',
            image: defaultImage,
            imageAlt: 'Emblema del Municipio Serranoble',
            featured: false
        },
        {
            id: 'adopcion-gatos',
            category: 'Comunidad',
            title: 'Gatos en adopción: nueva jornada de tenencia responsable',
            summary: 'La jornada promueve la adopción responsable y el cuidado de los animales.',
            content: 'La nueva jornada de tenencia responsable promueve la adopción de gatos y el cuidado de los animales en la comunidad.\n\nPara conocer los detalles de la actividad, consultá los canales oficiales del Municipio.',
            image: defaultImage,
            imageAlt: 'Emblema del Municipio Serranoble',
            featured: false
        },
        {
            id: 'muni-mas-cerca-el-hornero',
            category: 'La Muni Más Cerca',
            title: 'La Muni Más Cerca llegó al barrio El Hornero',
            summary: 'La edición 109 acercó atención y servicios a las familias del barrio.',
            content: 'La edición 109 de La Muni Más Cerca llegó al barrio El Hornero para acercar atención y servicios municipales a las familias.',
            image: defaultImage,
            imageAlt: 'Emblema del Municipio Serranoble',
            featured: false
        }
    ];

    function readNews() {
        const saved = window.localStorage.getItem(storageKey);
        if (saved === null) {
            writeNews(seedNews);
            return seedNews.map((news) => ({ ...news }));
        }

        const news = JSON.parse(saved);
        if (!Array.isArray(news) || news.some((item) => !item || typeof item.id !== 'string')) {
            throw new Error('El listado guardado de noticias tiene un formato inválido.');
        }
        let foundFeatured = false;
        const normalized = news.map((item, index) => {
            const featured = item.featured && !foundFeatured;
            if (featured) foundFeatured = true;
            if (index === 0 && !foundFeatured && news.length) {
                foundFeatured = true;
                return { ...item, featured: true };
            }
            return item.featured === featured ? item : { ...item, featured };
        });
        if (normalized.some((item, index) => item !== news[index])) writeNews(normalized);
        return normalized;
    }

    function writeNews(news) {
        window.localStorage.setItem(storageKey, JSON.stringify(news));
    }

    function makeElement(tag, className, text) {
        const element = document.createElement(tag);
        if (className) element.className = className;
        if (text !== undefined) element.textContent = text;
        return element;
    }

    function articleUrl(id) {
        return `noticia-detalle.html?id=${encodeURIComponent(id)}`;
    }

    function categoryClass(category) {
        return categories[category] || 'comunidad';
    }

    function safeImageSource(source) {
        if (typeof source !== 'string' || !source.trim()) return defaultImage;
        const value = source.trim();
        if (value.startsWith('assets/')) return value;
        try {
            const url = new URL(value, window.location.href);
            return url.protocol === 'https:' || url.protocol === 'http:' ? value : defaultImage;
        } catch {
            return defaultImage;
        }
    }

    function isValidImageSource(source) {
        if (!source.trim() || source.startsWith('assets/')) return true;
        try {
            const url = new URL(source, window.location.href);
            return (url.protocol === 'https:' || url.protocol === 'http:') && /^https?:\/\//i.test(source);
        } catch {
            return false;
        }
    }

    function updateFeatured(newsItems) {
        const image = document.getElementById('noticia-destacada-imagen');
        const title = document.getElementById('noticia-destacada-titulo');
        const link = document.getElementById('noticia-destacada-enlace');
        if (!image || !title || !link) return;

        const featured = newsItems.find((item) => item.featured) || newsItems[0];
        const section = document.getElementById('principal');
        if (!featured) {
            if (section) section.hidden = true;
            return;
        }

        if (section) section.hidden = false;
        image.src = safeImageSource(featured.image);
        image.alt = featured.imageAlt || featured.title;
        title.textContent = featured.summary || featured.title;
        link.href = articleUrl(featured.id);
    }

    function renderNewsList(newsItems) {
        const list = document.getElementById('lista-noticias');
        if (!list) return;
        list.replaceChildren();

        newsItems.forEach((news) => {
            const column = makeElement('div', 'col');
            const card = makeElement('article', 'card noticia-card h-100 shadow-sm');
            const image = makeElement('img', 'card-img-top');
            image.src = safeImageSource(news.image);
            image.alt = news.imageAlt || news.title;
            const body = makeElement('div', 'card-body');
            const category = makeElement('span', 'badge mb-2', news.category);
            category.classList.add(`noticia-badge--${categoryClass(news.category)}`);
            const title = makeElement('h3', 'h5 card-title fw-bold', news.title);
            const summary = makeElement('p', 'card-text', news.summary);
            const footer = makeElement('div', 'card-footer bg-transparent border-0');
            const link = makeElement('a', 'btn btn-primary btn-sm', 'Leer noticia');
            link.href = articleUrl(news.id);

            body.append(category, title, summary);
            footer.append(link);
            card.append(image, body, footer);
            column.append(card);
            list.append(column);
        });

        if (!newsItems.length) {
            list.append(makeElement('p', 'col', 'Todavía no hay noticias publicadas.'));
        }
    }

    function renderHomepage(newsItems) {
        const carousel = document.getElementById('noticias-secundarias');
        if (!carousel) return;

        carousel.replaceChildren();
        newsItems.filter((item) => !item.featured).slice(0, 4).forEach((news) => {
            const card = makeElement('article', 'card card-secundaria shadow-sm flex-shrink-0');
            const category = makeElement('span', 'badge position-absolute top-0 start-0 m-2', news.category);
            category.classList.add(`noticia-badge--${categoryClass(news.category)}`);
            const image = makeElement('img', 'card-img-top pt-4 px-2');
            image.src = safeImageSource(news.image);
            image.alt = news.imageAlt || news.title;
            const body = makeElement('div', 'card-body');
            body.append(makeElement('p', 'card-text small fw-semibold', news.title));
            const footer = makeElement('div', 'card-footer bg-transparent border-0 text-end');
            const link = makeElement('a', 'btn btn-primary btn-sm', 'Ampliar');
            link.href = articleUrl(news.id);
            footer.append(link);
            card.append(category, image, body, footer);
            carousel.append(card);
        });
    }

    function renderArticle(newsItems) {
        const container = document.getElementById('noticia-detalle-contenido');
        if (!container) return;

        const id = new URLSearchParams(window.location.search).get('id');
        const news = newsItems.find((item) => item.id === id);
        container.replaceChildren();
        if (!news) {
            container.append(makeElement('p', 'alert alert-warning', 'No encontramos esa noticia. Puede haber sido eliminada.'));
            return;
        }

        document.title = `${news.title} - Municipio Activo`;
        const article = makeElement('article', `noticia-detalle noticia-detalle--${categoryClass(news.category)}`);
        const header = makeElement('header', 'noticia-detalle-header');
        header.append(
            makeElement('span', 'badge noticia-detalle-categoria', news.category),
            makeElement('h1', '', news.title),
            makeElement('p', 'noticia-detalle-bajada', news.summary)
        );
        const image = makeElement('img', 'noticia-detalle-imagen');
        image.src = safeImageSource(news.image);
        image.alt = news.imageAlt || news.title;
        const content = makeElement('div', 'noticia-detalle-cuerpo');
        news.content.split(/\r?\n/).map((paragraph) => paragraph.trim()).filter(Boolean).forEach((paragraph) => {
            content.append(makeElement('p', '', paragraph));
        });
        article.append(header, image, content);
        container.append(article);
    }

    function showLoadError(error) {
        console.error('No se pudieron cargar las noticias guardadas.', error);
        const target = document.getElementById('lista-noticias') || document.getElementById('noticia-detalle-contenido');
        if (target) {
            target.replaceChildren(makeElement('p', 'alert alert-danger', 'No se pudieron cargar las noticias guardadas. Revisá el almacenamiento de este navegador e intentá nuevamente.'));
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        try {
            const news = readNews();
            updateFeatured(news);
            renderNewsList(news);
            renderHomepage(news);
            renderArticle(news);
        } catch (error) {
            showLoadError(error);
        }
    });

    window.NoticiasStore = {
        read: readNews,
        write: writeNews,
        imageSource: safeImageSource,
        isValidImageSource,
        detailUrl: articleUrl
    };
})();
