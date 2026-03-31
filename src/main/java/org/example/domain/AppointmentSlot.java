package org.example.domain;

/**
 * Represents a bookable appointment slot.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AppointmentSlot {

    private String time;
    private boolean booked;
    private boolean cancelled;

    /**
     * Creates an appointment slot for the provided time.
     *
     * @param time the slot time label (for example, "10:00")
     */
    public AppointmentSlot(String time) {
        this.time = time;
        this.booked = false;
        this.cancelled = false;
    }

    /**
     * Returns the time of this appointment slot.
     *
     * @return the configured slot time
     */
    public String getTime() {
        return time;
    }

    /**
     * Indicates whether this slot has already been booked.
     *
     * @return true when booked, otherwise false
     */
    public boolean isBooked() {
        return booked;
    }

    /**
     * Indicates whether this slot can still be booked.
     *
     * @return true when the slot is not booked, otherwise false
     */
    public boolean isAvailable() {
        return !booked;
    }

    /**
     * Marks this appointment slot as booked.
     */
    public void book() {
        booked = true;
    }

    /**
     * Marks this appointment slot as available again.
     */
    public void release() {
        booked = false;
    }

    /**
     * Indicates whether this slot has been cancelled by an administrator.
     *
     * @return true when slot is cancelled, otherwise false
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Marks this appointment slot as cancelled (admin action).
     */
    public void cancel() {
        cancelled = true;
    }
}