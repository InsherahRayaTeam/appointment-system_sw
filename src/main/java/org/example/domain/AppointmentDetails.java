package org.example.domain;

import java.time.LocalDateTime;

/**
 * Groups customer and scheduling details for an appointment.
 */
public final class AppointmentDetails {

    private final String customerName;
    private final String customerEmail;
    private final String customerPhoneNumber;
    private final LocalDateTime startTime;
    private final int duration;
    private final int participants;

    /**
     * Creates appointment details.
     *
     * @param customerName customer name
     * @param customerEmail customer email
     * @param customerPhoneNumber customer phone number
     * @param startTime appointment start time
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     */
    public AppointmentDetails(
            String customerName,
            String customerEmail,
            String customerPhoneNumber,
            LocalDateTime startTime,
            int duration,
            int participants
    ) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhoneNumber = customerPhoneNumber;
        this.startTime = startTime;
        this.duration = duration;
        this.participants = participants;
    }

    /**
     * Returns the customer name.
     *
     * @return customer name
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Returns the customer email.
     *
     * @return customer email
     */
    public String getCustomerEmail() {
        return customerEmail;
    }

    /**
     * Returns the customer phone number.
     *
     * @return customer phone number
     */
    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    /**
     * Returns the appointment start time.
     *
     * @return appointment start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the appointment duration.
     *
     * @return duration in minutes
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the number of participants.
     *
     * @return number of participants
     */
    public int getParticipants() {
        return participants;
    }
}