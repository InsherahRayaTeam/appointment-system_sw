package org.example.notification;

import org.example.domain.Appointment;

/**
 * Event fired when an appointment is cancelled.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AppointmentCancelledEvent {

    private final Appointment appointment;
    private final String username;
    private final String reason;

    /**
     * Creates a cancellation event.
     *
     * @param appointment the cancelled appointment
     * @param username the username of the canceller
     * @param reason reason for cancellation
     */
    public AppointmentCancelledEvent(Appointment appointment, String username, String reason) {
        this.appointment = appointment;
        this.username = username;
        this.reason = reason;
    }

    /**
     * Returns the cancelled appointment.
     *
     * @return the appointment
     */
    public Appointment getAppointment() {
        return appointment;
    }

    /**
     * Returns the username of the canceller.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the reason for cancellation.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "AppointmentCancelledEvent{" +
                "appointment=" + appointment +
                ", username='" + username + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}

