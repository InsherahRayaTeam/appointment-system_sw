package org.example.service;
import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import java.util.Objects;
/**
 * Represents virtual rule in the system.
 */
public class VirtualRule implements AppointmentTypeRule {
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.VIRTUAL;
    }
    @Override
    public boolean isValid(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");
        return true;
    }
}
