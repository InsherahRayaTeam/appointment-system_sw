package org.example.presentation.gui;

import org.example.service.BookingStatus;

/**
 * Represents gui message helper in the system.
 */
public final class GuiMessageHelper {

    private GuiMessageHelper() {
        // Utility class
    }

    /**
     * Runs to message for this class.
     *
     * @param status status value used for this operation
     *
     * @return text result from this method
     */
    public static String toMessage(BookingStatus status) {
        if (status == null) {
            return "Operation failed.";
        }

        return switch (status) {
            case SUCCESS -> "Operation completed successfully.";
            case BLANK_CUSTOMER_NAME -> "Customer name is required.";
            case BLANK_PHONE_NUMBER -> "Phone number is required.";
            case BLANK_SLOT_TIME -> "Please select an appointment date/day/time slot.";
            case INVALID_DURATION -> "Duration is invalid. Allowed range is 15-120 minutes.";
            case INVALID_PARTICIPANT_COUNT -> "Participant count is invalid. Allowed range is 1-10.";
            case INVALID_PHONE_NUMBER -> "Phone number is invalid. Use a valid local or international format.";
            case INVALID_SLOT_DATE_TIME -> "Slot date/time is invalid. Please choose a future date and time.";
            case INVALID_APPOINTMENT_RULES -> "Selected appointment type does not satisfy the booking rules.";
            case DUPLICATE_SLOT -> "The selected slot already exists.";
            case SLOT_NOT_FOUND -> "Selected slot was not found.";
            case SLOT_ALREADY_BOOKED -> "Selected slot is already booked.";
            case UNAUTHORIZED -> "You are not authorized to perform this action.";
            case APPOINTMENT_NOT_FOUND -> "Reservation was not found.";
            case APPOINTMENT_NOT_FUTURE -> "Only future reservations can be changed.";
            case APPOINTMENT_ALREADY_CANCELLED -> "Reservation is already cancelled.";
            case APPOINTMENT_ALREADY_ATTENDED -> "Reservation has already been marked as attended.";
            case APPOINTMENT_ALREADY_NOT_ATTENDED -> "Reservation has already been marked as not attended.";
            case APPOINTMENT_ALREADY_COMPLETED -> "Reservation has already been completed.";
            case APPOINTMENT_NOT_ATTENDED -> "Reservation must be marked as attended before it can be completed.";
            case UPDATE_FAILED -> "Reservation update failed. Please try again.";
        };
    }
}
