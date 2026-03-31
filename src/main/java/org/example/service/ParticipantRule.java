package org.example.service;

import org.example.domain.Appointment;

/**
 * Strategy implementation that enforces appointment participant bounds.
 *
 * Validates that participant count is within valid range:
 * - Minimum: 1 participant (at least 1 person must participate)
 * - Maximum: 5 participants (cannot exceed group size)
 *
 * @author appointment-system
 * @version 1.0
 */
public class ParticipantRule implements BookingRuleStrategy {

    private static final int MIN_PARTICIPANTS = 1;
    private static final int MAX_PARTICIPANTS = 5;

    /**
     * Checks whether appointment participant count is within valid bounds.
     *
     * @param appointment appointment candidate to validate
     * @return true when participant count is between min (1) and max (5), otherwise false
     * @throws NullPointerException when appointment is null
     */
    @Override
    public boolean isValid(Appointment appointment) {
        if (appointment == null) {
            throw new NullPointerException("appointment cannot be null");
        }
        int participants = appointment.getParticipants();
        return participants >= MIN_PARTICIPANTS && participants <= MAX_PARTICIPANTS;
    }
}