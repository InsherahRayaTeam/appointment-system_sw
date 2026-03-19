package org.example.domain;

/**
 * Represents a confirmed booking for a specific appointment slot.
 */
public final class Appointment {
    private final String customerName;
    private final String slotTime;
    private final int durationMinutes;
    private final int participantCount;
    private final AppointmentStatus status;

    /**
     * Creates an appointment with all required booking details.
     *
     * @param customerName the customer name associated with the booking
     * @param slotTime the time label of the booked slot
     * @param durationMinutes the appointment duration in minutes
     * @param participantCount the number of participants in the appointment
     * @param status the current status of the appointment
     */
    public Appointment(
            String customerName,
            String slotTime,
            int durationMinutes,
            int participantCount,
            AppointmentStatus status
    ) {
        this.customerName = customerName;
        this.slotTime = slotTime;
        this.durationMinutes = durationMinutes;
        this.participantCount = participantCount;
        this.status = status;
    }

    /**
     * Returns the customer name associated with this appointment.
     *
     * @return the customer name
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Returns the time label of the booked slot.
     *
     * @return the slot time
     */
    public String getSlotTime() {
        return slotTime;
    }

    /**
     * Returns the duration of the appointment in minutes.
     *
     * @return the duration in minutes
     */
    public int getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Returns the participant count for this appointment.
     *
     * @return the participant count
     */
    public int getParticipantCount() {
        return participantCount;
    }

    /**
     * Returns the current appointment status.
     *
     * @return the appointment status
     */
    public AppointmentStatus getStatus() {
        return status;
    }
}

