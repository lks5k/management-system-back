-- =============================================================================
-- V2__schema_updates_v3.sql
-- Actualizaciones de esquema — Modelo de Datos V3.0
-- Referencia: SDD secciones 3, 5 y 6 · Auditoría de conflictos V3.0
-- =============================================================================

-- ─── 1. Eliminar OFFICER del CHECK de rol en usuarios ────────────────────────
-- OFFICER fue eliminado del modelo en V3.0 (SDD sección 3).
-- ALTER TABLE + DROP/ADD es la forma portátil de reemplazar un CHECK en PG.
ALTER TABLE usuarios
    DROP CONSTRAINT chk_usuarios_rol;

ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_rol
        CHECK (rol IN ('SUPERADMIN','ADMIN','MANAGER','OPERATOR','SALES_REP'));

-- ─── 2. Secuencia global de artes (RN-18) ────────────────────────────────────
-- Nunca se reinicia. La capa de servicio la consulta para generar el código
-- de arte al crear un nuevo Arte activo.
CREATE SEQUENCE IF NOT EXISTS seq_arte_consecutivo
    START WITH 1
    INCREMENT BY 1
    NO CYCLE;

-- ─── 3. Tabla artes ──────────────────────────────────────────────────────────
-- Representa el "Arte Base" de una combinación empresa/marca/producto.
-- Solo puede existir un Arte activo (activo = true) por combinación (índice
-- único parcial uq_arte_combinacion_vigente).  Cuando version_actual llega a
-- 99 y se requiere una nueva versión se crea un Arte nuevo y el anterior queda
-- activo = false, permitiendo así múltiples artes históricos por combinación.
CREATE TABLE artes (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    codigo          VARCHAR(15) NOT NULL,
    empresa_id      UUID        NOT NULL,
    marca_id        UUID        NOT NULL,
    producto_id     UUID        NOT NULL,
    version_actual  SMALLINT    NOT NULL DEFAULT 0,
    activo          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_artes
        PRIMARY KEY (id),
    CONSTRAINT uq_artes_codigo
        UNIQUE (codigo),
    CONSTRAINT chk_artes_version
        CHECK (version_actual BETWEEN 0 AND 99),
    CONSTRAINT fk_arte_empresa
        FOREIGN KEY (empresa_id)  REFERENCES empresas  (id),
    CONSTRAINT fk_arte_marca
        FOREIGN KEY (marca_id)    REFERENCES marcas    (id),
    CONSTRAINT fk_arte_producto
        FOREIGN KEY (producto_id) REFERENCES productos (id)
);

-- Índice de búsqueda por combinación (todas las versiones)
CREATE INDEX idx_artes_combinacion
    ON artes (empresa_id, marca_id, producto_id);

-- Garantiza que solo exista UN arte vigente por combinación empresa/marca/producto
CREATE UNIQUE INDEX uq_arte_combinacion_vigente
    ON artes (empresa_id, marca_id, producto_id)
    WHERE activo = true;

-- ─── 4. Nuevas columnas en solicitudes ───────────────────────────────────────
-- Vinculan una solicitud con el Arte al que pertenece y registran cuántas
-- versiones de arte se entregaron en ella (máximo 3 por solicitud).

ALTER TABLE solicitudes
    ADD COLUMN arte_id                        UUID     REFERENCES artes (id),
    ADD COLUMN version_arte_generada          SMALLINT,
    ADD COLUMN cantidad_versiones_entregadas  SMALLINT
        CONSTRAINT chk_solicitudes_cant_versiones CHECK (cantidad_versiones_entregadas <= 3);

CREATE INDEX idx_solicitudes_arte
    ON solicitudes (arte_id);
