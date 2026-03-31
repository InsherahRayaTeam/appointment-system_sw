package org.example.service;

import org.example.domain.Appointment;

/**
 * Strategy implementation that enforces appointment duration bounds.
 *
 * Validates that duration is within valid range:
 * - Minimum: 1 minute (must be at least 1 minute long)
 * - Maximum: 120 minutes (must not exceed 2 hours)
 *
 * @author appointment-system
 * @version 1.0
 */
public class DurationRule implements BookingRuleStrategy {

    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 120;

    /**
     * Checks whether appointment duration is within valid bounds.
     *
     * @param appointment appointment candidate to validate
     * @return true when duration is between min (1) and max (120) minutes, otherwise false
     * @throws NullPointerException when appointment is null
     */
    @Override
    public boolean isValid(Appointment appointment) {
        if (appointment == null) {
            throw new NullPointerException("appointment cannot be null");
        }
        int duration = appointment.getDurationMinutes();
        return duration >= MIN_DURATION && duration <= MAX_DURATION;
    }
}