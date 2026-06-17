package com.imsas.erp.shared.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Excepción lanzada cuando una entidad no se encuentra en la base de datos.
 *
 * <p>Produce automáticamente una respuesta HTTP 404 con el código interno
 * {@code "ENTITY_NOT_FOUND"} y un mensaje descriptivo.
 *
 * <pre>{@code
 * // Por UUID:
 * throw new EntityNotFoundException("Usuario", id);
 *
 * // Por campo arbitrario:
 * throw new EntityNotFoundException("Empresa", "nit", "900123456");
 * }</pre>
 */
public class EntityNotFoundException extends BusinessException {

    /**
     * Crea la excepción indicando la entidad y su UUID.
     *
     * @param entityName nombre de la entidad (p. ej. {@code "Usuario"})
     * @param id         UUID por el que se buscó
     */
    public EntityNotFoundException(String entityName, UUID id) {
        super(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND",
                entityName + " no encontrado con id: " + id);
    }

    /**
     * Crea la excepción indicando la entidad, el campo y el valor buscado.
     *
     * @param entityName nombre de la entidad
     * @param field      nombre del campo (p. ej. {@code "codigo"})
     * @param value      valor que no produjo resultados
     */
    public EntityNotFoundException(String entityName, String field, String value) {
        super(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND",
                entityName + " no encontrado con " + field + ": " + value);
    }
}
