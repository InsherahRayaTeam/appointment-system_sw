package org.example.service;

import org.example.domain.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DurationRuleTest {

    private DurationRule durationRule;
    private LocalDateTime appointmentTime;

    @BeforeEach
    void setUp() {
        durationRule = new DurationRule();
        appointmentTime = LocalDateTime.of(2026, 3, 22, 10, 0);
    }

    @Test
    void shouldAllowValidDuration() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 60, 3);

        boolean result = durationRule.isValid(appointment);

        assertTrue(result);
    }

    @Test
    void shouldAllowMinimalDuration() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 1, 3);

        boolean result = durationRule.isValid(appointment);

        assertTrue(result);
    }

    @Test
    void shouldAllowDurationAtLimit() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 120, 3);

        boolean result = durationRule.isValid(appointment);

        assertTrue(result);
    }

    @Test
    void shouldRejectLongDuration() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 121, 3);

        boolean result = durationRule.isValid(appointment);

        assertFalse(result);
    }

    @Test
    void shouldRejectExcessivelyLongDuration() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 300, 3);

        boolean result = durationRule.isValid(appointment);

        assertFalse(result);
    }

    @Test
    void shouldAllowZeroDurationBecauseRuleChecksOnlyMaxLimit() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 0, 3);

        boolean result = durationRule.isValid(appointment);

        assertTrue(result);
    }

    @Test
    void shouldAllowNegativeDurationBecauseRuleChecksOnlyMaxLimit() {
        Appointment appointment = new Appointment("apt1", appointmentTime, -60, 3);

        boolean result = durationRule.isValid(appointment);

        assertTrue(result);
    }

    @Test
    void shouldThrowWhenAppointmentIsNull() {
        assertThrows(NullPointerException.class, () -> durationRule.isValid(null));
    }
}

