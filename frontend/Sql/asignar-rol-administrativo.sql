-- Ejecuta después de registrar la cuenta. Sustituye el correo de ejemplo.
-- Este archivo no contiene contraseñas ni asigna roles durante el registro.

UPDATE usuarios
SET rol = 'administrativo',
    privilegios = 'crear_reclamo,seguimiento_reclamo,ver_novedades,administrar_noticias',
    activo = 1
WHERE email = 'reemplazar-por-correo@ejemplo.com';

SELECT ROW_COUNT() AS usuarios_actualizados;
