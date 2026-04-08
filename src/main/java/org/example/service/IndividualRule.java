package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

/**
 * Enforces validation rules for individual appointments.
 */
public class IndividualRule implements AppointmentTypeRule {

    private static final int REQUIRED_PARTICIPANT_COUNT = 1;

    /**
     * Checks whether the appointment satisfies individual rules.
     *
     * @param appointment appointment to validate
     * @return true when valid, otherwise false
     */
    @Override
    public boolean isValid(Appointment appointment) {
        if (appointment == null) {
            return false;
        }
        return appointment.getParticipantCount() == REQUIRED_PARTICIPANT_COUNT;
    }

    /**
     * Returns the supported appointment type.
     *
     * @return supported appointment type
     */
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.INDIVIDUAL;
    }
}