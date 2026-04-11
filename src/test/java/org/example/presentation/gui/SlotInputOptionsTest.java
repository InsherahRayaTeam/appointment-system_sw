package org.example.presentation.gui;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SlotInputOptionsTest {

    @Test
    void dayValues_ReturnsSundayToSaturdayOrdering() {
        assertArrayEquals(
                new DayOfWeek[] {
                        DayOfWeek.SUNDAY,
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY
                },
                SlotInputOptions.dayValues()
        );
    }

    @Test
    void timeValues_ReturnsFixedAppointmentTimes() {
        assertArrayEquals(
                new String[] {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"},
                SlotInputOptions.timeValues()
        );
    }

    @Test
    void deriveDay_FromDateText_ReturnsMatchingDay() {
        LocalDate date = LocalDate.of(2030, 12, 12);

        assertEquals(date.getDayOfWeek(), SlotInputOptions.deriveDay(date));
        assertEquals(date.getDayOfWeek(), SlotInputOptions.deriveDay(date.toString()));
    }

    @Test
    void parseDate_InvalidOrBlankInput_ReturnsNull() {
        assertNull(SlotInputOptions.parseDate("   "));
        assertNull(SlotInputOptions.parseDate("2026/12/12"));
    }
}


