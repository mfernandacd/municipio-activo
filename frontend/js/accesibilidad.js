document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('panel-accesibilidad')) {
        document.body.insertAdjacentHTML('beforeend', `
            <div id="panel-accesibilidad" class="panel-accesibilidad d-none" aria-labelledby="titulo-accesibilidad">
                <div class="panel-accesibilidad-topbar">
                    <div class="panel-idioma-wrapper">
                        <button type="button" id="btn-idioma" class="panel-idioma" aria-label="Seleccionar idioma" aria-expanded="false" aria-controls="lista-idiomas">🇪🇸 <span>Español</span>⌄</button>
                        <div id="lista-idiomas" class="panel-lista-idiomas" hidden><button type="button" aria-current="true">🇪🇸 Español (actual)</button></div>
                    </div>
                    <button type="button" id="btn-cerrar-accesibilidad" class="panel-cerrar" aria-label="Cerrar">&times;</button>
                </div>
                <div class="panel-accesibilidad-header">
                    <div class="panel-icono" aria-hidden="true">&#9733;</div>
                    <h2 id="titulo-accesibilidad">Ajustes de Accesibilidad</h2>
                    <p>Desarrollado para Municipio Activo</p>
                </div>
                <div class="panel-accesibilidad-body">
                    <section class="accesibilidad-seccion" aria-labelledby="contenido-accesibilidad">
                        <h3 id="contenido-accesibilidad">Módulos de contenido</h3>
                        <div class="control-tamano-fuente">
                            <span>Tamaño de fuente</span>
                            <div class="control-tamano-botones">
                                <button type="button" class="control-circular" data-action="increase-font" aria-label="Aumentar tamaño de fuente">+</button>
                                <button type="button" class="control-predeterminado" data-action="reset-font">Predeterminado</button>
                                <button type="button" class="control-circular" data-action="decrease-font" aria-label="Disminuir tamaño de fuente">-</button>
                            </div>
                        </div>
                        <div class="accesibilidad-grid">
                            <button type="button" class="accesibilidad-opcion" data-action="readable-font"><span class="opcion-icon">Aa</span><span>Fuente legible</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="line-height"><span class="opcion-icon">↕</span><span>Altura de línea</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="cursor"><span class="opcion-icon">⌁</span><span>Cursor grande</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="letter-spacing"><span class="opcion-icon">↔</span><span>Espaciado de letras</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="align-center"><span class="opcion-icon">≡</span><span>Alinear al centro</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="bold"><span class="opcion-icon">B</span><span>Grosor de fuente</span></button>
                        </div>
                    </section>
                    <section class="accesibilidad-seccion" aria-labelledby="color-accesibilidad">
                        <h3 id="color-accesibilidad">Módulos de color</h3>
                        <div class="accesibilidad-grid accesibilidad-grid-three">
                            <button type="button" class="accesibilidad-opcion" data-action="light-contrast"><span class="opcion-icon">☼</span><span>Contraste claro</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="high-contrast"><span class="opcion-icon">◐</span><span>Alto contraste</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="monochrome"><span class="opcion-icon">◑</span><span>Monocromo</span></button>
                        </div>
                    </section>
                    <section class="accesibilidad-seccion" aria-labelledby="orientacion-accesibilidad">
                        <h3 id="orientacion-accesibilidad">Módulos de orientación</h3>
                        <div class="accesibilidad-grid accesibilidad-grid-three">
                            <button type="button" class="accesibilidad-opcion" data-action="reading-line"><span class="opcion-icon">▤</span><span>Línea de lectura</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="reading-mask"><span class="opcion-icon">▰</span><span>Máscara de lectura</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="hide-images"><span class="opcion-icon">◩</span><span>Ocultar imágenes</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="highlight-content"><span class="opcion-icon">⛶</span><span>Resaltar contenido</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="stop-animations"><span class="opcion-icon">⊘</span><span>Detener animaciones</span></button>
                            <button type="button" class="accesibilidad-opcion" data-action="underline-links"><span class="opcion-icon">↗</span><span>Resaltar enlaces</span></button>
                        </div>
                    </section>
                    <button type="button" id="btn-restablecer-accesibilidad" class="btn-restablecer-accesibilidad">Restablecer configuraciones</button>
                </div>
            </div>
            <button type="button" id="btn-accesibilidad" class="btn-accesibilidad" aria-label="Ajustes de accesibilidad" aria-expanded="false" aria-controls="panel-accesibilidad">
                <img src="https://www.munichile.cl/wp-content/plugins/accessibility-onetap/assets/images/admin/Original_Logo_Icon.svg" alt="Icono de accesibilidad">
            </button>`);
    }

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
