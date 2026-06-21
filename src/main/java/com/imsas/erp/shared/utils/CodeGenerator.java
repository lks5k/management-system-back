package com.imsas.erp.shared.utils;

/**
 * Utilidad para generar y derivar códigos de solicitud y arte según las reglas de negocio
 * RN-04, RN-05, RN-06, RN-07 y RN-18.
 *
 * <h3>Formato de códigos</h3>
 * <ul>
 *   <li><b>Código solicitud:</b> {@code P-[A-Z]\d{5}} — p. ej. {@code P-A00001}</li>
 *   <li><b>Código base de datos:</b> {@code BD-[A-Z]\d{5}} — p. ej. {@code BD-A00001}</li>
 *   <li><b>Código BD versionado:</b> {@code BD-[A-Z]\d{5}-\d{2}} — p. ej. {@code BD-A00001-00}</li>
 *   <li><b>Código arte:</b> {@code ART-[A-Z]\d{4}} — p. ej. {@code ART-A0001}</li>
 *   <li><b>Código arte versionado:</b> {@code ART-[A-Z]\d{4}-\d{2}} — p. ej. {@code ART-A0001-00}</li>
 * </ul>
 *
 * <h3>Codificación letra+dígitos</h3>
 * <p>La letra del código no es un prefijo fijo: es parte del consecutivo codificado.
 * Cada letra cubre un bloque de N posiciones (N = 100 000 para solicitudes, 10 000 para artes).
 * La fórmula es:
 * <pre>
 *   letterIndex = consecutivo / SLOTS_POR_LETRA   → letra = 'A' + letterIndex
 *   digits      = consecutivo % SLOTS_POR_LETRA   → formateado con zero-padding
 * </pre>
 * Ejemplo solicitudes ({@code SLOTS_POR_LETRA = 100 000}):
 * <pre>
 *   consecutivo=1      → A00001
 *   consecutivo=99999  → A99999
 *   consecutivo=100000 → B00000
 * </pre>
 *
 * <h3>Responsabilidades de esta clase</h3>
 * <p>Esta clase <em>solo formatea strings</em>. La obtención del consecutivo global
 * (RN-05) es responsabilidad de la capa de servicio, que lo obtiene de las secuencias
 * {@code seq_solicitud_consecutivo} y {@code seq_arte_consecutivo} en PostgreSQL.
 * Los consecutivos nunca se reinician (RN-05) y los códigos asignados son inmutables (RN-06).
 *
 * <p>Esta clase no puede ser instanciada.
 */
public final class CodeGenerator {

    /** Número de posiciones que cubre cada letra en los códigos de solicitud (A00000–A99999). */
    private static final long SOLICITUD_SLOTS = 100_000L;

    /** Número de posiciones que cubre cada letra en los códigos de arte (A0000–A9999). */
    private static final long ARTE_SLOTS = 10_000L;

    /** Dígitos de la parte numérica en el código de solicitud/BD. */
    private static final int SOLICITUD_DIGITS = 5;

    /** Dígitos de la parte numérica en el código de arte. */
    private static final int ARTE_DIGITS = 4;

    /** Dígitos de la versión (BD y Arte), siempre dos: {@code -00}, {@code -01} … */
    private static final int VERSION_DIGITS = 2;

    /** Número máximo de letras disponibles (A–Z). */
    private static final int LETTERS = 26;

    private CodeGenerator() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar");
    }

    // ─── Generación del código de solicitud (RN-04, RN-05) ───────────────────

    /**
     * Genera el código de solicitud a partir del consecutivo global.
     *
     * <p>Formato: {@code P-[A-Z]\d{5}} — p. ej. {@code P-A00001}, {@code P-B00000}
     *
     * <p>Este método debe invocarse únicamente cuando la solicitud pasa del estado
     * BORRADOR a PENDIENTE (RN-04). El consecutivo lo provee la capa de servicio
     * desde la secuencia {@code seq_solicitud_consecutivo} de PostgreSQL.
     *
     * @param consecutivo número consecutivo global (1 ≤ consecutivo ≤ {@code 26 × 100 000 − 1})
     * @return código de solicitud formateado, p. ej. {@code "P-A00001"}
     * @throws IllegalArgumentException si el consecutivo está fuera del rango válido
     */
    public static String generate(long consecutivo) {
        validateConsecutivo(consecutivo, SOLICITUD_SLOTS, "solicitud");
        return "P-" + encodeConsecutivo(consecutivo, SOLICITUD_SLOTS, SOLICITUD_DIGITS);
    }

    // ─── Derivación del código base de datos (RN-07) ─────────────────────────

    /**
     * Deriva el código base de datos a partir del código de solicitud.
     *
     * <p>RN-07: {@code codigoBaseDatos = "BD-" + sufijo_de_codigo}.
     * El sufijo es la parte que sigue a {@code "P-"}, es decir {@code [A-Z]\d{5}}.
     *
     * <p>Ejemplo: {@code "P-A00001"} → {@code "BD-A00001"}
     *
     * @param codigoSolicitud código de solicitud previamente generado (p. ej. {@code "P-A00001"})
     * @return código base de datos correspondiente
     * @throws IllegalArgumentException si el código no tiene el formato esperado
     */
    public static String deriveCodigoBaseDatos(String codigoSolicitud) {
        validateCodigoSolicitud(codigoSolicitud);
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
     * <p>Ejemplo: {@code "BD-A00001"}, versión {@code 0} → {@code "BD-A00001-00"}
     *
     * @param codigoBaseDatos código BD sin versión (p. ej. {@code "BD-A00001"})
     * @param version         número de versión (base 0; máximo 99)
     * @return código BD versionado (p. ej. {@code "BD-A00001-00"})
     * @throws IllegalArgumentException si el código BD no tiene el formato esperado
     *                                  o si la versión está fuera del rango [0, 99]
     */
    public static String buildVersionedCodigoBd(String codigoBaseDatos, int version) {
        validateCodigoBaseDatos(codigoBaseDatos);
        validateVersion(version);
        return String.format("%s-%0" + VERSION_DIGITS + "d", codigoBaseDatos, version);
    }

    /**
     * Genera el código BD versionado inicial ({@code -00}) directamente desde el
     * código de solicitud. Combina {@link #deriveCodigoBaseDatos} y
     * {@link #buildVersionedCodigoBd}.
     *
     * <p>Ejemplo: {@code "P-A00001"} → {@code "BD-A00001-00"}
     *
     * @param codigoSolicitud código de solicitud
     * @return código BD con versión inicial {@code -00}
     */
    public static String initialVersionedCodigoBd(String codigoSolicitud) {
        return buildVersionedCodigoBd(deriveCodigoBaseDatos(codigoSolicitud), 0);
    }

    // ─── Generación y versionado del código Arte (RN-18) ─────────────────────

    /**
     * Genera el código de arte a partir del consecutivo de arte.
     *
     * <p>Formato: {@code ART-[A-Z]\d{4}} — p. ej. {@code ART-A0001}
     *
     * <p>El consecutivo lo provee la capa de servicio desde la secuencia
     * {@code seq_arte_consecutivo} de PostgreSQL.
     *
     * @param consecutivoArte número consecutivo global de arte (1 ≤ valor ≤ {@code 26 × 10 000 − 1})
     * @return código de arte formateado, p. ej. {@code "ART-A0001"}
     * @throws IllegalArgumentException si el consecutivo está fuera del rango válido
     */
    public static String generateCodigoArte(long consecutivoArte) {
        validateConsecutivo(consecutivoArte, ARTE_SLOTS, "arte");
        return "ART-" + encodeConsecutivo(consecutivoArte, ARTE_SLOTS, ARTE_DIGITS);
    }

    /**
     * Construye el código Arte versionado a partir del código Arte y el número de versión.
     *
     * <p>Ejemplo: {@code "ART-A0001"}, versión {@code 0} → {@code "ART-A0001-00"}
     *
     * @param codigoArte código arte sin versión (p. ej. {@code "ART-A0001"})
     * @param version    número de versión (base 0; máximo 99)
     * @return código arte versionado (p. ej. {@code "ART-A0001-00"})
     * @throws IllegalArgumentException si el código arte no tiene el formato esperado
     *                                  o si la versión está fuera del rango [0, 99]
     */
    public static String buildVersionedCodigoArte(String codigoArte, int version) {
        validateCodigoArte(codigoArte);
        validateVersion(version);
        return String.format("%s-%0" + VERSION_DIGITS + "d", codigoArte, version);
    }

    // ─── Lógica interna de codificación ──────────────────────────────────────

    /**
     * Codifica un consecutivo en formato letra+dígitos con zero-padding.
     *
     * @param consecutivo valor a codificar (≥ 0)
     * @param slots       número de posiciones por letra (p. ej. 100 000)
     * @param digits      cantidad de dígitos con zero-padding
     * @return cadena del tipo {@code "A00001"}
     */
    private static String encodeConsecutivo(long consecutivo, long slots, int digits) {
        int letterIndex = (int) (consecutivo / slots);
        long numericPart = consecutivo % slots;
        char letter = (char) ('A' + letterIndex);
        return String.format("%c%0" + digits + "d", letter, numericPart);
    }

    // ─── Validaciones internas ────────────────────────────────────────────────

    /**
     * Valida que el consecutivo esté dentro del rango soportado por el esquema letra+dígitos.
     *
     * @param consecutivo valor a validar
     * @param slots       posiciones por letra
     * @param tipo        nombre del tipo ("solicitud" o "arte"), usado en el mensaje de error
     * @throws IllegalArgumentException si el consecutivo es inválido
     */
    private static void validateConsecutivo(long consecutivo, long slots, String tipo) {
        long maxExclusive = (long) LETTERS * slots;
        if (consecutivo <= 0 || consecutivo >= maxExclusive) {
            throw new IllegalArgumentException(
                    "Consecutivo de " + tipo + " fuera de rango [1, " + (maxExclusive - 1) + "]: "
                    + consecutivo);
        }
    }

    /**
     * Valida que el número de versión esté en [0, 99].
     *
     * @param version número de versión
     * @throws IllegalArgumentException si la versión no está en el rango
     */
    private static void validateVersion(int version) {
        if (version < 0 || version > 99) {
            throw new IllegalArgumentException(
                    "La versión debe estar entre 0 y 99. Recibido: " + version);
        }
    }

    /**
     * Valida que el código de solicitud tenga el formato {@code P-[A-Z]\d{5}}.
     *
     * @param codigo código a validar
     * @throws IllegalArgumentException si el formato no es válido
     */
    private static void validateCodigoSolicitud(String codigo) {
        if (codigo == null || !codigo.matches("^P-[A-Z]\\d{5}$")) {
            throw new IllegalArgumentException(
                    "Formato de código de solicitud inválido: '" + codigo
                    + "'. Esperado: P-[A-Z]\\d{5} (p. ej. P-A00001)");
        }
    }

    /**
     * Valida que el código BD tenga el formato {@code BD-[A-Z]\d{5}}.
     *
     * @param codigo código a validar
     * @throws IllegalArgumentException si el formato no es válido
     */
    private static void validateCodigoBaseDatos(String codigo) {
        if (codigo == null || !codigo.matches("^BD-[A-Z]\\d{5}$")) {
            throw new IllegalArgumentException(
                    "Formato de código BD inválido: '" + codigo
                    + "'. Esperado: BD-[A-Z]\\d{5} (p. ej. BD-A00001)");
        }
    }

    /**
     * Valida que el código arte tenga el formato {@code ART-[A-Z]\d{4}}.
     *
     * @param codigo código a validar
     * @throws IllegalArgumentException si el formato no es válido
     */
    private static void validateCodigoArte(String codigo) {
        if (codigo == null || !codigo.matches("^ART-[A-Z]\\d{4}$")) {
            throw new IllegalArgumentException(
                    "Formato de código arte inválido: '" + codigo
                    + "'. Esperado: ART-[A-Z]\\d{4} (p. ej. ART-A0001)");
        }
    }
}
