package org.example.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppointmentTest {

    @Test
    void shouldStoreStartTimeDurationAndParticipantsFromConstructor() {
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 22, 10, 0);
        Appointment appointment = new Appointment("apt1", startTime, 60, 3);

        assertEquals(startTime, appointment.getStartTime());
        assertEquals(60, appointment.getDurationMinutes());
        assertEquals(3, appointment.getParticipants());
    }

    @Test
    void shouldReturnStartTimeFromGetter() {
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 22, 14, 30);
        Appointment appointment = new Appointment("apt2", startTime, 90, 5);

        assertEquals(startTime, appointment.getStartTime());
    }

    @Test
    void shouldReturnDurationMinutesFromGetter() {
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 22, 10, 0);
        Appointment appointment = new Appointment("apt1", startTime, 120, 4);

        assertEquals(120, appointment.getDurationMinutes());
    }

    @Test
    void shouldReturnParticipantsFromGetter() {
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 22, 10, 0);
        Appointment appointment = new Appointment("apt1", startTime, 60, 5);

        assertEquals(5, appointment.getParticipants());
    }

    @Test
    void shouldAllowNullStartTimeInConstructor() {
        Appointment appointment = new Appointment("apt-null", null, 45, 2);

        assertNull(appointment.getStartTime());
        assertEquals(45, appointment.getDurationMinutes());
        assertEquals(2, appointment.getParticipants());
    }

    @Test
    void isFutureComparedTo_ShouldReturnTrueForFutureStartTime() {
        Appointment appointment = new Appointment(
                "apt-future",
                LocalDateTime.now().plusHours(2),
                60,
                1
        );

        boolean result = appointment.isFutureComparedTo(LocalDateTime.now());

        assertTrue(result);
    }

    @Test
    void withStatus_ShouldReturnCopyWithUpdatedStatus() {
        Appointment appointment = new Appointment(
                "apt-status",
                "Alice",
                LocalDateTime.now().plusHours(1),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );

        Appointment updated = appointment.withStatus(AppointmentStatus.CANCELLED);

        assertEquals(AppointmentStatus.CANCELLED, updated.getStatus());
        assertEquals("apt-status", updated.getId());
    }
}

