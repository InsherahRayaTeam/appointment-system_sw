package org.example.service;

import org.example.domain.Appointment;

/**
 * Strategy implementation that enforces the maximum participant count.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ParticipantRule implements BookingRuleStrategy {

    private final int maxParticipants = 5;

    /**
     * Checks whether appointment participants do not exceed the configured maximum.
     *
     * @param appointment appointment candidate to validate
     * @return true when participant count is within limit, otherwise false
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getParticipants() <= maxParticipants;
    }
}