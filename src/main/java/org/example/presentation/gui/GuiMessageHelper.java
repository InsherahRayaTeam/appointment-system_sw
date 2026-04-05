package org.example.presentation.gui;

import org.example.service.BookingStatus;

/**
 * Maps service-layer statuses to concise UI messages.
 *
 * @author appointment-system
 * @version 1.0
 */
public final class GuiMessageHelper {

    private GuiMessageHelper() {
        // Utility class
    }

    /**
     * Converts a booking status into a user-facing message.
     *
     * @param status service outcome status
     * @return short UI message
     */
    public static String toMessage(BookingStatus status) {
        if (status == null) {
            return "Operation failed.";
        }

        switch (status) {
            case SUCCESS:
                return "Operation completed successfully.";
            case BLANK_CUSTOMER_NAME:
                return "Customer name is required.";
            case BLANK_SLOT_TIME:
                return "Please select a slot time.";
            case INVALID_DURATION:
                return "Duration is invalid. Allowed range is 15-120 minutes.";
            case INVALID_PARTICIPANT_COUNT:
                return "Participant count is invalid. Allowed range is 1-10.";
            case SLOT_NOT_FOUND:
                return "Selected slot was not found.";
            case SLOT_ALREADY_BOOKED:
                return "Selected slot is already booked.";
            case UNAUTHORIZED:
                return "You are not authorized to perform this action.";
            case APPOINTMENT_NOT_FOUND:
                return "Reservation was not found.";
            case APPOINTMENT_NOT_FUTURE:
                return "Only future reservations can be changed.";
            case APPOINTMENT_ALREADY_CANCELLED:
                return "Reservation is already cancelled.";
            case UPDATE_FAILED:
                return "Reservation update failed. Please try again.";
            default:
                return "Operation failed.";
        }
    }
}

