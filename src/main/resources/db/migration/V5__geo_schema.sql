-- Tablas de referencia geográfica (solo lectura desde la app)
CREATE TABLE paises (
    id      SMALLSERIAL  PRIMARY KEY,
    codigo  CHAR(2)      NOT NULL UNIQUE,  -- ISO 3166-1 alpha-2 (CO, US, etc.)
    nombre  VARCHAR(100) NOT NULL
);

CREATE TABLE departamentos (
    id       SMALLSERIAL  PRIMARY KEY,
    pais_id  SMALLINT     NOT NULL REFERENCES paises(id),
    nombre   VARCHAR(100) NOT NULL,
    UNIQUE (pais_id, nombre)
);

CREATE TABLE ciudades (
    id               SERIAL    PRIMARY KEY,
    departamento_id  SMALLINT  NOT NULL REFERENCES departamentos(id),
    nombre           VARCHAR(100) NOT NULL,
    UNIQUE (departamento_id, nombre)
);

-- Nueva columna en empresas (nullable, no rompe registros existentes)
ALTER TABLE empresas ADD COLUMN departamento VARCHAR(100);
