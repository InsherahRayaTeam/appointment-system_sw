package org.example.notification;

/**
 * Extended observer contract for handling typed domain events.
 *
 * @author appointment-system
 * @version 1.0
 */
public interface EventObserver {

    /**
     * Called when an appointment is booked.
     *
     * @param event the booking event
     */
    void onAppointmentBooked(AppointmentBookedEvent event);

    /**
     * Called when an appointment is modified.
     *
     * @param event the modification event
     */
    void onAppointmentModified(AppointmentModifiedEvent event);

    /**
     * Called when an appointment is cancelled.
     *
     * @param event the cancellation event
     */
    void onAppointmentCancelled(AppointmentCancelledEvent event);

    /**
     * Called when a slot is cancelled by admin.
     *
     * @param event the slot cancellation event
     */
    void onSlotCancelled(SlotCancelledEvent event);
}

