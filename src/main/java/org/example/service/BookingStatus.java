package org.example.service;

/**
 * Represents the outcome of an appointment booking attempt.
 * SUCCESS: booking completed and appointment was saved.
 * BLANK_CUSTOMER_NAME: customer name input was blank.
 * BLANK_SLOT_TIME: slot time input was blank.
 * INVALID_DURATION: duration input was invalid or outside allowed rules.
 * INVALID_PARTICIPANT_COUNT: participant count input was invalid or outside allowed rules.
 * SLOT_NOT_FOUND: requested slot time does not exist.
 * SLOT_ALREADY_BOOKED: requested slot exists but is not available.
 */
public enum BookingStatus {
    SUCCESS,
    BLANK_CUSTOMER_NAME,
    BLANK_SLOT_TIME,
    INVALID_DURATION,
    INVALID_PARTICIPANT_COUNT,
    SLOT_NOT_FOUND,
    SLOT_ALREADY_BOOKED
}

