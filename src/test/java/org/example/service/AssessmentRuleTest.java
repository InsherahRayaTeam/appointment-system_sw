package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssessmentRuleTest {

    private final AssessmentRule assessmentRule = new AssessmentRule();

    @Test
    void isValid_DurationAtMinimumBoundary_ReturnsTrue() {
        Appointment appointment = new Appointment(
                "apt-1",
                LocalDateTime.of(2026, 4, 8, 10, 0),
                60,
                1
        );

        assertTrue(assessmentRule.isValid(appointment));
    }

    @Test
    void isValid_DurationBelowMinimum_ReturnsFalse() {
        Appointment appointment = new Appointment(
                "apt-2",
                LocalDateTime.of(2026, 4, 8, 10, 0),
                59,
                1
        );

        assertFalse(assessmentRule.isValid(appointment));
    }

    @Test
    void isValid_NullAppointment_ReturnsFalse() {
        assertFalse(assessmentRule.isValid(null));
    }

    @Test
    void getSupportedType_ReturnsAssessment() {
        assertEquals(AppointmentType.ASSESSMENT, assessmentRule.getSupportedType());
    }
}
