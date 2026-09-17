# Municipio Activo

Sitio web institucional para la Municipalidad de Serranoble, con información pública, noticias, servicios y accesibilidad.

## Estructura del proyecto

- [frontend/index.html](frontend/index.html): página principal del sitio.
- [frontend/Css/styles.css](frontend/Css/styles.css): estilos visuales y ajustes de accesibilidad.
- [frontend/js/accesibilidad.js](frontend/js/accesibilidad.js): lógica del panel de accesibilidad y personalización visual.
- [frontend/js/main.js](frontend/js/main.js): archivo base para futuras funciones globales del frontend.
- [frontend/assets](frontend/assets): recursos gráficos e imágenes del sitio.
- [backend](backend): carpeta de backend o lógica del servidor.

## Cómo ejecutar

Puedes abrir la página principal directamente en el navegador:

- [frontend/index.html](frontend/index.html)

O servir la carpeta localmente con un servidor estático, por ejemplo:

```bash
cd frontend
python -m http.server 8000
```

Luego abre:

```text
http://localhost:8000
```

## Objetivo

Este proyecto busca brindar una experiencia clara, accesible y moderna para que los ciudadanos consulten noticias, trámites y servicios municipales.
