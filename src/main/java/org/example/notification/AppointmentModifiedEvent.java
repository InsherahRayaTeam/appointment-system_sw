package org.example.notification;

import org.example.domain.Appointment;

/**
 * Event fired when an appointment is modified.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AppointmentModifiedEvent {

    private final Appointment oldAppointment;
    private final Appointment newAppointment;
    private final String username;

    /**
     * Creates a modification event.
     *
     * @param oldAppointment the appointment before modification
     * @param newAppointment the appointment after modification
     * @param username the username of the modifier
     */
    public AppointmentModifiedEvent(Appointment oldAppointment, Appointment newAppointment, String username) {
        this.oldAppointment = oldAppointment;
        this.newAppointment = newAppointment;
        this.username = username;
    }

    /**
     * Returns the appointment state before modification.
     *
     * @return the old appointment
     */
    public Appointment getOldAppointment() {
        return oldAppointment;
    }

    /**
     * Returns the appointment state after modification.
     *
     * @return the new appointment
     */
    public Appointment getNewAppointment() {
        return newAppointment;
    }

    /**
     * Returns the username of the modifier.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "AppointmentModifiedEvent{" +
                "oldAppointment=" + oldAppointment +
                ", newAppointment=" + newAppointment +
                ", username='" + username + '\'' +
                '}';
    }
}

