-- ============================================================================
-- V1 · Esquema base de Knows-IS (MySQL 8)
-- Solo estructura. Los datos de ejemplo los cargan los frentes en sus propias migraciones:
--   V10 (cursos y categorías) · V20 (usuarios) · V30 (órdenes)
-- Regla: una migración que ya se subió a GitHub NO se edita; los cambios van en una versión nueva.
-- ============================================================================

CREATE TABLE categoria (
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    slug     VARCHAR(40) NOT NULL,
    nombre   VARCHAR(80) NOT NULL,
    en_menu  BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT uk_categoria_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE curso (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    codigo        VARCHAR(20)  NOT NULL,
    nombre        VARCHAR(150) NOT NULL,
    descripcion   VARCHAR(500) NOT NULL,
    precio        INT          NOT NULL,             -- pesos chilenos, sin decimales
    cupos         INT          NOT NULL,             -- cupos TOTALES (los ocupados se calculan desde las órdenes)
    cupo_critico  INT          NOT NULL,
    categoria_id  BIGINT       NOT NULL,
    nivel         VARCHAR(20)  NOT NULL,             -- Inicial | Intermedio | Avanzado
    instructor    VARCHAR(100) NOT NULL,
    imagen        VARCHAR(500),
    icono         VARCHAR(60),
    destacado     BOOLEAN      NOT NULL DEFAULT FALSE,
    nuevo         BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT uk_curso_codigo UNIQUE (codigo),
    CONSTRAINT fk_curso_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT ck_curso_precio CHECK (precio >= 0),
    CONSTRAINT ck_curso_cupos CHECK (cupos >= 0),
    CONSTRAINT ck_curso_critico CHECK (cupo_critico >= 0 AND cupo_critico <= cupos)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuario (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(100) NOT NULL,
    apellidos      VARCHAR(100),
    correo         VARCHAR(100) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,            -- hash BCrypt, nunca la contraseña real
    telefono       VARCHAR(20),
    region         VARCHAR(80),
    comuna         VARCHAR(80),
    direccion      VARCHAR(300),
    rut            VARCHAR(12),
    rol            VARCHAR(12)  NOT NULL,            -- Admin | Instructor | Alumno
    estado         VARCHAR(10)  NOT NULL DEFAULT 'Activo',   -- Activo | Inactivo
    creado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('Admin', 'Instructor', 'Alumno')),
    CONSTRAINT ck_usuario_estado CHECK (estado IN ('Activo', 'Inactivo'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_token (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id  BIGINT      NOT NULL,
    token_hash  VARCHAR(64) NOT NULL,                -- SHA-256 en hexadecimal: el token real nunca se guarda
    expira_en   DATETIME    NOT NULL,
    revocado    BOOLEAN     NOT NULL DEFAULT FALSE,
    creado_en   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orden (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    numero             VARCHAR(12)  NOT NULL,         -- KS-00001 (es el "id" que ve el frontend)
    usuario_id         BIGINT       NOT NULL,
    fecha              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal           INT          NOT NULL,
    descuento          INT          NOT NULL DEFAULT 0,
    total              INT          NOT NULL,
    cuotas             INT          NOT NULL,         -- 1, 3 o 6
    primera_cuota      INT          NOT NULL,
    valor_cuota        INT          NOT NULL,
    estado             VARCHAR(10)  NOT NULL DEFAULT 'Pagada',   -- Pagada | Anulada
    factura_nombre     VARCHAR(100) NOT NULL,
    factura_rut        VARCHAR(12)  NOT NULL,
    factura_correo     VARCHAR(100) NOT NULL,
    factura_telefono   VARCHAR(20),
    factura_direccion  VARCHAR(300) NOT NULL,
    pago_medio         VARCHAR(40)  NOT NULL,         -- "Tarjeta •••• 4242" (solo los últimos 4 dígitos)
    pago_referencia    VARCHAR(40)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_orden_numero UNIQUE (numero),
    CONSTRAINT fk_orden_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_orden_estado CHECK (estado IN ('Pagada', 'Anulada')),
    CONSTRAINT ck_orden_cuotas CHECK (cuotas IN (1, 3, 6)),
    CONSTRAINT ck_orden_montos CHECK (subtotal >= 0 AND descuento >= 0 AND total >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orden_item (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    orden_id    BIGINT       NOT NULL,
    curso_id    BIGINT       NOT NULL,
    -- copia de los datos del curso al momento de comprar: el historial no cambia si el curso se edita
    codigo      VARCHAR(20)  NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    instructor  VARCHAR(100) NOT NULL,
    categoria   VARCHAR(40)  NOT NULL,
    precio      INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_orden_item UNIQUE (orden_id, curso_id),
    CONSTRAINT fk_orden_item_orden FOREIGN KEY (orden_id) REFERENCES orden (id) ON DELETE CASCADE,
    CONSTRAINT fk_orden_item_curso FOREIGN KEY (curso_id) REFERENCES curso (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_curso_categoria ON curso (categoria_id);
CREATE INDEX idx_orden_usuario ON orden (usuario_id);
CREATE INDEX idx_orden_item_curso ON orden_item (curso_id);
CREATE INDEX idx_refresh_token_usuario ON refresh_token (usuario_id);
