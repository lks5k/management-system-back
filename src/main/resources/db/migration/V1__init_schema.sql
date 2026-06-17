-- =============================================================================
-- V1__init_schema.sql
-- Schema inicial del Sistema de Gestión de Producción — IMSAS V1
-- Referencia: MODELO DE DATOS V1.1
-- =============================================================================

-- ─── Secuencia global de solicitudes (RN-05) ─────────────────────────────────
-- Nunca se reinicia. La capa de servicio la consulta para generar el código
-- al pasar una solicitud de BORRADOR a PENDIENTE (RN-04).
CREATE SEQUENCE IF NOT EXISTS seq_solicitud_consecutivo
    START WITH 1
    INCREMENT BY 1
    NO CYCLE;

-- ─── usuarios ────────────────────────────────────────────────────────────────
CREATE TABLE usuarios (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    nombre        VARCHAR(120) NOT NULL,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol           VARCHAR(20)  NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_usuarios
        PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_email
        UNIQUE (email),
    CONSTRAINT chk_usuarios_rol
        CHECK (rol IN ('SUPERADMIN','ADMIN','MANAGER','OPERATOR','OFFICER','SALES_REP'))
);

-- ─── empresas ────────────────────────────────────────────────────────────────
CREATE TABLE empresas (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    tipo_documento       VARCHAR(10)  NOT NULL,
    numero_documento     VARCHAR(30)  NOT NULL,
    razon_social         VARCHAR(200) NOT NULL,
    tipo_empresa         VARCHAR(20)  NOT NULL,
    direccion            VARCHAR(200),
    ciudad               VARCHAR(100),
    telefono             VARCHAR(30),
    sitio_web            VARCHAR(200),
    dia_cierre_contable  SMALLINT,
    pais                 VARCHAR(60)  NOT NULL DEFAULT 'Colombia',
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por           UUID,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_empresas
        PRIMARY KEY (id),
    CONSTRAINT uq_empresa_numero_documento
        UNIQUE (numero_documento),
    CONSTRAINT chk_empresas_tipo_documento
        CHECK (tipo_documento IN ('NIT','CC','CE','PASAPORTE')),
    CONSTRAINT chk_empresas_tipo_empresa
        CHECK (tipo_empresa IN ('SAS','SA','LTDA','PERSONA_NATURAL','OTRO')),
    CONSTRAINT chk_dia_cierre
        CHECK (dia_cierre_contable BETWEEN 1 AND 31),
    CONSTRAINT fk_empresa_creado_por
        FOREIGN KEY (creado_por) REFERENCES usuarios (id)
);

-- ─── contactos ───────────────────────────────────────────────────────────────
CREATE TABLE contactos (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    empresa_id     UUID         NOT NULL,
    nombre         VARCHAR(120) NOT NULL,
    cargo          VARCHAR(100),
    telefono       VARCHAR(30),
    es_facturacion BOOLEAN      NOT NULL DEFAULT FALSE,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_contactos
        PRIMARY KEY (id),
    CONSTRAINT fk_contacto_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresas (id)
);

-- ─── contacto_emails ─────────────────────────────────────────────────────────
CREATE TABLE contacto_emails (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    contacto_id  UUID         NOT NULL,
    email        VARCHAR(120) NOT NULL,
    es_principal BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_contacto_emails
        PRIMARY KEY (id),
    CONSTRAINT uq_contacto_email
        UNIQUE (contacto_id, email),
    CONSTRAINT fk_contacto_email_contacto
        FOREIGN KEY (contacto_id) REFERENCES contactos (id)
);

-- ─── marcas ──────────────────────────────────────────────────────────────────
CREATE TABLE marcas (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    nombre      VARCHAR(120) NOT NULL,
    empresa_id  UUID         NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_marcas
        PRIMARY KEY (id),
    CONSTRAINT uq_marca_nombre_empresa
        UNIQUE (nombre, empresa_id),
    CONSTRAINT fk_marca_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresas (id)
);

-- ─── productos ───────────────────────────────────────────────────────────────
CREATE TABLE productos (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    nombre     VARCHAR(120) NOT NULL,
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_productos
        PRIMARY KEY (id),
    CONSTRAINT uq_producto_nombre
        UNIQUE (nombre)
);

-- ─── solicitudes ─────────────────────────────────────────────────────────────
CREATE TABLE solicitudes (
    id                      UUID          NOT NULL DEFAULT gen_random_uuid(),
    codigo                  VARCHAR(20)   UNIQUE,
    codigo_base_datos       VARCHAR(20)   UNIQUE,
    version_bd              SMALLINT      NOT NULL DEFAULT 0,
    motivo_version_bd       TEXT,
    solicitud_origen_id     UUID,
    asesor_id               UUID          NOT NULL,
    empresa_id              UUID          NOT NULL,
    contacto_id             UUID,
    marca_id                UUID,
    producto_id             UUID,
    tipo_solicitud          VARCHAR(20)   NOT NULL,
    descripcion             TEXT,
    cantidad                INTEGER       NOT NULL CHECK (cantidad > 0),
    tecnica                 VARCHAR(20)   NOT NULL,
    ancho                   NUMERIC(8,2),
    largo                   NUMERIC(8,2),
    colores_frente          SMALLINT,
    colores_reverso         SMALLINT,
    costura                 VARCHAR(20),
    orden_compra            VARCHAR(60),
    precio_referencia       NUMERIC(12,2),
    abono                   NUMERIC(12,2) DEFAULT 0,
    observaciones           TEXT,
    estado                  VARCHAR(20)   NOT NULL DEFAULT 'BORRADOR',
    observacion_cancelacion TEXT,
    activo                  BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_solicitudes
        PRIMARY KEY (id),
    CONSTRAINT chk_solicitudes_tipo
        CHECK (tipo_solicitud IN ('DISEÑO','MUESTRAS','PEDIDO','REPOSICION','CORTE','PRESTAMO')),
    CONSTRAINT chk_solicitudes_estado
        CHECK (estado IN ('BORRADOR','CONFIRMAR','PENDIENTE','COMPLETADO','CANCELADO')),
    CONSTRAINT chk_solicitudes_tecnica
        CHECK (tecnica IN ('DIGITAL','SUBLIMACION','FLEXOGRAFIA','LITOGRAFIA','SCREEN','TEJIDA','TRANSFER')),
    CONSTRAINT chk_solicitudes_costura
        CHECK (costura IN ('ARRIBA','ABAJO','IZQUIERDA','DERECHA','PARA_DOBLAR','TODOS_LOS_LADOS','SIN_COSTURA','TRAPECIO','A_DOS_LADOS')),
    CONSTRAINT fk_solicitud_asesor
        FOREIGN KEY (asesor_id)          REFERENCES usuarios  (id),
    CONSTRAINT fk_solicitud_empresa
        FOREIGN KEY (empresa_id)         REFERENCES empresas  (id),
    CONSTRAINT fk_solicitud_contacto
        FOREIGN KEY (contacto_id)        REFERENCES contactos (id),
    CONSTRAINT fk_solicitud_marca
        FOREIGN KEY (marca_id)           REFERENCES marcas    (id),
    CONSTRAINT fk_solicitud_producto
        FOREIGN KEY (producto_id)        REFERENCES productos (id),
    CONSTRAINT fk_solicitud_origen
        FOREIGN KEY (solicitud_origen_id) REFERENCES solicitudes (id)
);

-- ─── Índices ──────────────────────────────────────────────────────────────────
CREATE INDEX idx_solicitudes_estado     ON solicitudes (estado);
CREATE INDEX idx_solicitudes_empresa    ON solicitudes (empresa_id);
CREATE INDEX idx_solicitudes_asesor     ON solicitudes (asesor_id);
CREATE INDEX idx_solicitudes_created_at ON solicitudes (created_at DESC);
CREATE INDEX idx_solicitudes_origen     ON solicitudes (solicitud_origen_id);
CREATE INDEX idx_contactos_empresa      ON contactos   (empresa_id);
CREATE INDEX idx_marcas_empresa         ON marcas      (empresa_id);
CREATE INDEX idx_contacto_emails_contacto ON contacto_emails (contacto_id);
