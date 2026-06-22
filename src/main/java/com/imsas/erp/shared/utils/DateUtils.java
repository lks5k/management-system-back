package com.imsas.erp.shared.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    
    public static final ZoneId UTC = ZoneId.of("UTC");

    
    public static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");

    
    public static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(UTC);

    
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(COLOMBIA);

    private DateUtils() {
        throw new UnsupportedOperationException("Clase de utilidad, no instanciar");
    }

    // ─── Obtener instante actual ──────────────────────────────────────────────

    
    public static Instant nowUtc() {
        return Instant.now();
    }

    
    public static LocalDateTime nowColombia() {
        return LocalDateTime.now(COLOMBIA);
    }

    
    public static LocalDate todayColombia() {
        return LocalDate.now(COLOMBIA);
    }

    // ─── Conversión entre zonas ───────────────────────────────────────────────

    
    public static ZonedDateTime toColombiaTime(Instant instant) {
        return instant.atZone(COLOMBIA);
    }

    
    public static Instant toUtcInstant(LocalDateTime colombiaTime) {
        return colombiaTime.atZone(COLOMBIA).toInstant();
    }

    // ─── Formateo ─────────────────────────────────────────────────────────────

    
    public static String formatUtc(Instant instant) {
        return ISO_FORMATTER.format(instant);
    }

    
    public static int yearOf(Instant instant) {
        return instant.atZone(COLOMBIA).getYear();
    }
}
