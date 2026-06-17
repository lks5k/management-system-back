package com.imsas.erp.shared.utils;

/**
 * Utilidad para generar y derivar códigos de solicitud según las reglas de negocio
 * RN-04, RN-05, RN-06 y RN-07.
 *
 * <h3>Formato de códigos</h3>
 * <ul>
 *   <li><b>Código solicitud:</b> {@code P-S{N}} — p. ej. {@code P-S59644}</li>
 *   <li><b>Código base de datos:</b> {@code BD-S{N}} — p. ej. {@code BD-S59644}</li>
 *   <li><b>Código BD versionado:</b> {@code BD-S{N}-{VV}} — p. ej. {@code BD-S59644-00}</li>
 * </ul>
 *
 * <p>El sufijo compartido entre el código de solicitud y el código BD es {@code S{N}},
 * lo que implementa RN-07: {@code codigoBaseDatos = "BD-" + sufijo_de_codigo}.
 *
 * <h3>Responsabilidades de esta clase</h3>
 * <p>Esta clase <em>solo formatea strings</em>. La obtención del consecutivo global
 * (RN-05) es responsabilidad de la capa de servicio, que lo obtiene de la secuencia
 * {@code seq_solicitud_consecutivo} en PostgreSQL. El consecutivo nunca se reinicia
 * (RN-05) y el código asignado es inmutable (RN-06).
 *
 * <p>Esta clase no puede ser instanciada.
 */
public final class CodeGenerator {

    /** Prefijo fijo del código de solicitud. */
    private static final String SOLICITUD_PREFIX = "P-S";

    /** Prefijo fijo del código base de datos. */
    private static final String BD_PREFIX = "BD-S";

    /** Número de dígitos de la versión BD (siempre dos, p. ej. {@code -00}, {@code -01}). */
    private static final int VERSION_DIGITS = 2;

    private CodeGenerator() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar");
    }

    // ─── Generación del código de solicitud (RN-04, RN-05) ───────────────────

    /**
     * Genera el código de solicitud a partir del consecutivo global.
     *
     * <p>Formato: {@code P-S{N}} — p. ej. {@code P-S59644}
     *
     * <p>Este método debe invocarse únicamente cuando la solicitud pasa del estado
     * BORRADOR a PENDIENTE (RN-04). El consecutivo lo provee la capa de servicio
     * desde la secuencia {@code seq_solicitud_consecutivo} de PostgreSQL.
     *
     * @param consecutivo número consecutivo global (debe ser &gt; 0)
     * @return código de solicitud formateado
     * @throws IllegalArgumentException si el consecutivo es menor o igual a cero
     */
    public static String generate(long consecutivo) {
        if (consecutivo <= 0) {
            throw new IllegalArgumentException(
                    "El consecutivo debe ser mayor a cero. Recibido: " + consecutivo);
        }
        return SOLICITUD_PREFIX + consecutivo;
    }

    // ─── Derivación del código base de datos (RN-07) ─────────────────────────

    /**
     * Deriva el código base de datos a partir del código de solicitud.
     *
     * <p>RN-07: {@code codigoBaseDatos = "BD-" + sufijo_de_codigo}.
     * El sufijo es la parte que sigue a {@code "P-"}, es decir {@code S{N}}.
     *
     * <p>Ejemplo: {@code "P-S59644"} → {@code "BD-S59644"}
     *
     * @param codigoSolicitud código de solicitud previamente generado (p. ej. {@code "P-S59644"})
     * @return código base de datos correspondiente
     * @throws IllegalArgumentException si el código no tiene el formato esperado
     */
    public static String deriveCodigoBaseDatos(String codigoSolicitud) {
        validateCodigoSolicitud(codigoSolicitud);
        // Sufijo: la parte después de "P-", p. ej. "S59644"
        String suffix = codigoSolicitud.substring("P-".length());
        return "BD-" + suffix;
    }

    // ─── Versionado del código BD (RN-07, RN-08) ─────────────────────────────

    /**
     * Construye el código BD versionado a partir del código BD y el número de versión.
     *
     * <p>RN-07: la versión inicial es {@code -00}.
     * <p>RN-08: la versión incrementa solo por corrección estructural con motivo obligatorio.
     *
     * <p>Ejemplo: {@code "BD-S59644"}, versión {@code 0} → {@code "BD-S59644-00"}
     *
     * @param codigoBaseDatos código BD sin versión (p. ej. {@code "BD-S59644"})
     * @param version         número de versión (base 0)
     * @return código BD versionado (p. ej. {@code "BD-S59644-00"})
     * @throws IllegalArgumentException si el código BD no tiene el formato esperado
     *                                  o si la versión es negativa
     */
    public static String buildVersionedCodigoBd(String codigoBaseDatos, int version) {
        validateCodigoBaseDatos(codigoBaseDatos);
        if (version < 0) {
            throw new IllegalArgumentException("La versión no puede ser negativa: " + version);
        }
        return String.format("%s-%0" + VERSION_DIGITS + "d", codigoBaseDatos, version);
    }

    /**
     * Genera el código BD versionado inicial ({@code -00}) directamente desde el
     * código de solicitud. Combina {@link #deriveCodigoBaseDatos} y
     * {@link #buildVersionedCodigoBd}.
     *
     * <p>Ejemplo: {@code "P-S59644"} → {@code "BD-S59644-00"}
     *
     * @param codigoSolicitud código de solicitud
     * @return código BD con versión inicial {@code -00}
     */
    public static String initialVersionedCodigoBd(String codigoSolicitud) {
        return buildVersionedCodigoBd(deriveCodigoBaseDatos(codigoSolicitud), 0);
    }

    // ─── Validación interna ───────────────────────────────────────────────────

    /**
     * Valida que el código de solicitud tenga el formato {@code P-S{N}} (N entero positivo).
     *
     * @param codigo código a validar
     * @throws IllegalArgumentException si el formato no es válido
     */
    private static void validateCodigoSolicitud(String codigo) {
        if (codigo == null || !codigo.matches("^P-S\\d+$")) {
            throw new IllegalArgumentException(
                    "Formato de código de solicitud inválido: '" + codigo + "'. Esperado: P-S{N}");
        }
    }

    /**
     * Valida que el código BD tenga el formato {@code BD-S{N}} (N entero positivo).
     *
     * @param codigo código a validar
     * @throws IllegalArgumentException si el formato no es válido
     */
    private static void validateCodigoBaseDatos(String codigo) {
        if (codigo == null || !codigo.matches("^BD-S\\d+$")) {
            throw new IllegalArgumentException(
                    "Formato de código BD inválido: '" + codigo + "'. Esperado: BD-S{N}");
        }
    }
}
