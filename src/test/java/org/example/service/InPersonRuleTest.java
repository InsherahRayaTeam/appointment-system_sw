package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InPersonRuleTest {

    private final InPersonRule inPersonRule = new InPersonRule();

    @Test
    void isValid_ParticipantCountAtMinimumBoundary_ReturnsTrue() {
        Appointment appointment = new Appointment(
                "apt-1",
                LocalDateTime.of(2026, 4, 8, 10, 0),
                60,
                1
        );

        assertTrue(inPersonRule.isValid(appointment));
    }

    @Test
    void isValid_ParticipantCountBelowMinimum_ReturnsFalse() {
        Appointment appointment = new Appointment(
                "apt-2",
                LocalDateTime.of(2026, 4, 8, 10, 0),
                60,
                0
        );

        assertFalse(inPersonRule.isValid(appointment));
    }

    @Test
    void isValid_NullAppointment_ReturnsFalse() {
        assertFalse(inPersonRule.isValid(null));
    }

    @Test
    void getSupportedType_ReturnsInPerson() {
        assertEquals(AppointmentType.IN_PERSON, inPersonRule.getSupportedType());
    }
}
