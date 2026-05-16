package com.cooperative.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateFormatUtil {
    private static final DateTimeFormatter FRENCH_DATE =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter FRENCH_DATETIME =
            DateTimeFormatter.ofPattern("d MMMM yyyy 'à' HH:mm", Locale.FRENCH);

    private DateFormatUtil() {
    }

    public static String formatFrenchDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return capitalize(FRENCH_DATE.format(date));
    }

    public static String formatFrenchDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return capitalize(FRENCH_DATETIME.format(dateTime));
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
