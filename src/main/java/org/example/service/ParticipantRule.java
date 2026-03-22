package org.example.service;

import org.example.domain.Appointment;

public class ParticipantRule implements BookingRuleStrategy {

    private final int maxParticipants = 5;

    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getParticipants() <= maxParticipants;
    }
}