-- Creación de la base de datos (opcional si ya existe)
CREATE DATABASE IF NOT EXISTS municipalidad_serranoble
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE municipalidad_serranoble;

-- 1. Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar roles base del sistema
INSERT INTO roles (nombre_rol) VALUES 
('contribuyente'),
('agente'),
('administrador')
ON DUPLICATE KEY UPDATE nombre_rol=VALUES(nombre_rol);

-- 2. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    id_rol_fk INT NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_roles 
        FOREIGN KEY (id_rol_fk) 
        REFERENCES roles(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;