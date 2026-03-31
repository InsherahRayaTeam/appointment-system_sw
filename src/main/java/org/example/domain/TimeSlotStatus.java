package org.example.domain;

/**
 * Represents the status of a time slot.
 *
 * @author appointment-system
 * @version 1.0
 */
public enum TimeSlotStatus {
    /**
     * Slot is available for booking.
     */
    AVAILABLE,

    /**
     * Slot has been booked by a customer.
     */
    BOOKED,

    /**
     * Slot has been cancelled by an administrator.
     */
    CANCELLED
}

