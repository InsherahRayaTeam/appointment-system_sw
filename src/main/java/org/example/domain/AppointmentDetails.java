package org.example.domain;

import java.time.LocalDateTime;

/**
 * Groups appointment customer and scheduling details.
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
     * @param participants number of participants
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

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public int getParticipants() {
        return participants;
    }
}