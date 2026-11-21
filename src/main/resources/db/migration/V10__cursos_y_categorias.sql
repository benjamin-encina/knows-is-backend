-- V10: DATOS INICIALES DE CATEGORIAS Y CURSOS

INSERT INTO categoria (slug, nombre, en_menu) VALUES ('programacion', 'Programación', TRUE),
                                                     ('redes', 'Redes', TRUE),
                                                     ('diseno', 'Diseño digital', TRUE),
                                                     ('ciberseguridad', 'Ciberseguridad', FALSE),
                                                     ('datos', 'Datos', FALSE),
                                                     ('ia', 'Inteligencia Artificial', FALSE);

-- CURSO 1
INSERT INTO curso (codigo, nombre, descripcion, precio, cupos, cupo_critico, categoria_id,nivel, instructor, icono, destacado, nuevo)
SELECT '10001', 'JavaScript desde cero',
       'Domina la base del desarrollo web moderno, paso a paso.',
       210000, 30, 5, c.id, 'Inicial', 'Kevin Soto', 'bi-code-slash', TRUE, FALSE
FROM categoria c WHERE c.slug = 'programacion';

-- CURSO 2
INSERT INTO curso (codigo, nombre, descripcion, precio, cupos, cupo_critico, categoria_id, nivel, instructor, icono, destacado, nuevo)
SELECT '10002', 'Fundamentos de Ciberseguridad',
       'Conoce las amenazas más comunes y cómo proteger sistemas y datos.',
       450000, 10, 3, c.id, 'Intermedio', 'Benjamín Rojas', 'bi-shield-lock', FALSE, FALSE
FROM categoria c WHERE c.slug = 'ciberseguridad';

-- CURSO 3
INSERT INTO curso (codigo, nombre, descripcion, precio, cupos, cupo_critico, categoria_id, nivel, instructor, icono, destacado, nuevo)
SELECT '10003', 'UX/UI para Producto Digital',
       'Diseña interfaces claras y centradas en la persona usuaria.',
       260000, 30, 5, c.id, 'Inicial', 'Kevin Soto', 'bi-palette', TRUE, FALSE
FROM categoria c WHERE c.slug = 'diseno';

-- CURSO 4
INSERT INTO curso (codigo, nombre, descripcion, precio, cupos, cupo_critico, categoria_id, nivel, instructor, icono, destacado, nuevo)
SELECT '10004', 'Administración de Redes',
       'Configura y administra infraestructura de red desde cero.',
       320000, 25, 5, c.id, 'Intermedio', 'Benjamín Rojas', 'bi-diagram-3', TRUE, FALSE
FROM categoria c WHERE c.slug = 'redes';

-- CURSO 5
INSERT INTO curso (codigo, nombre, descripcion, precio, cupos, cupo_critico, categoria_id, nivel, instructor, icono, destacado, nuevo)
SELECT '10005', 'Ingeniería de Prompts y Agentes IA',
       'Aprende a diseñar prompts y agentes que resuelvan tareas reales.',
       390000, 8, 3, c.id, 'Inicial', 'Kevin Soto', 'bi-robot', FALSE, TRUE
FROM categoria c WHERE c.slug = 'ia';