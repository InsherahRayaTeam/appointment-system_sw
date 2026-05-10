package org.example.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AppointmentDetailsTest {

    @Test
    void constructor_ShouldStoreAllProvidedValues() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 30);

        AppointmentDetails details = new AppointmentDetails(
                "Alice",
                "alice@example.com",
                "0599999999",
                startTime,
                60,
                2
        );

        assertEquals("Alice", details.getCustomerName());
        assertEquals("alice@example.com", details.getCustomerEmail());
        assertEquals("0599999999", details.getCustomerPhoneNumber());
        assertEquals(startTime, details.getStartTime());
        assertEquals(60, details.getDuration());
        assertEquals(2, details.getParticipants());
    }

    @Test
    void constructor_ShouldAllowNullCustomerName() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 30);

        AppointmentDetails details = new AppointmentDetails(
                null,
                "alice@example.com",
                "0599999999",
                startTime,
                60,
                2
        );

        assertNull(details.getCustomerName());
        assertEquals("alice@example.com", details.getCustomerEmail());
        assertEquals("0599999999", details.getCustomerPhoneNumber());
        assertEquals(startTime, details.getStartTime());
        assertEquals(60, details.getDuration());
        assertEquals(2, details.getParticipants());
    }

    @Test
    void constructor_ShouldAllowNullCustomerEmail() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 30);

        AppointmentDetails details = new AppointmentDetails(
                "Alice",
                null,
                "0599999999",
                startTime,
                60,
                2
        );

        assertEquals("Alice", details.getCustomerName());
        assertNull(details.getCustomerEmail());
        assertEquals("0599999999", details.getCustomerPhoneNumber());
        assertEquals(startTime, details.getStartTime());
    }

    @Test
    void constructor_ShouldAllowNullCustomerPhoneNumber() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 30);

        AppointmentDetails details = new AppointmentDetails(
                "Alice",
                "alice@example.com",
                null,
                startTime,
                60,
                2
        );

        assertEquals("Alice", details.getCustomerName());
        assertEquals("alice@example.com", details.getCustomerEmail());
        assertNull(details.getCustomerPhoneNumber());
        assertEquals(startTime, details.getStartTime());
    }

    @Test
    void constructor_ShouldAllowNullStartTime() {
        AppointmentDetails details = new AppointmentDetails(
                "Alice",
                "alice@example.com",
                "0599999999",
                null,
                60,
                2
        );

        assertEquals("Alice", details.getCustomerName());
        assertEquals("alice@example.com", details.getCustomerEmail());
        assertEquals("0599999999", details.getCustomerPhoneNumber());
        assertNull(details.getStartTime());
        assertEquals(60, details.getDuration());
        assertEquals(2, details.getParticipants());
    }

    @Test
    void constructor_ShouldStoreZeroDurationAndZeroParticipants() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 30);

        AppointmentDetails details = new AppointmentDetails(
                "Alice",
                "alice@example.com",
                "0599999999",
                startTime,
                0,
                0
        );

        assertEquals(0, details.getDuration());
        assertEquals(0, details.getParticipants());
    }

    @Test
    void constructor_ShouldStoreNegativeDurationAndParticipants_WhenProvided() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 30);

        AppointmentDetails details = new AppointmentDetails(
                "Alice",
                "alice@example.com",
                "0599999999",
                startTime,
                -10,
                -1
        );

        assertEquals(-10, details.getDuration());
        assertEquals(-1, details.getParticipants());
    }

    @Test
    void constructor_ShouldNotTrimOrNormalizeTextFields() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 10, 30);

        AppointmentDetails details = new AppointmentDetails(
                "  Alice  ",
                "  ALICE@EXAMPLE.COM  ",
                "  0599999999  ",
                startTime,
                60,
                2
        );

        assertEquals("  Alice  ", details.getCustomerName());
        assertEquals("  ALICE@EXAMPLE.COM  ", details.getCustomerEmail());
        assertEquals("  0599999999  ", details.getCustomerPhoneNumber());
    }
}