package com.imsas.erp.shared.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad centralizada para operaciones de fecha y hora.
 *
 * <p>Todas las marcas de tiempo se almacenan en UTC en la base de datos.
 * La zona horaria de Colombia ({@code America/Bogota}, UTC-5 sin horario de verano)
 * se usa únicamente para presentación en logs y mensajes de error.
 *
 * <p>Esta clase no puede ser instanciada.
 */
public final class DateUtils {

    /** Zona horaria UTC, usada para almacenamiento en BD y timestamps de la API. */
    public static final ZoneId UTC = ZoneId.of("UTC");

    /** Zona horaria de Colombia (UTC-5, sin horario de verano). */
    public static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");

    /** Formato estándar ISO-8601 para presentación de fechas con hora. */
    public static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(UTC);

    /** Formato de fecha sin hora para el campo {@code fechaEntrega} de solicitudes. */
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(COLOMBIA);

    private DateUtils() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar");
    }

    // ─── Obtener instante actual ──────────────────────────────────────────────

    /**
     * Retorna el instante actual del sistema en UTC.
     * Equivalente a {@link Instant#now()} pero semánticamente explícito.
     *
     * @return instante actual en UTC
     */
    public static Instant nowUtc() {
        return Instant.now();
    }

    /**
     * Retorna la fecha y hora actuales en la zona horaria de Colombia.
     *
     * @return {@link LocalDateTime} en zona {@code America/Bogota}
     */
    public static LocalDateTime nowColombia() {
        return LocalDateTime.now(COLOMBIA);
    }

    /**
     * Retorna la fecha actual en la zona horaria de Colombia (sin hora).
     *
     * @return {@link LocalDate} en zona {@code America/Bogota}
     */
    public static LocalDate todayColombia() {
        return LocalDate.now(COLOMBIA);
    }

    // ─── Conversión entre zonas ───────────────────────────────────────────────

    /**
     * Convierte un {@link Instant} UTC a {@link ZonedDateTime} en zona Colombia.
     * Útil para mostrar fechas en logs o mensajes de error orientados al usuario.
     *
     * @param instant instante en UTC
     * @return mismo instante expresado en zona {@code America/Bogota}
     */
    public static ZonedDateTime toColombiaTime(Instant instant) {
        return instant.atZone(COLOMBIA);
    }

    /**
     * Convierte un {@link LocalDateTime} de Colombia a {@link Instant} UTC.
     *
     * @param colombiaTime fecha/hora en zona {@code America/Bogota}
     * @return instante equivalente en UTC
     */
    public static Instant toUtcInstant(LocalDateTime colombiaTime) {
        return colombiaTime.atZone(COLOMBIA).toInstant();
    }

    // ─── Formateo ─────────────────────────────────────────────────────────────

    /**
     * Formatea un {@link Instant} como string ISO-8601 en UTC.
     * Ejemplo: {@code "2026-06-16T15:30:00"}
     *
     * @param instant instante a formatear
     * @return string ISO-8601 en UTC
     */
    public static String formatUtc(Instant instant) {
        return ISO_FORMATTER.format(instant);
    }

    /**
     * Extrae el año de cuatro dígitos de un {@link Instant} en la zona Colombia.
     * Se usa en {@link CodeGenerator} para componer el código de solicitud.
     *
     * @param instant instante de referencia
     * @return año (p. ej. {@code 2026})
     */
    public static int yearOf(Instant instant) {
        return instant.atZone(COLOMBIA).getYear();
    }
}
