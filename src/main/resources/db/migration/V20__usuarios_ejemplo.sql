-- V20 · Usuarios de ejemplo (frente B)
-- Contraseña de los 4 usuarios: Knows#2026  (guardada con BCrypt, nunca en texto plano)

INSERT INTO usuario (nombre, apellidos, correo, password_hash, telefono, region, comuna, direccion, rol, estado) VALUES
('Benjamín', 'Encina',    'benjamin@knowsis.cl', '$2a$10$6Uw9UvrjlI6Wtqesxk1PwuqsS0j9kMTycGOM6nvBVK4Dn/fgfQWqq', NULL, NULL, NULL, NULL, 'Admin',  'Activo'),
('Kevin',    'Pincheira', 'kevin@knowsis.cl',    '$2a$10$6Uw9UvrjlI6Wtqesxk1PwuqsS0j9kMTycGOM6nvBVK4Dn/fgfQWqq', NULL, NULL, NULL, NULL, 'Admin',  'Activo'),
('Ghislane', 'Carrasco',  'ghislane@knowsis.cl', '$2a$10$6Uw9UvrjlI6Wtqesxk1PwuqsS0j9kMTycGOM6nvBVK4Dn/fgfQWqq', NULL, NULL, NULL, NULL, 'Admin',  'Activo'),
('Alumno',   'Demo',      'alumno@knowsis.cl',   '$2a$10$6Uw9UvrjlI6Wtqesxk1PwuqsS0j9kMTycGOM6nvBVK4Dn/fgfQWqq', NULL, NULL, NULL, NULL, 'Alumno', 'Activo');