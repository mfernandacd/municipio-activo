CREATE DATABASE IF NOT EXISTS `Municipio-Activo`
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE `Municipio-Activo`;

CREATE TABLE IF NOT EXISTS reclamos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_seguimiento VARCHAR(50) NOT NULL UNIQUE, -- 
    usuario_id BIGINT,                              -- 
    categoria VARCHAR(50) NOT NULL,                 -- Categoría del reclamo (Luminarias, Bacheo, Limpieza, Otros)
    area_asignada VARCHAR(100),                     -- Área municipal 
    direccion VARCHAR(255) NOT NULL,                -- 
    google_maps_url VARCHAR(500),                   -- 
    descripcion TEXT NOT NULL,                      -- 
    imagen_path VARCHAR(255),                       --
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, --
    PRIMARY KEY (id),
    CONSTRAINT fk_reclamos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO usuarios (email, password_hash, nombre, activo)
VALUES (
    'admin@municipio.gob.ar',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Administrador Municipal',
    TRUE
);

INSERT INTO reclamos
(categoria, direccion, google_maps_url, descripcion, imagen_path, estado, fecha_creacion)
VALUES
(
    'Luminarias',
    'Av. San Martin 1234, Barrio Centro',
    'https://maps.google.com/',
    'La luminaria de la esquina permanece apagada durante la noche.',
    '',
    'PENDIENTE',
    '2026-09-10 08:30:00'
),
(
    'Bacheo',
    'Calle Belgrano 450, Barrio Norte',
    '',
    'Hay un bache grande que dificulta el paso de vehiculos y peatones.',
    '',
    'EN_PROCESO',
    '2026-09-11 10:15:00'
),
(
    'Limpieza',
    'Pasaje Los Fresnos 78, Barrio Los Fresnos',
    'https://maps.google.com/',
    'Se necesita retirar residuos acumulados en la esquina.',
    '',
    'PENDIENTE',
    '2026-09-12 14:20:00'
),
(
    'Otros',
    'Plaza Principal, zona oeste',
    '',
    'El banco de la plaza se encuentra roto y necesita reparacion.',
    '',
    'RESUELTO',
    '2026-09-13 16:45:00'
);