package org.example.presentation.gui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Represents slot input options in the system.
 */
final class SlotInputOptions {

    private static final DayOfWeek[] DAY_VALUES = {
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    };

    private static final String[] TIME_VALUES = {
            "09:00",
            "10:00",
            "11:00",
            "12:00",
            "13:00",
            "14:00",
            "15:00"
    };

    private SlotInputOptions() {
        // Utility class
    }

    static DayOfWeek[] dayValues() {
        return DAY_VALUES.clone();
    }

    static String[] timeValues() {
        return TIME_VALUES.clone();
    }

    static DayOfWeek deriveDay(LocalDate date) {
        return date == null ? null : date.getDayOfWeek();
    }


    static DayOfWeek deriveDay(String dateText) {
        LocalDate parsedDate = parseDate(dateText);
        return parsedDate == null ? null : parsedDate.getDayOfWeek();
    }

    static LocalDate parseDate(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateText.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}

