package com.imsas.erp.shared.utils;

public final class CodeGenerator {

    
    private static final long SOLICITUD_SLOTS = 100_000L;

    
    private static final long ARTE_SLOTS = 10_000L;

    
    private static final int SOLICITUD_DIGITS = 5;

    
    private static final int ARTE_DIGITS = 4;

    
    private static final int VERSION_DIGITS = 2;

    
    private static final int LETTERS = 26;

    private CodeGenerator() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar");
    }

    // ─── Generación del código de solicitud (RN-04, RN-05) ───────────────────

    
    public static String generate(long consecutivo) {
        validateConsecutivo(consecutivo, SOLICITUD_SLOTS, "solicitud");
        return "P-" + encodeConsecutivo(consecutivo, SOLICITUD_SLOTS, SOLICITUD_DIGITS);
    }

    // ─── Derivación del código base de datos (RN-07) ─────────────────────────

    
    public static String deriveCodigoBaseDatos(String codigoSolicitud) {
        validateCodigoSolicitud(codigoSolicitud);
        String suffix = codigoSolicitud.substring("P-".length());
        return "BD-" + suffix;
    }

    // ─── Versionado del código BD (RN-07, RN-08) ─────────────────────────────

    
    public static String buildVersionedCodigoBd(String codigoBaseDatos, int version) {
        validateCodigoBaseDatos(codigoBaseDatos);
        validateVersion(version);
        return String.format("%s-%0" + VERSION_DIGITS + "d", codigoBaseDatos, version);
    }

    
    public static String initialVersionedCodigoBd(String codigoSolicitud) {
        return buildVersionedCodigoBd(deriveCodigoBaseDatos(codigoSolicitud), 0);
    }

    // ─── Generación y versionado del código Arte (RN-18) ─────────────────────

    
    public static String generateCodigoArte(long consecutivoArte) {
        validateConsecutivo(consecutivoArte, ARTE_SLOTS, "arte");
        return "ART-" + encodeConsecutivo(consecutivoArte, ARTE_SLOTS, ARTE_DIGITS);
    }

    
    public static String buildVersionedCodigoArte(String codigoArte, int version) {
        validateCodigoArte(codigoArte);
        validateVersion(version);
        return String.format("%s-%0" + VERSION_DIGITS + "d", codigoArte, version);
    }

    // ─── Lógica interna de codificación ──────────────────────────────────────

    
    private static String encodeConsecutivo(long consecutivo, long slots, int digits) {
        int letterIndex = (int) (consecutivo / slots);
        long numericPart = consecutivo % slots;
        char letter = (char) ('A' + letterIndex);
        return String.format("%c%0" + digits + "d", letter, numericPart);
    }

    // ─── Validaciones internas ────────────────────────────────────────────────

    
    private static void validateConsecutivo(long consecutivo, long slots, String tipo) {
        long maxExclusive = (long) LETTERS * slots;
        if (consecutivo <= 0 || consecutivo >= maxExclusive) {
            throw new IllegalArgumentException(
                    "Consecutivo de " + tipo + " fuera de rango [1, " + (maxExclusive - 1) + "]: "
                    + consecutivo);
        }
    }

    
    private static void validateVersion(int version) {
        if (version < 0 || version > 99) {
            throw new IllegalArgumentException(
                    "La versión debe estar entre 0 y 99. Recibido: " + version);
        }
    }

    
    private static void validateCodigoSolicitud(String codigo) {
        if (codigo == null || !codigo.matches("^P-[A-Z]\\d{5}$")) {
            throw new IllegalArgumentException(
                    "Formato de código de solicitud inválido: '" + codigo
                    + "'. Esperado: P-[A-Z]\\d{5} (p. ej. P-A00001)");
        }
    }

    
    private static void validateCodigoBaseDatos(String codigo) {
        if (codigo == null || !codigo.matches("^BD-[A-Z]\\d{5}$")) {
            throw new IllegalArgumentException(
                    "Formato de código BD inválido: '" + codigo
                    + "'. Esperado: BD-[A-Z]\\d{5} (p. ej. BD-A00001)");
        }
    }

    
    private static void validateCodigoArte(String codigo) {
        if (codigo == null || !codigo.matches("^ART-[A-Z]\\d{4}$")) {
            throw new IllegalArgumentException(
                    "Formato de código arte inválido: '" + codigo
                    + "'. Esperado: ART-[A-Z]\\d{4} (p. ej. ART-A0001)");
        }
    }
}
