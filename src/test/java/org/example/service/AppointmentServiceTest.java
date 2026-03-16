package org.example.service;

import org.example.domain.AppointmentSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest {

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService();
    }

    @Test
    void getAvailableSlots_initiallyContainsAllSlots() {
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        assertEquals(3, slots.size());
    }

    @Test
    void bookSlot_validSlot_succeeds() {
        boolean result = appointmentService.bookSlot("10:00");
        assertTrue(result);
    }

    @Test
    void bookSlot_sameSlotTwice_failsSecondTime() {
        appointmentService.bookSlot("10:00");
        boolean result = appointmentService.bookSlot("10:00");
        assertFalse(result);
    }

    @Test
    void bookSlot_nonExistingSlot_fails() {
        boolean result = appointmentService.bookSlot("15:00");
        assertFalse(result);
    }

    @Test
    void bookSlot_nullTime_fails() {
        boolean result = appointmentService.bookSlot(null);
        assertFalse(result);
    }

    @Test
    void bookSlot_blankTime_fails() {
        boolean result = appointmentService.bookSlot("   ");
        assertFalse(result);
    }

    @Test
    void getAvailableSlots_afterBooking_decreases() {
        appointmentService.bookSlot("10:00");
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        assertEquals(2, slots.size());
    }
}