package org.example.service;

import org.example.domain.Appointment;

/**
 * Strategy contract for validating appointment booking constraints.
 *
 * @author appointment-system
 * @version 1.0
 */
public interface BookingRuleStrategy {

    /**
     * Validates whether the provided appointment satisfies this rule.
     *
     * @param appointment appointment candidate to validate
     * @return true when the rule accepts the appointment, otherwise false
     */
    boolean isValid(Appointment appointment);
}