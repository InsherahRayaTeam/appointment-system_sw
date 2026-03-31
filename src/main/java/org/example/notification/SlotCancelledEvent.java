package org.example.notification;

/**
 * Event fired when an admin cancels a time slot, affecting all potential bookings.
 *
 * @author appointment-system
 * @version 1.0
 */
public class SlotCancelledEvent {

    private final String slotTime;
    private final String adminUsername;

    /**
     * Creates a slot cancellation event.
     *
     * @param slotTime the time of the cancelled slot
     * @param adminUsername the username of the admin who cancelled it
     */
    public SlotCancelledEvent(String slotTime, String adminUsername) {
        this.slotTime = slotTime;
        this.adminUsername = adminUsername;
    }

    /**
     * Returns the slot time that was cancelled.
     *
     * @return the slot time
     */
    public String getSlotTime() {
        return slotTime;
    }

    /**
     * Returns the username of the admin who cancelled the slot.
     *
     * @return the username
     */
    public String getAdminUsername() {
        return adminUsername;
    }

    @Override
    public String toString() {
        return "SlotCancelledEvent{" +
                "slotTime='" + slotTime + '\'' +
                ", adminUsername='" + adminUsername + '\'' +
                '}';
    }
}

