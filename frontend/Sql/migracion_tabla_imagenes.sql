-- =============================================================================
-- SCRIPT DE MIGRACIÓN: Creación de tabla `imagenes` y mejoras sobre BD existente
-- Base de datos: la configurada para la aplicación (Render / Clever Cloud).
-- Este script NO borra datos existentes; actualiza tipos, añade índices y crea
-- la tabla `imagenes` migrando las imágenes actuales de las noticias.
-- =============================================================================

START TRANSACTION;

-- 1. Actualizar charsets y collations a utf8mb4 en tablas existentes
ALTER TABLE `usuarios` 
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    MODIFY COLUMN `activo` tinyint(1) NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS `fecha_actualizacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE `reclamos` 
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    ADD COLUMN IF NOT EXISTS `fecha_actualizacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE `noticias` 
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    MODIFY COLUMN `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    MODIFY COLUMN `updated_at` datetime(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6);

-- Limpiar fechas '0000-00-00' si existen
UPDATE `noticias` 
SET `updated_at` = NULL 
WHERE CAST(`updated_at` AS CHAR) LIKE '0000-00-00%';

-- Limpiar strings vacíos en reclamos para que sean NULL
UPDATE `reclamos` SET `google_maps_url` = NULL WHERE `google_maps_url` = '';
UPDATE `reclamos` SET `imagen_path` = NULL WHERE `imagen_path` = '';

-- 2. Crear tabla `imagenes` conectada a `noticias`
CREATE TABLE IF NOT EXISTS `imagenes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `noticia_id` bigint NOT NULL,
  `url` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `alt_text` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `es_principal` tinyint(1) NOT NULL DEFAULT 1,
  `orden` int NOT NULL DEFAULT 1,
  `mime_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tamano_bytes` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_imagenes_noticia` (`noticia_id`),
  KEY `idx_imagenes_principal` (`noticia_id`, `es_principal`),
  CONSTRAINT `fk_imagenes_noticia` FOREIGN KEY (`noticia_id`) REFERENCES `noticias` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Migrar las imágenes que ya están en la tabla `noticias` hacia `imagenes`
INSERT IGNORE INTO `imagenes` (`noticia_id`, `url`, `alt_text`, `es_principal`, `orden`, `created_at`)
SELECT 
    `id` AS `noticia_id`,
    `image` AS `url`,
    `image_alt` AS `alt_text`,
    1 AS `es_principal`,
    1 AS `orden`,
    `created_at`
FROM `noticias`
WHERE `image` IS NOT NULL AND `image` <> ''
  AND `id` NOT IN (SELECT DISTINCT `noticia_id` FROM `imagenes`);

-- 4. Índices para mejorar rendimiento de consultas
-- Noticias
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'noticias' AND index_name = 'idx_noticias_created_at');
SET @sql := IF(@exist = 0, 'ALTER TABLE noticias ADD INDEX idx_noticias_created_at (created_at DESC)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'noticias' AND index_name = 'idx_noticias_featured');
SET @sql := IF(@exist = 0, 'ALTER TABLE noticias ADD INDEX idx_noticias_featured (featured)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Reclamos
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'reclamos' AND index_name = 'idx_reclamos_estado');
SET @sql := IF(@exist = 0, 'ALTER TABLE reclamos ADD INDEX idx_reclamos_estado (estado)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'reclamos' AND index_name = 'idx_reclamos_fecha');
SET @sql := IF(@exist = 0, 'ALTER TABLE reclamos ADD INDEX idx_reclamos_fecha (fecha_creacion DESC)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Usuarios
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND index_name = 'idx_usuarios_rol');
SET @sql := IF(@exist = 0, 'ALTER TABLE usuarios ADD INDEX idx_usuarios_rol (rol)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

COMMIT;
