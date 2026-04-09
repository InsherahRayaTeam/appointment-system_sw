package org.example.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingStatusTest {

    @Test
    void enum_ContainsExpectedStatuses() {
        assertEquals(BookingStatus.SUCCESS, BookingStatus.valueOf("SUCCESS"));
        assertEquals(BookingStatus.BLANK_CUSTOMER_NAME, BookingStatus.valueOf("BLANK_CUSTOMER_NAME"));
        assertEquals(BookingStatus.BLANK_SLOT_TIME, BookingStatus.valueOf("BLANK_SLOT_TIME"));
        assertEquals(BookingStatus.INVALID_DURATION, BookingStatus.valueOf("INVALID_DURATION"));
        assertEquals(BookingStatus.INVALID_PARTICIPANT_COUNT, BookingStatus.valueOf("INVALID_PARTICIPANT_COUNT"));
        assertEquals(BookingStatus.INVALID_APPOINTMENT_RULES, BookingStatus.valueOf("INVALID_APPOINTMENT_RULES"));
        assertEquals(BookingStatus.SLOT_NOT_FOUND, BookingStatus.valueOf("SLOT_NOT_FOUND"));
        assertEquals(BookingStatus.SLOT_ALREADY_BOOKED, BookingStatus.valueOf("SLOT_ALREADY_BOOKED"));
        assertEquals(BookingStatus.UNAUTHORIZED, BookingStatus.valueOf("UNAUTHORIZED"));
        assertEquals(BookingStatus.APPOINTMENT_NOT_FOUND, BookingStatus.valueOf("APPOINTMENT_NOT_FOUND"));
        assertEquals(BookingStatus.APPOINTMENT_NOT_FUTURE, BookingStatus.valueOf("APPOINTMENT_NOT_FUTURE"));
        assertEquals(
                BookingStatus.APPOINTMENT_ALREADY_CANCELLED,
                BookingStatus.valueOf("APPOINTMENT_ALREADY_CANCELLED")
        );
        assertEquals(
                BookingStatus.APPOINTMENT_ALREADY_ATTENDED,
                BookingStatus.valueOf("APPOINTMENT_ALREADY_ATTENDED")
        );
        assertEquals(
                BookingStatus.APPOINTMENT_ALREADY_COMPLETED,
                BookingStatus.valueOf("APPOINTMENT_ALREADY_COMPLETED")
        );
        assertEquals(BookingStatus.APPOINTMENT_NOT_ATTENDED, BookingStatus.valueOf("APPOINTMENT_NOT_ATTENDED"));
        assertEquals(BookingStatus.UPDATE_FAILED, BookingStatus.valueOf("UPDATE_FAILED"));
    }

    @Test
    void enum_HasExpectedTotalNumberOfValues() {
        assertEquals(16, BookingStatus.values().length);
    }
}

