(() => {
    const esPanelReclamos = document.body.hasAttribute('data-admin-dashboard');
    const encabezado = document.querySelector('[data-site-header]');
    const pie = document.querySelector('[data-site-footer]');

    if (encabezado) {
        encabezado.innerHTML = `
            <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
                <div class="container">
                    <a class="navbar-brand d-flex align-items-center" href="index.html">
                        <img src="assets/logo/Logo.jpeg" alt="Municipio Serranoble" class="me-2" height="80">
                        <span>Municipio Serranoble</span>
                    </a>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="collapse navbar-collapse" id="navbarNav">
                        <ul class="navbar-nav ms-auto">
                            <li class="nav-item">
                                <a class="nav-link" href="index.html" data-nav="inicio">Inicio</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="index.html#noticias" data-nav="noticias">Noticias</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="index.html#tramites" data-nav="tramites">Trámites / Reclamos</a>
                            </li>
                            <li class="nav-item" data-admin-only hidden>
                                <a class="nav-link" href="vista-reclamo.html" data-nav="gestionar-reclamos">Gestionar reclamos</a>
                            </li>
                            <li class="nav-item" data-admin-only hidden>
                                <a class="nav-link" href="administrar-noticias.html" data-nav="administrar-noticias">Administrar noticias</a>
                            </li>
                            ${esPanelReclamos
                                ? `<li class="nav-item d-flex align-items-center">
                                    <span id="adminUserGreeting" class="navbar-text text-white me-3 small" aria-live="polite"></span>
                                    <button type="button" class="btn btn-outline-light btn-sm" id="btnCerrarSesion">Cerrar sesión</button>
                                </li>`
                                : `<li class="nav-item">
                                    <a class="btn btn-outline-light ms-2" href="login.html" data-auth-link>Iniciar Sesión</a>
                                </li>`}
                        </ul>
                    </div>
                </div>
            </nav>`;

        const seccionPorPagina = {
            'index.html': 'inicio',
            'noticias.html': 'noticias',
            'noticia-detalle.html': 'noticias',
            'reclamos-municipal.html': 'tramites',
            'reclamo_y_notificacion_mas_correo.html': 'tramites',
            'vista-reclamo.html': 'gestionar-reclamos',
            'administrar-noticias.html': 'administrar-noticias'
        };
        const pagina = window.location.pathname.split('/').pop() || 'index.html';
        const seccionActual = seccionPorPagina[pagina];
        const enlaceActivo = seccionActual && encabezado.querySelector(`[data-nav="${seccionActual}"]`);

        if (enlaceActivo) {
            enlaceActivo.classList.add('active');
            enlaceActivo.setAttribute('aria-current', 'page');
        }
    }

    if (pie) {
        pie.innerHTML = `
            <div class="container">
                <div class="row text-center text-md-start g-3 align-items-center">
                    <div class="col-md-4">
                        <h5 class="fw-bold mb-2">Municipio Serranoble</h5>
                        <p class="mb-1">Atención al ciudadano</p>
                        <p class="mb-0">Comprometidos con la comunidad y el servicio público.</p>
                    </div>
                    <div class="col-md-4">
                        <h6 class="fw-bold mb-2">Ubicación</h6>
                        <p class="mb-1">Av. Principal 123</p>
                        <p class="mb-0">Serranoble, Provincia</p>
                    </div>
                    <div class="col-md-4">
                        <h6 class="fw-bold mb-2">Contacto</h6>
                        <p class="mb-1">Tel: (0341) 456-7890</p>
                        <p class="mb-0">info@serranoble.gob.ar</p>
                    </div>
                </div>
                <p class="mb-0 mt-4 text-center">&copy; 2026 Municipio Serranoble - Todos los derechos reservados</p>
            </div>`;
    }
})();
