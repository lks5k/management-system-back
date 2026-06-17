package com.imsas.erp.shared.utils;

import java.time.Year;
import java.util.Map;

/**
 * Utilidad para generar y derivar códigos de solicitud según las reglas de negocio
 * RN-04, RN-05 y RN-07.
 *
 * <h3>Formato de códigos</h3>
 * <ul>
 *   <li><b>Código solicitud:</b> {@code {PREFIX}-{YYYY}-{NNNNN}} — p. ej. {@code DIS-2026-00042}</li>
 *   <li><b>Código base de datos:</b> {@code BD-{YYYY}-{NNNNN}} — p. ej. {@code BD-2026-00042}</li>
 *   <li><b>Código BD versionado:</b> {@code BD-{YYYY}-{NNNNN}-{VV}} — p. ej. {@code BD-2026-00042-00}</li>
 * </ul>
 *
 * <h3>Responsabilidades de esta clase</h3>
 * <p>Esta clase <em>solo formatea strings</em>. La obtención del consecutivo global
 * (RN-05) es responsabilidad de la capa de servicio, que lo obtiene de la secuencia
 * {@code seq_solicitud_consecutivo} en PostgreSQL.
 *
 * <p>Esta clase no puede ser instanciada.
 */
public final class CodeGenerator {

    /** Mapeo de tipo de solicitud (enum name) → prefijo de código. */
    private static final Map<String, String> PREFIX_MAP = Map.of(
            "DISEÑO",     "DIS",
            "MUESTRAS",   "MUE",
            "PEDIDO",     "PED",
            "REPOSICION", "REP",
            "CORTE",      "COR",
            "PRESTAMO",   "PRE"
    );

    private static final int CONSECUTIVE_DIGITS = 5;
    private static final int VERSION_DIGITS      = 2;
    private static final String BD_PREFIX         = "BD";

    private CodeGenerator() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar");
    }

    // ─── Generación de código de solicitud ────────────────────────────────────

    /**
     * Genera el código de una solicitud a partir de su tipo, el año en curso y el
     * consecutivo global.
     *
     * <p>Formato: {@code {PREFIX}-{YYYY}-{NNNNN}}
     *
     * @param tipoSolicitud nombre del enum {@code TipoSolicitud} (p. ej. {@code "DISEÑO"})
     * @param consecutivo   número consecutivo global obtenido de la secuencia de BD
     * @return código formateado (p. ej. {@code "DIS-2026-00042"})
     * @throws IllegalArgumentException si el tipo de solicitud no tiene prefijo definido
     */
    public static String generate(String tipoSolicitud, long consecutivo) {
        return generate(tipoSolicitud, Year.now().getValue(), consecutivo);
    }

    /**
     * Genera el código de una solicitud con año explícito (útil en migraciones y tests).
     *
     * @param tipoSolicitud nombre del enum {@code TipoSolicitud}
     * @param year          año de cuatro dígitos
     * @param consecutivo   número consecutivo global
     * @return código formateado
     * @throws IllegalArgumentException si el tipo de solicitud no tiene prefijo definido
     */
    public static String generate(String tipoSolicitud, int year, long consecutivo) {
        String prefix = PREFIX_MAP.get(tipoSolicitud.toUpperCase());
        if (prefix == null) {
            throw new IllegalArgumentException(
                    "Tipo de solicitud sin prefijo definido: " + tipoSolicitud);
        }
        return String.format("%s-%d-%0" + CONSECUTIVE_DIGITS + "d", prefix, year, consecutivo);
    }

    // ─── Derivación de código base de datos (RN-07) ───────────────────────────

    /**
     * Deriva el código base de datos a partir del código de solicitud.
     *
     * <p>RN-07: {@code codigoBaseDatos = "BD-" + sufijo_de_codigo}, donde el sufijo
     * es la parte del código que sigue al prefijo de tipo ({@code YYYY-NNNNN}).
     *
     * <p>Ejemplo: {@code "DIS-2026-00042"} → {@code "BD-2026-00042"}
     *
     * @param codigoSolicitud código de solicitud previamente generado
     * @return código base de datos correspondiente
     * @throws IllegalArgumentException si el código no tiene el formato esperado
     */
    public static String deriveCodigoBaseDatos(String codigoSolicitud) {
        validateCodigoFormat(codigoSolicitud);
        // El sufijo empieza después del primer guión: "DIS-2026-00042" → "2026-00042"
        String suffix = codigoSolicitud.substring(codigoSolicitud.indexOf('-') + 1);
        return BD_PREFIX + "-" + suffix;
    }

    // ─── Versionado de código BD (RN-07, RN-08) ──────────────────────────────

    /**
     * Construye el código BD versionado a partir del código BD y el número de versión.
     *
     * <p>RN-07: la versión inicial es {@code -00}.
     * <p>RN-08: la versión incrementa en uno con cada cambio que requiera motivo.
     *
     * <p>Ejemplo: {@code "BD-2026-00042"}, versión {@code 0} → {@code "BD-2026-00042-00"}
     *
     * @param codigoBaseDatos código BD sin versión (p. ej. {@code "BD-2026-00042"})
     * @param version         número de versión (base 0)
     * @return código BD versionado (p. ej. {@code "BD-2026-00042-00"})
     */
    public static String buildVersionedCodigoBd(String codigoBaseDatos, int version) {
        return String.format("%s-%0" + VERSION_DIGITS + "d", codigoBaseDatos, version);
    }

    /**
     * Genera el código BD versionado inicial ({@code -00}) a partir del código de solicitud.
     * Combina {@link #deriveCodigoBaseDatos} y {@link #buildVersionedCodigoBd}.
     *
     * @param codigoSolicitud código de solicitud
     * @return código BD con versión {@code -00}
     */
    public static String initialVersionedCodigoBd(String codigoSolicitud) {
        return buildVersionedCodigoBd(deriveCodigoBaseDatos(codigoSolicitud), 0);
    }

    // ─── Validación interna ───────────────────────────────────────────────────

    /**
     * Valida que el código tenga el formato {@code XXX-YYYY-NNNNN}.
     *
     * @param codigo código a validar
     * @throws IllegalArgumentException si el formato no es válido
     */
    private static void validateCodigoFormat(String codigo) {
        if (codigo == null || !codigo.matches("^[A-Z]{2,3}-\\d{4}-\\d{" + CONSECUTIVE_DIGITS + "}$")) {
            throw new IllegalArgumentException(
                    "Formato de código inválido: '" + codigo + "'. Esperado: PREFIX-YYYY-NNNNN");
        }
    }
}
