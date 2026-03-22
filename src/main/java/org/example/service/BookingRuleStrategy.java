package org.example.service;

import org.example.domain.Appointment;

public interface BookingRuleStrategy {
    boolean isValid(Appointment appointment);
}