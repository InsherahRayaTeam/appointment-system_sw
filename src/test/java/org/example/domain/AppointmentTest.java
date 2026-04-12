package org.example.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void feedbackFields_ShouldBeStoredAndCopiedWithAppointment() {
        Appointment appointment = new Appointment(
                "apt-feedback",
                "alice@example.com",
                LocalDateTime.now().plusHours(1),
                60,
                2,
                AppointmentStatus.COMPLETED
        );

        appointment.setRating(5);
        appointment.setFeedbackComment("Great service");
        appointment.setFeedbackSubmitted(true);

        Appointment copied = appointment.withStatus(AppointmentStatus.COMPLETED);

        assertEquals(5, appointment.getRating());
        assertEquals("Great service", appointment.getFeedbackComment());
        assertTrue(appointment.isFeedbackSubmitted());
        assertEquals(5, copied.getRating());
        assertEquals("Great service", copied.getFeedbackComment());
        assertTrue(copied.isFeedbackSubmitted());
    }

    @Test
    void feedbackFields_DefaultToEmptyState() {
        Appointment appointment = new Appointment("apt-empty", LocalDateTime.now().plusHours(1), 30, 1);

        assertEquals(0, appointment.getRating());
        assertNull(appointment.getFeedbackComment());
        assertFalse(appointment.isFeedbackSubmitted());
    }

    @Test
    void constructor_WithStringStatus_ParsesStatusIgnoringCase() {
        Appointment appointment = new Appointment("Alice", "10:00", 60, 1, "confirmed");

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
    }

    @Test
    void constructor_WithInvalidStringStatus_StoresNullStatus() {
        Appointment appointment = new Appointment("Alice", "10:00", 60, 1, "not_a_status");

        assertNull(appointment.getStatus());
    }

    @Test
    void withSlotTimeAndStatus_ShouldReturnCopyWithUpdatedTimeAndStatus() {
        Appointment original = new Appointment(
                "apt-modify",
                "Alice",
                LocalDateTime.now().plusHours(2),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );

        Appointment modified = original.withSlotTimeAndStatus("11:00", AppointmentStatus.MODIFIED);

        assertEquals(AppointmentStatus.MODIFIED, modified.getStatus());
        assertEquals("11:00", modified.getSlotTime());
        assertEquals(original.getId(), modified.getId());
    }

    @Test
    void shouldExposeSlotDateDayAndDateTimeLabel() {
        LocalDateTime start = LocalDate.of(2026, 4, 20).atTime(LocalTime.of(15, 30));
        Appointment appointment = new Appointment("apt-date", "alice@example.com", start, 60, 1, AppointmentStatus.CONFIRMED);

        assertEquals("2026-04-20", appointment.getSlotDate());
        assertEquals("MONDAY", appointment.getSlotDay());
        assertEquals("2026-04-20 15:30", appointment.getSlotDateTimeLabel());
    }

    @Test
    void withStartTimeAndStatus_ShouldReturnCopyWithUpdatedDateTimeAndStatus() {
        Appointment original = new Appointment(
                "apt-start-update",
                "alice@example.com",
                LocalDate.of(2026, 4, 20).atTime(10, 0),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );

        Appointment updated = original.withStartTimeAndStatus(
                LocalDate.of(2026, 4, 22).atTime(11, 15),
                AppointmentStatus.MODIFIED
        );

        assertEquals(AppointmentStatus.MODIFIED, updated.getStatus());
        assertEquals("2026-04-22", updated.getSlotDate());
        assertEquals("11:15", updated.getSlotTime());
        assertEquals(original.getId(), updated.getId());
    }
}

