package org.example.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppointmentSlotTest {

    @Test
    void constructor_CorrectlyInitializesTimeAndBookedStatus() {
        AppointmentSlot slot = new AppointmentSlot("10:00");

        assertEquals("10:00", slot.getTime());
        assertFalse(slot.isBooked());
    }

    @Test
    void getTime_ReturnsCorrectTime() {
        AppointmentSlot slot = new AppointmentSlot("14:30");

        assertEquals("14:30", slot.getTime());
    }

    @Test
    void isBooked_InitiallyReturnsFalse() {
        AppointmentSlot slot = new AppointmentSlot("10:00");

        assertFalse(slot.isBooked());
    }

    @Test
    void book_ChangesBookedStatusToTrue() {
        AppointmentSlot slot = new AppointmentSlot("10:00");

        slot.book();

        assertTrue(slot.isBooked());
    }

    @Test
    void isBooked_ReturnsTrueAfterBooking() {
        AppointmentSlot slot = new AppointmentSlot("11:00");
        assertFalse(slot.isBooked());

        slot.book();

        assertTrue(slot.isBooked());
    }
}

