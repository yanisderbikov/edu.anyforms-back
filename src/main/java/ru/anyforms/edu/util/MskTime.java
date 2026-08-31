package ru.anyforms.edu.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Время открытия модулей задаётся и показывается по Москве, а хранится и
 * сравнивается как Instant — так логика не зависит от таймзоны JVM и БД.
 * Единственное место перевода «настенное время МСК» ↔ Instant.
 */
public final class MskTime {

    public static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    /** Формат обмена с фронтом: 2026-09-01T14:00 (минуты, без секунд и зоны) */
    private static final DateTimeFormatter WIRE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private MskTime() {
    }

    /** Московское настенное время → момент; null → null */
    public static Instant toInstant(LocalDateTime mskWallTime) {
        return mskWallTime == null ? null : mskWallTime.atZone(ZONE).toInstant();
    }

    /** Момент → строка московского времени вида «2026-09-01T14:00»; null → null */
    public static String format(Instant instant) {
        return instant == null ? null : WIRE_FORMAT.format(LocalDateTime.ofInstant(instant, ZONE));
    }
}
