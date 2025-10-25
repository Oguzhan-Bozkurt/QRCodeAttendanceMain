package com.example.qrkodlayoklama.util;

import java.time.*;

public final class DateFormat {
    private DateFormat() {}

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Istanbul");
    public static final java.time.format.DateTimeFormatter OUT_FMT =
            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    .withLocale(new java.util.Locale("tr","TR"));

    public static String any(String input) {
        return any(input, DEFAULT_ZONE, OUT_FMT);
    }

    public static String any(String input, ZoneId zone, java.time.format.DateTimeFormatter fmt) {
        if (input == null || input.equals("-")) return "-";
        try {
            OffsetDateTime odt = OffsetDateTime.parse(input);
            return fmt.format(odt.atZoneSameInstant(zone));
        } catch (Exception ignored) {}
        try {
            Instant inst = Instant.parse(input);
            return fmt.format(inst.atZone(zone));
        } catch (Exception ignored) {}
        try {
            long ms = Long.parseLong(input);
            return fmt.format(Instant.ofEpochMilli(ms).atZone(zone));
        } catch (Exception ignored) {}
        return "-";
    }

    public static String of(Instant inst) {
        if (inst == null) return "-";
        return OUT_FMT.format(inst.atZone(DEFAULT_ZONE));
    }
    public static String of(OffsetDateTime odt) {
        if (odt == null) return "-";
        return OUT_FMT.format(odt.atZoneSameInstant(DEFAULT_ZONE));
    }
    public static String ofEpochMillis(Long ms) {
        if (ms == null) return "-";
        return OUT_FMT.format(Instant.ofEpochMilli(ms).atZone(DEFAULT_ZONE));
    }
}
