package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

/**
 * Enforces validation rules for assessment appointments.
 */
public class AssessmentRule implements AppointmentTypeRule {

    private static final int MIN_DURATION_MINUTES = 60;

    /**
     * Checks whether the appointment satisfies assessment rules.
     *
     * @param appointment appointment to validate
     * @return true when valid, otherwise false
     */
    @Override
    public boolean isValid(Appointment appointment) {
        if (appointment == null) {
            return false;
        }
        return appointment.getDurationMinutes() >= MIN_DURATION_MINUTES;
    }

    /**
     * Returns the supported appointment type.
     *
     * @return supported appointment type
     */
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.ASSESSMENT;
    }
}