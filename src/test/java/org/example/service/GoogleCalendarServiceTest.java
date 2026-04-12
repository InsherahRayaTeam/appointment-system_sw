package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleCalendarServiceTest {

    @Test
    void formatDateTime_formatsUsingGoogleCalendarPattern() {
        GoogleCalendarService service = new GoogleCalendarService();

        String formatted = service.formatDateTime(LocalDateTime.of(2032, 5, 10, 14, 30, 0));

        assertEquals("20320510T143000", formatted);
    }

    @Test
    void buildGoogleCalendarEventUrl_includesPrefilledFields() {
        GoogleCalendarService service = new GoogleCalendarService();
        Appointment appointment = new Appointment(
                "res-1",
                "Alice",
                "alice@example.com",
                "+970500000000",
                LocalDateTime.of(2032, 5, 10, 14, 30),
                45,
                3,
                AppointmentStatus.CONFIRMED,
                AppointmentType.GROUP
        );

        String url = service.buildGoogleCalendarEventUrl(appointment);

        assertTrue(url.startsWith("https://calendar.google.com/calendar/render?action=TEMPLATE"));
        assertTrue(url.contains("text=Appointment+with+Alice"));
        assertTrue(url.contains("dates=20320510T143000/20320510T151500"));
        assertTrue(url.contains("Phone%3A+%2B970500000000"));
        assertTrue(url.contains("Type%3A+Group"));
        assertTrue(url.contains("Participants%3A+3"));
    }
}

