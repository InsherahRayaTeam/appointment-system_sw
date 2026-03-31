package org.example.notification;

import org.example.domain.Appointment;

/**
 * Event fired when an appointment is booked.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AppointmentBookedEvent {

    private final Appointment appointment;
    private final String username;

    /**
     * Creates a booked event for the given appointment.
     *
     * @param appointment the booked appointment
     * @param username the username of the booker
     */
    public AppointmentBookedEvent(Appointment appointment, String username) {
        this.appointment = appointment;
        this.username = username;
    }

    /**
     * Returns the booked appointment.
     *
     * @return the appointment
     */
    public Appointment getAppointment() {
        return appointment;
    }

    /**
     * Returns the username of the booker.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "AppointmentBookedEvent{" +
                "appointment=" + appointment +
                ", username='" + username + '\'' +
                '}';
    }
}

