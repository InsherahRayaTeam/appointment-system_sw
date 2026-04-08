package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

/**
 * Enforces validation rules for in-person appointments.
 */
public class InPersonRule implements AppointmentTypeRule {

    private static final int MIN_PARTICIPANTS = 1;

    /**
     * Checks whether the appointment satisfies in-person rules.
     *
     * @param appointment appointment to validate
     * @return true when valid, otherwise false
     */
    @Override
    public boolean isValid(Appointment appointment) {
        if (appointment == null) {
            return false;
        }
        return appointment.getParticipantCount() >= MIN_PARTICIPANTS;
    }

    /**
     * Returns the supported appointment type.
     *
     * @return supported appointment type
     */
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.IN_PERSON;
    }
}