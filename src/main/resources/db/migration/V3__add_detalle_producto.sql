-- Agrega columna detalle_producto a la tabla solicitudes.
-- Permite registrar variantes o especificaciones libres del producto sin
-- crear entradas nuevas en la tabla productos.
ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS detalle_producto VARCHAR(200);
