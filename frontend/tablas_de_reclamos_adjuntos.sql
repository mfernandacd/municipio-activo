USE municipalidad_serranoble;

-- Creación de la tabla reclamos
CREATE TABLE IF NOT EXISTS reclamos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_seguimiento VARCHAR(50) NOT NULL UNIQUE,
    id_usuario_fk INT NOT NULL, -- Relación con la tabla usuarios (contribuyente)
    categoria VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    ubicacion VARCHAR(255) NULL, -- Coordenadas GPS (Lat, Long) o detalle geográfico
    descripcion TEXT NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Restricción de clave foránea hacia la tabla usuarios
    CONSTRAINT fk_reclamos_usuarios 
        FOREIGN KEY (id_usuario_fk) 
        REFERENCES usuarios(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,

    -- Índices para optimización de búsqueda y filtrado
    INDEX idx_reclamos_estado (estado),
    INDEX idx_reclamos_categoria (categoria),
    INDEX idx_reclamos_usuario (id_usuario_fk),
    INDEX idx_reclamos_fecha (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;