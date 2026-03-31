package org.example.service;

import org.example.domain.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticipantRuleTest {

    private ParticipantRule participantRule;
    private LocalDateTime appointmentTime;

    @BeforeEach
    void setUp() {
        participantRule = new ParticipantRule();
        appointmentTime = LocalDateTime.of(2026, 3, 22, 10, 0);
    }

    @Test
    void shouldAllowValidParticipants() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 60, 3);

        boolean result = participantRule.isValid(appointment);

        assertTrue(result);
    }

    @Test
    void shouldAllowMinimalParticipants() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 60, 1);

        boolean result = participantRule.isValid(appointment);

        assertTrue(result);
    }

    @Test
    void shouldAllowParticipantsAtLimit() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 60, 5);

        boolean result = participantRule.isValid(appointment);

        assertTrue(result);
    }

    @Test
    void shouldRejectTooManyParticipants() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 60, 6);

        boolean result = participantRule.isValid(appointment);

        assertFalse(result);
    }

    @Test
    void shouldRejectExcessiveParticipants() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 60, 20);

        boolean result = participantRule.isValid(appointment);

        assertFalse(result);
    }

    @Test
    void shouldRejectZeroParticipants() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 60, 0);

        boolean result = participantRule.isValid(appointment);

        assertFalse(result);
    }

    @Test
    void shouldRejectNegativeParticipants() {
        Appointment appointment = new Appointment("apt1", appointmentTime, 60, -5);

        boolean result = participantRule.isValid(appointment);

        assertFalse(result);
    }

    @Test
    void shouldThrowWhenAppointmentIsNull() {
        assertThrows(NullPointerException.class, () -> participantRule.isValid(null));
    }
}

