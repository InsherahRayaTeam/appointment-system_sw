package org.example.service;

import org.example.domain.Appointment;

/**
 * Represents duration rule in the system.
 */
public class DurationRule implements BookingRuleStrategy {

    private final int maxDuration = 120;

    /**
     * Checks whether valid is true.
     *
     * @param appointment value for appointment
     *
     * @return true when the action is valid or successful, otherwise false
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getDurationMinutes() <= maxDuration;
    }
}
