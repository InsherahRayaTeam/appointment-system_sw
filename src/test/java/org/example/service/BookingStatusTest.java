package org.example.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingStatusTest {

    private static final int EXPECTED_STATUS_COUNT = 28;

    private void assertStatus(String name, BookingStatus expected) {
        assertEquals(expected, BookingStatus.valueOf(name));
    }

    @Test
    void enum_ContainsExpectedStatuses() {
        assertStatus("SUCCESS", BookingStatus.SUCCESS);
        assertStatus("BLANK_CUSTOMER_NAME", BookingStatus.BLANK_CUSTOMER_NAME);
        assertStatus("BLANK_PHONE_NUMBER", BookingStatus.BLANK_PHONE_NUMBER);
        assertStatus("BLANK_SLOT_TIME", BookingStatus.BLANK_SLOT_TIME);
        assertStatus("INVALID_DURATION", BookingStatus.INVALID_DURATION);
        assertStatus("INVALID_PARTICIPANT_COUNT", BookingStatus.INVALID_PARTICIPANT_COUNT);
        assertStatus("INVALID_PHONE_NUMBER", BookingStatus.INVALID_PHONE_NUMBER);
        assertStatus("INVALID_SLOT_DATE_TIME", BookingStatus.INVALID_SLOT_DATE_TIME);
        assertStatus("INVALID_APPOINTMENT_RULES", BookingStatus.INVALID_APPOINTMENT_RULES);
        assertStatus("DUPLICATE_SLOT", BookingStatus.DUPLICATE_SLOT);
        assertStatus("SLOT_NOT_FOUND", BookingStatus.SLOT_NOT_FOUND);
        assertStatus("SLOT_ALREADY_BOOKED", BookingStatus.SLOT_ALREADY_BOOKED);
        assertStatus("WAITLISTED", BookingStatus.WAITLISTED);
        assertStatus("WAITLIST_ALREADY_EXISTS", BookingStatus.WAITLIST_ALREADY_EXISTS);
        assertStatus("WAITLIST_ALREADY_BOOKED", BookingStatus.WAITLIST_ALREADY_BOOKED);
        assertStatus("WAITLIST_PROMOTION_FAILED", BookingStatus.WAITLIST_PROMOTION_FAILED);
        assertStatus("UNAUTHORIZED", BookingStatus.UNAUTHORIZED);
        assertStatus("APPOINTMENT_NOT_FOUND", BookingStatus.APPOINTMENT_NOT_FOUND);
        assertStatus("APPOINTMENT_NOT_FUTURE", BookingStatus.APPOINTMENT_NOT_FUTURE);
        assertStatus("APPOINTMENT_ALREADY_CANCELLED", BookingStatus.APPOINTMENT_ALREADY_CANCELLED);
        assertStatus("APPOINTMENT_ALREADY_ATTENDED", BookingStatus.APPOINTMENT_ALREADY_ATTENDED);
        assertStatus("APPOINTMENT_ALREADY_NOT_ATTENDED", BookingStatus.APPOINTMENT_ALREADY_NOT_ATTENDED);
        assertStatus("APPOINTMENT_ALREADY_COMPLETED", BookingStatus.APPOINTMENT_ALREADY_COMPLETED);
        assertStatus("APPOINTMENT_NOT_ATTENDED", BookingStatus.APPOINTMENT_NOT_ATTENDED);
        assertStatus("UPDATE_FAILED", BookingStatus.UPDATE_FAILED);
    }

    @Test
    void enum_HasExpectedTotalNumberOfValues() {
        assertEquals(EXPECTED_STATUS_COUNT, BookingStatus.values().length);
    }
}
