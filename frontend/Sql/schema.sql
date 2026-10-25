-- Esquema inicial de Municipio Activo para una base MySQL vacía.
-- No contiene usuarios, contraseñas ni datos de reclamos reales.
-- Configura DB_NAME y ejecuta este archivo sobre esa base antes de iniciar la aplicación.

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'ciudadano',
    privilegios VARCHAR(255) DEFAULT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email),
    KEY idx_usuarios_rol (rol),
    KEY idx_usuarios_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS noticias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(180) NOT NULL,
    category VARCHAR(80) NOT NULL,
    title VARCHAR(160) NOT NULL,
    summary VARCHAR(300) NOT NULL,
    content TEXT NOT NULL,
    image VARCHAR(1000) DEFAULT NULL,
    image_alt VARCHAR(180) DEFAULT NULL,
    featured TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_noticias_slug (slug),
    KEY idx_noticias_featured (featured),
    KEY idx_noticias_category (category),
    KEY idx_noticias_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reclamos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_seguimiento VARCHAR(20) NOT NULL,
    usuario_id BIGINT DEFAULT NULL,
    categoria VARCHAR(50) NOT NULL,
    area_asignada VARCHAR(100) DEFAULT NULL,
    direccion VARCHAR(255) NOT NULL,
    barrio VARCHAR(100) NOT NULL DEFAULT 'Sin especificar',
    zona VARCHAR(20) NOT NULL DEFAULT 'Sin especificar',
    google_maps_url VARCHAR(500) DEFAULT NULL,
    descripcion TEXT NOT NULL,
    imagen_path VARCHAR(255) DEFAULT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reclamos_codigo (codigo_seguimiento),
    KEY idx_reclamos_usuario (usuario_id),
    KEY idx_reclamos_estado (estado),
    KEY idx_reclamos_categoria (categoria),
    KEY idx_reclamos_fecha (fecha_creacion DESC),
    CONSTRAINT fk_reclamos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS imagenes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    noticia_id BIGINT NOT NULL,
    url VARCHAR(1000) NOT NULL,
    alt_text VARCHAR(255) DEFAULT NULL,
    es_principal TINYINT(1) NOT NULL DEFAULT 1,
    orden INT NOT NULL DEFAULT 1,
    mime_type VARCHAR(100) DEFAULT NULL,
    tamano_bytes BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_imagenes_noticia (noticia_id),
    KEY idx_imagenes_principal (noticia_id, es_principal),
    CONSTRAINT fk_imagenes_noticia FOREIGN KEY (noticia_id)
        REFERENCES noticias (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
