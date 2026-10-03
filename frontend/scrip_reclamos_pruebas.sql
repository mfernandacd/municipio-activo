USE municipalidad_serranoble;

-- Inserción de un reclamo de prueba asociado al contribuyente de ejemplo
INSERT INTO reclamos (
    codigo_seguimiento, 
    id_usuario_fk, 
    categoria, 
    direccion, 
    ubicacion, 
    descripcion, 
    estado
) 
VALUES (
    'REC-2026-0001', 
    (SELECT id FROM usuarios WHERE email = 'juan.perez@ejemplo.com'), 
    'Alumbrado Público', 
    'Av. San Martín 1234', 
    '-31.4167,-64.1833', 
    'Foco quemado en poste de luz de la esquina desde hace 3 días.', 
    'Pendiente'
);

-- Consulta de verificación por código de seguimiento con datos del contribuyente
SELECT 
    r.codigo_seguimiento,
    r.categoria,
    r.direccion,
    r.ubicacion,
    r.descripcion,
    r.estado,
    r.fecha_creacion,
    u.email AS contribuyente_email
FROM reclamos r
INNER JOIN usuarios u ON r.id_usuario_fk = u.id
WHERE r.codigo_seguimiento = 'REC-2026-0001';