package org.example.domain;

/**
 * Represents appointment slot in the system.
 */
public class AppointmentSlot {

    private String time;
    private boolean booked;

    /**
     * Creates a new appointment slot object with the given values.
     *
     * @param time time value used by this method
     */
    public AppointmentSlot(String time) {
        this.time = time;
        this.booked = false;
    }

    /**
     * Returns the time.
     *
     * @return text result from this method
     */
    public String getTime() {
        return time;
    }

    /**
     * Checks whether booked is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isBooked() {
        return booked;
    }

    /**
     * Checks whether available is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isAvailable() {
        return !booked;
    }

    /**
     * Runs book for this class.
     */
    public void book() {
        booked = true;
    }

    /**
     * Runs release for this class.
     */
    public void release() {
        booked = false;
    }
}
