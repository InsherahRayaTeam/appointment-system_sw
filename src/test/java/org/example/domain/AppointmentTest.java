package org.example.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentTest {

    @Test
    void constructor_WithIdStartTimeDurationAndParticipants_ShouldStoreValues() {
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 22, 10, 0);

        Appointment appointment = new Appointment("apt1", startTime, 60, 3);

        assertEquals("apt1", appointment.getId());
        assertEquals(startTime, appointment.getStartTime());
        assertEquals(60, appointment.getDuration());
        assertEquals(60, appointment.getDurationMinutes());
        assertEquals(3, appointment.getParticipants());
        assertEquals(3, appointment.getParticipantCount());
        assertEquals(AppointmentType.NORMAL, appointment.getType());
    }

    @Test
    void constructor_ShouldAllowNullStartTime() {
        Appointment appointment = new Appointment("apt-null", null, 45, 2);

        assertEquals("apt-null", appointment.getId());
        assertNull(appointment.getStartTime());
        assertEquals(45, appointment.getDurationMinutes());
        assertEquals(2, appointment.getParticipants());
        assertNull(appointment.getSlotTime());
        assertNull(appointment.getSlotDate());
        assertNull(appointment.getSlotDay());
        assertNull(appointment.getSlotDateTimeLabel());
    }

    @Test
    void constructor_WithCustomerNameAndStatus_ShouldStoreCustomerAndStatus() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 0);

        Appointment appointment = new Appointment(
                "apt-customer",
                "Alice",
                startTime,
                60,
                2,
                AppointmentStatus.CONFIRMED
        );

        assertEquals("apt-customer", appointment.getId());
        assertEquals("Alice", appointment.getCustomerName());
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertEquals(startTime, appointment.getStartTime());
    }

    @Test
    void constructor_WithCustomerEmail_ShouldNormalizeEmail() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 0);

        Appointment appointment = new Appointment(
                "apt-email",
                "Alice",
                "ALICE@EXAMPLE.COM",
                startTime,
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        SystemUser user = appointment.getUser();

        assertNotNull(user);
        assertEquals("alice@example.com", user.getEmail());
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    void constructor_WithDetails_ShouldStorePhoneAndNormalizeType() {
        AppointmentDetails details = new AppointmentDetails(
                "Alice",
                "alice@example.com",
                " 0599999999 ",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                60,
                1
        );

        Appointment appointment = new Appointment(
                "apt-details",
                details,
                AppointmentStatus.CONFIRMED,
                null
        );

        assertEquals("apt-details", appointment.getId());
        assertEquals("Alice", appointment.getCustomerName());
        assertEquals("0599999999", appointment.getCustomerPhoneNumber());
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertEquals(AppointmentType.NORMAL, appointment.getType());
    }

    @Test
    void constructor_WithStringStatus_ShouldParseStatusIgnoringCase() {
        Appointment appointment = new Appointment("Alice", "10:00", 60, 1, "confirmed");

        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
    }

    @Test
    void constructor_WithInvalidStringStatus_ShouldStoreNullStatus() {
        Appointment appointment = new Appointment("Alice", "10:00", 60, 1, "not_a_status");

        assertNull(appointment.getStatus());
    }

    @Test
    void constructor_WithBlankStringStatus_ShouldStoreNullStatus() {
        Appointment appointment = new Appointment("Alice", "10:00", 60, 1, "   ");

        assertNull(appointment.getStatus());
    }

    @Test
    void constructor_WithStringSlotTime_ShouldCreateAppointmentWithParsedTime() {
        Appointment appointment = new Appointment(
                "Alice",
                "10:00",
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        assertNotNull(appointment.getId());
        assertEquals("Alice", appointment.getCustomerName());
        assertEquals("10:00", appointment.getSlotTime());
        assertEquals(60, appointment.getDurationMinutes());
        assertEquals(1, appointment.getParticipants());
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
    }

    @Test
    void constructor_WithInvalidSlotTime_ShouldStoreNullStartTime() {
        Appointment appointment = new Appointment(
                "Alice",
                "invalid-time",
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        assertNull(appointment.getStartTime());
        assertNull(appointment.getSlotTime());
        assertNull(appointment.getSlotDate());
        assertNull(appointment.getSlotDay());
        assertNull(appointment.getSlotDateTimeLabel());
    }

    @Test
    void constructor_WithDateTimeTextSlot_ShouldParseDateTime() {
        Appointment appointment = new Appointment(
                "Alice",
                "2026-04-20 15:30",
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        assertEquals(LocalDateTime.of(2026, 4, 20, 15, 30), appointment.getStartTime());
        assertEquals("15:30", appointment.getSlotTime());
        assertEquals("2026-04-20", appointment.getSlotDate());
        assertEquals("MONDAY", appointment.getSlotDay());
        assertEquals("2026-04-20 15:30", appointment.getSlotDateTimeLabel());
    }

    @Test
    void constructor_WithIsoDateTimeTextSlot_ShouldParseDateTime() {
        Appointment appointment = new Appointment(
                "Alice",
                "2026-04-20T15:30",
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        assertEquals(LocalDateTime.of(2026, 4, 20, 15, 30), appointment.getStartTime());
        assertEquals("15:30", appointment.getSlotTime());
        assertEquals("2026-04-20", appointment.getSlotDate());
        assertEquals("MONDAY", appointment.getSlotDay());
    }

    @Test
    void isFutureComparedTo_ShouldReturnTrueForFutureStartTime() {
        LocalDateTime referenceTime = LocalDateTime.of(2026, 4, 20, 10, 0);
        Appointment appointment = new Appointment(
                "apt-future",
                referenceTime.plusHours(2),
                60,
                1
        );

        boolean result = appointment.isFutureComparedTo(referenceTime);

        assertTrue(result);
    }

    @Test
    void isFutureComparedTo_ShouldReturnFalseForPastStartTime() {
        LocalDateTime referenceTime = LocalDateTime.of(2026, 4, 20, 10, 0);
        Appointment appointment = new Appointment(
                "apt-past",
                referenceTime.minusMinutes(1),
                60,
                1
        );

        boolean result = appointment.isFutureComparedTo(referenceTime);

        assertFalse(result);
    }

    @Test
    void isFutureComparedTo_ShouldReturnFalseWhenReferenceTimeIsNull() {
        Appointment appointment = new Appointment(
                "apt-null-reference",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                60,
                1
        );

        boolean result = appointment.isFutureComparedTo(null);

        assertFalse(result);
    }

    @Test
    void isFutureComparedTo_ShouldReturnFalseWhenStartTimeIsNull() {
        Appointment appointment = new Appointment("apt-null-start", null, 60, 1);

        boolean result = appointment.isFutureComparedTo(LocalDateTime.of(2026, 4, 20, 10, 0));

        assertFalse(result);
    }

    @Test
    void withStatus_ShouldReturnCopyWithUpdatedStatusAndSameCoreData() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 0);
        Appointment appointment = new Appointment(
                "apt-status",
                "Alice",
                startTime,
                60,
                2,
                AppointmentStatus.CONFIRMED
        );

        Appointment updated = appointment.withStatus(AppointmentStatus.CANCELLED);

        assertEquals(AppointmentStatus.CANCELLED, updated.getStatus());
        assertEquals("apt-status", updated.getId());
        assertEquals("Alice", updated.getCustomerName());
        assertEquals(startTime, updated.getStartTime());
        assertEquals(60, updated.getDurationMinutes());
        assertEquals(2, updated.getParticipants());
    }

    @Test
    void withCustomerPhoneNumber_ShouldReturnCopyWithUpdatedPhoneNumber() {
        AppointmentDetails details = new AppointmentDetails(
                "Alice",
                "alice@example.com",
                "0591111111",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                60,
                1
        );

        Appointment appointment = new Appointment(
                "apt-phone",
                details,
                AppointmentStatus.CONFIRMED,
                AppointmentType.NORMAL
        );

        Appointment updated = appointment.withCustomerPhoneNumber(" 0592222222 ");

        assertEquals("apt-phone", updated.getId());
        assertEquals("0592222222", updated.getCustomerPhoneNumber());
        assertEquals(AppointmentStatus.CONFIRMED, updated.getStatus());
        assertEquals(AppointmentType.NORMAL, updated.getType());
    }

    @Test
    void withSlotTimeAndStatus_ShouldReturnCopyWithUpdatedTimeAndStatus() {
        Appointment original = new Appointment(
                "apt-modify",
                "Alice",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );

        Appointment modified = original.withSlotTimeAndStatus("11:00", AppointmentStatus.MODIFIED);

        assertEquals(AppointmentStatus.MODIFIED, modified.getStatus());
        assertEquals("11:00", modified.getSlotTime());
        assertEquals(original.getId(), modified.getId());
        assertEquals(original.getCustomerName(), modified.getCustomerName());
        assertEquals(original.getDurationMinutes(), modified.getDurationMinutes());
        assertEquals(original.getParticipants(), modified.getParticipants());
    }

    @Test
    void withStartTimeAndStatus_ShouldReturnCopyWithUpdatedDateTimeAndStatus() {
        Appointment original = new Appointment(
                "apt-start-update",
                "Alice",
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
        assertEquals(original.getCustomerName(), updated.getCustomerName());
    }

    @Test
    void shouldExposeSlotDateDayAndDateTimeLabel() {
        LocalDateTime start = LocalDate.of(2026, 4, 20).atTime(LocalTime.of(15, 30));

        Appointment appointment = new Appointment(
                "apt-date",
                "Alice",
                "alice@example.com",
                start,
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        assertEquals("2026-04-20", appointment.getSlotDate());
        assertEquals("MONDAY", appointment.getSlotDay());
        assertEquals("2026-04-20 15:30", appointment.getSlotDateTimeLabel());
    }

    @Test
    void setType_WithNull_ShouldDefaultToNormal() {
        Appointment appointment = new Appointment(
                "apt-type",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                60,
                1
        );

        appointment.setType(null);

        assertEquals(AppointmentType.NORMAL, appointment.getType());
    }

    @Test
    void setType_WithValue_ShouldUpdateType() {
        Appointment appointment = new Appointment(
                "apt-type",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                60,
                1
        );

        appointment.setType(AppointmentType.URGENT);

        assertEquals(AppointmentType.URGENT, appointment.getType());
    }

    @Test
    void feedbackFields_ShouldDefaultToEmptyState() {
        Appointment appointment = new Appointment(
                "apt-empty",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                30,
                1
        );

        assertEquals(0, appointment.getRating());
        assertNull(appointment.getFeedbackComment());
        assertFalse(appointment.isFeedbackSubmitted());
    }

    @Test
    void feedbackFields_ShouldBeStoredAndCopiedWithAppointment() {
        Appointment appointment = new Appointment(
                "apt-feedback",
                "Alice",
                LocalDateTime.of(2026, 4, 20, 10, 0),
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
    void setFeedbackComment_WithBlankValue_ShouldStoreNull() {
        Appointment appointment = new Appointment(
                "apt-feedback-blank",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                30,
                1
        );

        appointment.setFeedbackComment("   ");

        assertNull(appointment.getFeedbackComment());
    }

    @Test
    void setFeedbackComment_WithNullValue_ShouldStoreNull() {
        Appointment appointment = new Appointment(
                "apt-feedback-null",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                30,
                1
        );

        appointment.setFeedbackComment(null);

        assertNull(appointment.getFeedbackComment());
    }

    @Test
    void setFeedbackComment_ShouldTrimValue() {
        Appointment appointment = new Appointment(
                "apt-feedback-trim",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                30,
                1
        );

        appointment.setFeedbackComment("  Good  ");

        assertEquals("Good", appointment.getFeedbackComment());
    }

    @Test
    void getUser_ShouldReturnNullWhenCustomerIdentityIsMissing() {
        AppointmentDetails details = new AppointmentDetails(
                null,
                null,
                null,
                LocalDateTime.of(2026, 4, 20, 10, 0),
                60,
                1
        );

        Appointment appointment = new Appointment(
                "apt-no-user",
                details,
                AppointmentStatus.CONFIRMED,
                AppointmentType.NORMAL
        );

        assertNull(appointment.getUser());
    }

    @Test
    void getUser_ShouldUseCustomerNameWhenEmailIsMissing() {
        Appointment appointment = new Appointment(
                "apt-user-name",
                "Alice",
                LocalDateTime.of(2026, 4, 20, 10, 0),
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        SystemUser user = appointment.getUser();

        assertNotNull(user);
        assertEquals("alice", user.getEmail());
        assertEquals(UserRole.USER, user.getRole());
    }
}