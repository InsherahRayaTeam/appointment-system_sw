package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

import java.util.Objects;

/**
 * Represents group rule in the system.
 */
public class GroupRule implements AppointmentTypeRule {

    private static final int MIN_GROUP_PARTICIPANTS = 3;

    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.GROUP;
    }

    @Override
    public boolean isValid(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");
        return appointment.getParticipantCount() >= MIN_GROUP_PARTICIPANTS;
    }
}

