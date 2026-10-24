-- Copia versionada del volcado corregido y optimizado con tabla imagenes
-- Original en: c:\Users\Javier\Downloads\bziotj8qzahsjnnfoacp.sql

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `password_hash` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `privilegios` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rol` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ciudadano',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuarios_email` (`email`),
  KEY `idx_usuarios_rol` (`rol`),
  KEY `idx_usuarios_activo` (`activo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `usuarios` (`id`, `nombre`, `email`, `activo`, `password_hash`, `privilegios`, `rol`, `fecha_creacion`) VALUES
(5, 'juan', 'juansito@gmail.com', 1, '$2a$10$Y6vldu4wNsB2ST3QIMy6d.O0vhzLOAO/YFvFzmPEevWYpr0F9OKWG', 'crear_reclamo,seguimiento_reclamo,ver_novedades', 'ciudadano', '2026-10-03 00:00:00'),
(6, 'Javier Maldonado', 'javimaldona@hotmail.com', 1, '$2a$10$VqEDm72zqA4H9Qz7jDtDHekbiweyrP8cAblTmxSzP0iZZYy3lAaeC', 'crear_reclamo,seguimiento_reclamo,ver_novedades', 'administrativo', '2026-10-03 00:00:00'),
(7, 'Juan peres', 'jaunperes@hotmail.com', 1, '$2a$10$1zAiETXEMEFWEf84/KJDzOhdxH5Mf5DA4M/7dHsRKIA1bbmQrNcWK', 'crear_reclamo,seguimiento_reclamo,ver_novedades', 'ciudadano', '2026-10-03 00:00:00'),
(8, 'María Fernanda', 'mariafernandacortezdiaz@gmail.com', 1, '$2a$10$Lyy6IyyV1bU9FIlnIkSOYeB2RdOqavLV/WVkqIyLpzL5V2UUgw6li', 'crear_reclamo,seguimiento_reclamo,ver_novedades', 'administrativo', '2026-10-03 00:00:00'),
(10, 'Franco Cortez', 'profqca.cortez@gmail.com', 1, '$2a$10$WjBc4OQdKcp8tvihUc8LPO0G8HCS/6mTT9lya3mMgKsSi7xo5WyL.', 'crear_reclamo,seguimiento_reclamo,ver_novedades', 'ciudadano', '2026-10-03 00:00:00'),
(11, 'Juana Pérez', '2711.mfcd@gmail.com', 1, '$2a$10$M4hjQLwWHd273tsV07gj7ezSB/AZZaf2aNhJrzgjwDoi0rarvcQui', 'crear_reclamo,seguimiento_reclamo,ver_novedades', 'ciudadano', '2026-10-03 00:00:00');

DROP TABLE IF EXISTS `noticias`;
CREATE TABLE `noticias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `slug` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `category` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  `summary` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `image` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_alt` varchar(180) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `featured` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_noticias_slug` (`slug`),
  KEY `idx_noticias_featured` (`featured`),
  KEY `idx_noticias_category` (`category`),
  KEY `idx_noticias_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `noticias` (`id`, `slug`, `category`, `title`, `summary`, `content`, `image`, `image_alt`, `featured`, `created_at`, `updated_at`) VALUES
(7, 'agenda-semanal-municipal', 'La Muni Más Cerca', 'La Municipalidad despliega una agenda semanal con capacitaciones, servicios en terreno y nuevas acciones de fortalecimiento barrial para toda la comunidad.', 'La agenda semanal reúne capacitaciones, servicios en terreno y acciones de fortalecimiento barrial para toda la comunidad.', 'La agenda semanal reúne capacitaciones, servicios en terreno y acciones de fortalecimiento barrial para toda la comunidad.', 'assets/imagenes/principal.png', 'Actividades y servicios municipales de la agenda semanal', 1, '2026-10-23 20:13:12.000000', NULL),
(8, 'jornada-adopcion-gatos', 'COMUNIDAD', 'Gatos en adopción: nueva jornada de tenencia responsable', 'La jornada promueve la adopción responsable y el cuidado de los animales.', 'La jornada promueve la adopción responsable y el cuidado de los animales.', 'assets/imagenes/gatitos.jpg', 'Gatos en adopción', 0, '2026-10-23 20:13:11.000000', NULL),
(9, 'convenio-construccion-100-viviendas', 'GESTIÓN', 'La Municipalidad firmó un convenio para construir 100 viviendas', 'El acuerdo impulsa un nuevo proyecto habitacional para vecinos de Serranoble.', 'El acuerdo impulsa un nuevo proyecto habitacional para vecinos de Serranoble.', 'assets/imagenes/viviendas.jpg', 'Convenio para construir viviendas', 0, '2026-10-23 20:13:10.000000', NULL),
(10, 'agenda-actividades-semanal', 'ACTIVIDADES', 'La Municipalidad presenta la agenda de actividades para esta semana', 'Conocé las propuestas y actividades abiertas para disfrutar en la comunidad.', 'Conocé las propuestas y actividades abiertas para disfrutar en la comunidad.', 'assets/imagenes/agenda cultural.jpg', 'Agenda cultural', 0, '2026-10-23 20:13:09.000000', NULL),
(11, 'la-muni-mas-cerca-los-fresnos-edicion-111', 'LA MUNI MÁS CERCA', 'La Muni Más Cerca llegó al barrio Los Fresnos en su edición 111', 'El operativo acercó servicios municipales y atención a las familias del barrio.', 'El operativo acercó servicios municipales y atención a las familias del barrio.', 'assets/imagenes/noticiabarrios2.jpg', 'Operativo La Muni Más Cerca en el barrio Los Fresnos', 0, '2026-10-23 20:13:08.000000', NULL);

DROP TABLE IF EXISTS `imagenes`;
CREATE TABLE `imagenes` (
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

INSERT INTO `imagenes` (`noticia_id`, `url`, `alt_text`, `es_principal`, `orden`, `mime_type`, `created_at`) VALUES
(7, 'assets/imagenes/principal.png', 'Actividades y servicios municipales de la agenda semanal', 1, 1, 'image/png', '2026-10-23 20:13:12'),
(8, 'assets/imagenes/gatitos.jpg', 'Gatos en adopción', 1, 1, 'image/jpeg', '2026-10-23 20:13:11'),
(9, 'assets/imagenes/viviendas.jpg', 'Convenio para construir viviendas', 1, 1, 'image/jpeg', '2026-10-23 20:13:10'),
(10, 'assets/imagenes/agenda cultural.jpg', 'Agenda cultural', 1, 1, 'image/jpeg', '2026-10-23 20:13:09'),
(11, 'assets/imagenes/noticiabarrios2.jpg', 'Operativo La Muni Más Cerca en el barrio Los Fresnos', 1, 1, 'image/jpeg', '2026-10-23 20:13:08');

DROP TABLE IF EXISTS `reclamos`;
CREATE TABLE `reclamos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo_seguimiento` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `categoria` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `area_asignada` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `direccion` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `google_maps_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `imagen_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDIENTE',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reclamos_codigo` (`codigo_seguimiento`),
  KEY `fk_reclamos_usuario` (`usuario_id`),
  KEY `idx_reclamos_estado` (`estado`),
  KEY `idx_reclamos_categoria` (`categoria`),
  KEY `idx_reclamos_fecha` (`fecha_creacion` DESC),
  CONSTRAINT `fk_reclamos_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `reclamos` (`id`, `codigo_seguimiento`, `usuario_id`, `categoria`, `area_asignada`, `direccion`, `google_maps_url`, `descripcion`, `imagen_path`, `estado`, `fecha_creacion`) VALUES
(1, 'E786417759A0', NULL, 'Luminarias', NULL, 'Av. San Martin 1234, Barrio Centro', 'https://maps.google.com/', 'La luminaria de la esquina permanece apagada durante la noche.', NULL, 'RESUELTO', '2026-10-03 03:53:48'),
(2, '9BD7A4DEE5C7', NULL, 'Bacheo', NULL, 'Calle Belgrano 450, Barrio Norte', NULL, 'Hay un bache grande que dificulta el paso de vehiculos y peatones.', NULL, 'EN_PROCESO', '2026-10-03 03:53:49'),
(3, 'F078BDE3134E', NULL, 'Luminarias', NULL, 'dsasadsdaasdasd 12', NULL, 'sadasdasdas dasd awsd asddsa', NULL, 'RESUELTO', '2026-10-03 21:10:35'),
(4, '00B44F4BF76E', NULL, 'Bacheo', NULL, 'bachesito 23', NULL, 'hay un bache en bachesito y esta muy bacheadi', NULL, 'PENDIENTE', '2026-10-03 21:19:52'),
(5, 'AB870EFF6B62', NULL, 'Otros', NULL, 'Calle dardo rocha, barrio cementerio', NULL, 'Es un árbol, necesita poda', NULL, 'PENDIENTE', '2026-10-04 21:39:55'),
(6, '20C7F4914A3B', NULL, 'Luminarias', NULL, 'calle 7 molinos de viento 1094', NULL, 'La luminaria está rota, requiere cambio', 'uploads/efb611b9-f828-4476-b9ae-a54b91294e61.webp', 'PENDIENTE', '2026-10-06 14:20:05'),
(7, 'BD2998C12C3B', NULL, 'Bacheo', NULL, 'Río Paraná 751', 'https://www.google.com/maps/place/R%C3%ADo+Paran%C3%A1+751,+F5302+La+Rioja/@-29.4045901,-66.8391044,761m/data=!3m2!1e3!4b1!4m6!3m5!1s0x9427da43e77cb80d:0x4f1a760944953937!8m2!3d-29.4045901!4d-66.8365295!16s%2Fg%2F11fm3pw9bz?entry=ttu&g_ep=EgoyMDI2MTAxMy4wIKXMDSoASAFQAw%3D%3D', 'Hay un pozo gigante frente a mi casa.', 'uploads/fa9c6289-fb82-4742-a750-c65a90e6e298.jpg', 'PENDIENTE', '2026-10-18 13:41:37'),
(8, '81D2B7A0CB3E', NULL, 'Limpieza', NULL, 'Rio Parana 963', NULL, 'Esta sucio, por favor limpien', NULL, 'RESUELTO', '2026-10-20 00:13:14'),
(9, '772FDA6DD4FE', NULL, 'Limpieza', NULL, 'san martin 40', NULL, 'limpiar porque esta sucio', NULL, 'PENDIENTE', '2026-10-20 01:28:44'),
(10, 'F7AEA88D7361', NULL, 'Otros', NULL, 'san martin 55', NULL, 'Los vecinos tiran basura en un descampado. Se esta formando un microbasural', NULL, 'PENDIENTE', '2026-10-21 18:34:38'),
(11, '026204204274', NULL, 'Limpieza', NULL, 'Rivadavia 84', 'https://www.google.com/maps/place/Rivadavia+84,+F5300+La+Rioja/@-29.4139505,-66.8513616,761m/data=!3m2!1e3!4b1!4m6!3m5!1s0x9427da361386c4ff:0xbd8b7181478bf226!8m2!3d-29.4139505!4d-66.8487867!16s%2Fg%2F11j0nk49h5?entry=ttu&g_ep=EgoyMDI2MTAxOS4wIKXMDSoASAFQAw%3D%3D', 'Hay bolsas de basura abiertas por animales', 'uploads/9452880b-b7e9-4248-ba09-15745f3e8140.jpg', 'PENDIENTE', '2026-10-23 19:06:21');

ALTER TABLE `usuarios` AUTO_INCREMENT = 12;
ALTER TABLE `noticias` AUTO_INCREMENT = 12;
ALTER TABLE `imagenes` AUTO_INCREMENT = 12;
ALTER TABLE `reclamos` AUTO_INCREMENT = 12;

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
