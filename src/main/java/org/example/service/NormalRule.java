package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

import java.util.Objects;

/**
 * Represents normal rule in the system.
 */
public class NormalRule implements AppointmentTypeRule {

    /**
     * Returns the supported type.
     *
     * @return normal appointment type
     */
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.NORMAL;
    }

    /**
     * Checks whether the appointment satisfies the normal rule.
     *
     * @param appointment value for appointment
     * @return true when the appointment is valid
     */
    @Override
    public boolean isValid(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");
        return true;
    }
}