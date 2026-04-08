package org.example.service;
import org.example.domain.Appointment;
import org.example.domain.AppointmentType;
import java.util.Objects;
/**
 * Represents follow up rule in the system.
 */
public class FollowUpRule implements AppointmentTypeRule {
    @Override
    public AppointmentType getSupportedType() {
        return AppointmentType.FOLLOW_UP;
    }
    @Override
    public boolean isValid(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");
        return true;
    }
}
