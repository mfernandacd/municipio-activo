-- Carga en MySQL las noticias que estaban escritas directamente en el inicio.
-- Ejecutar sobre la base configurada para la aplicación, una vez que exista la tabla noticias.
START TRANSACTION;

INSERT IGNORE INTO noticias
    (slug, category, title, summary, content, image, image_alt, featured, created_at)
VALUES
    (
        'agenda-semanal-municipal',
        'ACTIVIDADES',
        'La Municipalidad despliega una agenda semanal con capacitaciones, servicios en terreno y nuevas acciones de fortalecimiento barrial para toda la comunidad.',
        'La agenda semanal reúne capacitaciones, servicios en terreno y acciones de fortalecimiento barrial para toda la comunidad.',
        'La agenda semanal reúne capacitaciones, servicios en terreno y acciones de fortalecimiento barrial para toda la comunidad.',
        'assets/imagenes/principal.png',
        'Actividades y servicios municipales de la agenda semanal',
        TRUE,
        CURRENT_TIMESTAMP
    ),
    (
        'jornada-adopcion-gatos',
        'COMUNIDAD',
        'Gatos en adopción: nueva jornada de tenencia responsable',
        'La jornada promueve la adopción responsable y el cuidado de los animales.',
        'La jornada promueve la adopción responsable y el cuidado de los animales.',
        'assets/imagenes/gatitos.jpg',
        'Gatos en adopción',
        FALSE,
        CURRENT_TIMESTAMP - INTERVAL 1 SECOND
    ),
    (
        'convenio-construccion-100-viviendas',
        'GESTIÓN',
        'La Municipalidad firmó un convenio para construir 100 viviendas',
        'El acuerdo impulsa un nuevo proyecto habitacional para vecinos de Serranoble.',
        'El acuerdo impulsa un nuevo proyecto habitacional para vecinos de Serranoble.',
        'assets/imagenes/viviendas.jpg',
        'Convenio para construir viviendas',
        FALSE,
        CURRENT_TIMESTAMP - INTERVAL 2 SECOND
    ),
    (
        'agenda-actividades-semanal',
        'ACTIVIDADES',
        'La Municipalidad presenta la agenda de actividades para esta semana',
        'Conocé las propuestas y actividades abiertas para disfrutar en la comunidad.',
        'Conocé las propuestas y actividades abiertas para disfrutar en la comunidad.',
        'assets/imagenes/agenda cultural.jpg',
        'Agenda cultural',
        FALSE,
        CURRENT_TIMESTAMP - INTERVAL 3 SECOND
    ),
    (
        'la-muni-mas-cerca-los-fresnos-edicion-111',
        'LA MUNI MÁS CERCA',
        'La Muni Más Cerca llegó al barrio Los Fresnos en su edición 111',
        'El operativo acercó servicios municipales y atención a las familias del barrio.',
        'El operativo acercó servicios municipales y atención a las familias del barrio.',
        'assets/imagenes/noticiabarrios2.jpg',
        'Operativo La Muni Más Cerca en el barrio Los Fresnos',
        FALSE,
        CURRENT_TIMESTAMP - INTERVAL 4 SECOND
    );

-- Deja una sola noticia destacada y conserva la agenda como principal al volver a ejecutar este script.
UPDATE noticias
SET featured = (slug = 'agenda-semanal-municipal')
WHERE featured = TRUE
   OR slug IN (
       'agenda-semanal-municipal',
       'jornada-adopcion-gatos',
       'convenio-construccion-100-viviendas',
       'agenda-actividades-semanal',
       'la-muni-mas-cerca-los-fresnos-edicion-111'
   );

COMMIT;
