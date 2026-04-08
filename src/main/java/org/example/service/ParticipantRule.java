package org.example.service;

import org.example.domain.Appointment;

/**
 * Represents participant rule in the system.
 */
public class ParticipantRule implements BookingRuleStrategy {

    private final int maxParticipants = 5;

    /**
     * Checks whether valid is true.
     *
     * @param appointment value for appointment
     *
     * @return true when the action is valid or successful, otherwise false
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getParticipants() <= maxParticipants;
    }
}
