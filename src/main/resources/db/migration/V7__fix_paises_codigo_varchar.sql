-- V5 creó paises.codigo como CHAR(2), que rellena con espacios cuando el valor
-- tiene menos de 2 caracteres. VARCHAR(2) es más preciso y evita ese comportamiento.
ALTER TABLE paises ALTER COLUMN codigo TYPE VARCHAR(2);
