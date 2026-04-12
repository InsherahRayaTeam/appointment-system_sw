package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

import java.util.Objects;

/**
 * Represents urgent rule in the system.
 */
public class UrgentRule implements AppointmentTypeRule {

    private static final int MAX_URGENT_DURATION = 30;

    /**
     * Returns the appointment type this rule checks.
     *
     * @return the URGENT appointment type
     */
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.URGENT;
    }

    /**
     * Checks if an urgent appointment stays within the max duration.
     *
     * @param appointment appointment data to validate
     * @return true when duration is 30 minutes or less
     */
    @Override
    public boolean isValid(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");
        return appointment.getDurationMinutes() <= MAX_URGENT_DURATION;
    }
}

