package org.example.service;

import org.example.domain.Appointment;

/**
 * Strategy implementation that enforces the maximum appointment duration.
 *
 * @author appointment-system
 * @version 1.0
 */
public class DurationRule implements BookingRuleStrategy {

    private final int maxDuration = 120;

    /**
     * Checks whether appointment duration does not exceed the configured maximum.
     *
     * @param appointment appointment candidate to validate
     * @return true when duration is within limit, otherwise false
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getDurationMinutes() <= maxDuration;
    }
}