package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

/**
 * Defines the operations for appointment type rule.
 */
public interface AppointmentTypeRule {

    /**
     * Returns the supported type.
     *
     * @return requested value from this object
     */
    AppointmentType getSupportedType();

    /**
     * Checks whether valid is true.
     *
     * @param appointment value for appointment
     *
     * @return true when the action is valid or successful, otherwise false
     */
    boolean isValid(Appointment appointment);
}

