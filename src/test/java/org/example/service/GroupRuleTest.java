package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupRuleTest {

    private final GroupRule groupRule = new GroupRule();

    @Test
    void shouldAllowGroupAppointmentWithEnoughParticipants() {
        Appointment appointment = new Appointment("apt-1", LocalDateTime.of(2026, 3, 22, 10, 0), 60, 3);

        assertTrue(groupRule.isValid(appointment));
    }

    @Test
    void shouldRejectGroupAppointmentWithTooFewParticipants() {
        Appointment appointment = new Appointment("apt-1", LocalDateTime.of(2026, 3, 22, 10, 0), 60, 2);

        assertFalse(groupRule.isValid(appointment));
    }

    @Test
    void shouldExposeSupportedType() {
        assertEquals(AppointmentType.GROUP, groupRule.getSupportedType());
    }

    @Test
    void shouldThrowWhenAppointmentIsNull() {
        assertThrows(NullPointerException.class, () -> groupRule.isValid(null));
    }
}

