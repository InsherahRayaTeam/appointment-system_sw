package org.example.service;

import org.example.domain.Appointment;

public class DurationRule implements BookingRuleStrategy {

    private final int maxDuration = 120;

    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getDurationMinutes() <= maxDuration;
    }
}