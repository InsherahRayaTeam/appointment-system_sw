package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrgentRuleTest {

    private final UrgentRule urgentRule = new UrgentRule();

    @Test
    void shouldAllowUrgentAppointmentWithinLimit() {
        Appointment appointment = new Appointment("apt-1", LocalDateTime.of(2026, 3, 22, 10, 0), 30, 1);

        assertTrue(urgentRule.isValid(appointment));
    }

    @Test
    void shouldRejectUrgentAppointmentOverLimit() {
        Appointment appointment = new Appointment("apt-1", LocalDateTime.of(2026, 3, 22, 10, 0), 31, 1);

        assertFalse(urgentRule.isValid(appointment));
    }

    @Test
    void shouldExposeSupportedType() {
        assertEquals(AppointmentType.URGENT, urgentRule.getSupportedType());
    }

    @Test
    void shouldThrowWhenAppointmentIsNull() {
        assertThrows(NullPointerException.class, () -> urgentRule.isValid(null));
    }
}

