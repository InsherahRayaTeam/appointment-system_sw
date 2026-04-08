package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

import java.util.Objects;

/**
 * Represents virtual rule in the system.
 */
public class VirtualRule implements AppointmentTypeRule {

    private static final int MAX_DURATION_MINUTES = 90;
    private static final int MAX_PARTICIPANTS = 2;

    /**
     * Returns the supported type.
     *
     * @return virtual appointment type
     */
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.VIRTUAL;
    }

    /**
     * Checks whether the appointment satisfies virtual appointment rules.
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