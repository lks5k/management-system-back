package com.imsas.erp.modules.geo;

/**
 * DTO de solo lectura para listas geográficas de referencia.
 * Devuelve únicamente id y nombre para poblar selects en cascada.
 */
public record GeoItemResponse(Number id, String nombre) {

    public static GeoItemResponse from(Pais p) {
        return new GeoItemResponse(p.getId(), p.getNombre());
    }

    public static GeoItemResponse from(Departamento d) {
        return new GeoItemResponse(d.getId(), d.getNombre());
    }

    public static GeoItemResponse from(Ciudad c) {
        return new GeoItemResponse(c.getId(), c.getNombre());
    }
}
