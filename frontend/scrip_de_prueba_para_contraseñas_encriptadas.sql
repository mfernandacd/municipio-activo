USE municipalidad_serranoble;

-- Inserción de un usuario contribuyente de prueba
-- Nota: La contraseña en texto plano para este ejemplo es "Contribuyente123!"
-- El hash mostrado abajo corresponde a BCrypt con cost factor 10.
INSERT INTO usuarios (email, password_hash, id_rol_fk) 
VALUES (
    'juan.perez@ejemplo.com', 
    '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
    (SELECT id FROM roles WHERE nombre_rol = 'contribuyente')
);

-- Consulta de verificación con JOIN
SELECT 
    u.id AS usuario_id, 
    u.email, 
    r.nombre_rol AS rol, 
    u.creado_en 
FROM usuarios u
INNER JOIN roles r ON u.id_rol_fk = r.id
WHERE u.email = 'juan.perez@ejemplo.com';