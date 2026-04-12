package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

import java.util.Objects;

/**
 * Represents group rule in the system.
 */
public class GroupRule implements AppointmentTypeRule {

    private static final int MIN_GROUP_PARTICIPANTS = 3;

    /**
     * Returns the appointment type this rule checks.
     *
     * @return the GROUP appointment type
     */
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.GROUP;
    }

    /**
     * Checks if a group appointment has enough participants.
     *
     * @param appointment appointment data to validate
     * @return true when participant count is 3 or more
     */
    @Override
    public boolean isValid(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");
        return appointment.getParticipantCount() >= MIN_GROUP_PARTICIPANTS;
    }
}

