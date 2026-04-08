package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndividualRuleTest {

    private final IndividualRule individualRule = new IndividualRule();

    @Test
    void isValid_ParticipantCountEqualsRequired_ReturnsTrue() {
        Appointment appointment = new Appointment(
                "apt-1",
                LocalDateTime.of(2026, 4, 8, 10, 0),
                60,
                1
        );

        assertTrue(individualRule.isValid(appointment));
    }

    @Test
    void isValid_ParticipantCountBelowRequired_ReturnsFalse() {
        Appointment appointment = new Appointment(
                "apt-2",
                LocalDateTime.of(2026, 4, 8, 10, 0),
                60,
                0
        );

        assertFalse(individualRule.isValid(appointment));
    }

    @Test
    void isValid_ParticipantCountAboveRequired_ReturnsFalse() {
        Appointment appointment = new Appointment(
                "apt-3",
                LocalDateTime.of(2026, 4, 8, 10, 0),
                60,
                2
        );

        assertFalse(individualRule.isValid(appointment));
    }

    @Test
    void isValid_NullAppointment_ReturnsFalse() {
        assertFalse(individualRule.isValid(null));
    }

    @Test
    void getSupportedType_ReturnsIndividual() {
        assertEquals(AppointmentType.INDIVIDUAL, individualRule.getSupportedType());
    }
}
