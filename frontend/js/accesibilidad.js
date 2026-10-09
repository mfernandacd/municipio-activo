document.addEventListener('DOMContentLoaded', () => {
    // Elementos del panel de accesibilidad y del botón flotante.
    // Estos nodos se usan para abrir/cerrar el panel, cambiar ajustes visuales y restaurar valores por defecto.
    const btnAccesibilidad = document.getElementById('btn-accesibilidad');
    const panelAccesibilidad = document.getElementById('panel-accesibilidad');
    const btnCerrar = document.getElementById('btn-cerrar-accesibilidad');
    const btnIdioma = document.getElementById('btn-idioma');
    const listaIdiomas = document.getElementById('lista-idiomas');
    const btnRestablecer = document.getElementById('btn-restablecer-accesibilidad');
    const body = document.body;

    // Si faltan elementos esenciales, se corta la ejecución para evitar errores de DOM.
    if (!btnAccesibilidad || !panelAccesibilidad || !btnCerrar || !btnIdioma || !listaIdiomas || !btnRestablecer) {
        return;
    }

    // Control del tamaño de fuente actual para ajustar la lectura sin perder estado entre acciones.
    let fontSizeActual = 100;

    // Mapeo de cada acción disponible en el panel con la clase CSS que modifica la visualización.
    const actionToClass = {
        'readable-font': 'fuente-legible',
        'line-height': 'altura-linea',
        cursor: 'cursor-grande',
        'letter-spacing': 'espaciado-letras',
        'align-center': 'texto-centrado',
        bold: 'fuente-negrita',
        'light-contrast': 'contraste-claro',
        'high-contrast': 'alto-contraste',
        monochrome: 'monocromo',
        'reading-line': 'linea-lectura',
        'reading-mask': 'mascara-lectura',
        'hide-images': 'ocultar-imagenes',
        'highlight-content': 'resaltar-contenido',
        'stop-animations': 'detener-animaciones',
        'underline-links': 'resaltar-enlaces'
    };

    // Abre o cierra el panel de accesibilidad y actualiza el estado accesible del botón.
    const toggleAccessibilityPanel = () => {
        const hidden = panelAccesibilidad.classList.toggle('d-none');
        btnAccesibilidad.setAttribute('aria-expanded', String(!hidden));
    };

    // Restablece todas las clases visuales y el tamaño de letra a su estado original.
    // Se usa para devolver la página a la configuración por defecto del sitio.
    const resetAccessibilityState = () => {
        body.className = body.className
            .replace(/\b(fuente-legible|altura-linea|cursor-grande|espaciado-letras|texto-centrado|fuente-negrita|contraste-claro|alto-contraste|monocromo|linea-lectura|mascara-lectura|ocultar-imagenes|resaltar-contenido|detener-animaciones|resaltar-enlaces)\b/g, '')
            .replace(/\s+/g, ' ')
            .trim();

        body.style.fontSize = '';
        body.style.removeProperty('--cursor-x');
        body.style.removeProperty('--cursor-y');
        fontSizeActual = 100;

        document.querySelectorAll('.accesibilidad-opcion.activo').forEach((button) => button.classList.remove('activo'));
    };

    // Al tocar el botón flotante se alterna la visibilidad del panel.
    btnAccesibilidad.addEventListener('click', toggleAccessibilityPanel);

    // Cierra el panel cuando se presiona el botón de cerrar.
    btnCerrar.addEventListener('click', () => {
        panelAccesibilidad.classList.add('d-none');
        btnAccesibilidad.setAttribute('aria-expanded', 'false');
    });

    // Muestra u oculta la lista de idiomas al hacer clic en el selector.
    btnIdioma.addEventListener('click', () => {
        listaIdiomas.hidden = !listaIdiomas.hidden;
        btnIdioma.setAttribute('aria-expanded', String(!listaIdiomas.hidden));
    });

    // Guarda la posición del puntero para que ayudas visuales como línea de lectura o máscara
    // puedan seguir al cursor del usuario durante la navegación.
    document.addEventListener('pointermove', (event) => {
        body.style.setProperty('--cursor-x', `${event.clientX}px`);
        body.style.setProperty('--cursor-y', `${event.clientY}px`);
    });

    // Recorre todos los controles del panel y aplica su acción correspondiente.
    document.querySelectorAll('[data-action]').forEach((button) => {
        button.addEventListener('click', () => {
            const action = button.dataset.action;

            // Aumenta o reduce el tamaño de fuente con límites definidos.
            if (action === 'increase-font') {
                fontSizeActual = Math.min(fontSizeActual + 10, 140);
                body.style.fontSize = `${fontSizeActual}%`;
                return;
            }

            if (action === 'decrease-font') {
                fontSizeActual = Math.max(fontSizeActual - 10, 80);
                body.style.fontSize = `${fontSizeActual}%`;
                return;
            }

            if (action === 'reset-font') {
                fontSizeActual = 100;
                body.style.fontSize = '';
                return;
            }

            // Las ayudas de lectura se excluyen entre sí para no superponer dos modos a la vez.
            if (action === 'reading-line' || action === 'reading-mask') {
                const currentClass = action === 'reading-line' ? 'linea-lectura' : 'mascara-lectura';
                const otherClass = action === 'reading-line' ? 'mascara-lectura' : 'linea-lectura';
                const otherButton = document.querySelector(`[data-action="${action === 'reading-line' ? 'reading-mask' : 'reading-line'}"]`);

                if (otherButton) {
                    otherButton.classList.remove('activo');
                }

                body.classList.remove(otherClass);
                body.classList.toggle(currentClass);
                button.classList.toggle('activo');
                return;
            }

            // Para el resto de acciones, se activa la clase CSS asociada.
            if (actionToClass[action]) {
                body.classList.toggle(actionToClass[action]);
                button.classList.toggle('activo');
            }
        });
    });

    // Vuelve todos los ajustes de accesibilidad a su estado inicial.
    btnRestablecer.addEventListener('click', resetAccessibilityState);
});
