package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

import java.util.Objects;

/**
 * Represents follow-up rule in the system.
 */
public class FollowUpRule implements AppointmentTypeRule {

    private static final int MAX_DURATION_MINUTES = 60;
    private static final int MAX_PARTICIPANTS = 2;

    /**
     * Returns the supported type.
     *
     * @return follow-up appointment type
     */
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.FOLLOW_UP;
    }

    /**
     * Checks whether the appointment satisfies follow-up rules.
     *
     * @param appointment value for appointment
     * @return true when valid, otherwise false
     */
    @Override
    public boolean isValid(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");

        return appointment.getDurationMinutes() <= MAX_DURATION_MINUTES
                && appointment.getParticipantCount() <= MAX_PARTICIPANTS;
    }
}