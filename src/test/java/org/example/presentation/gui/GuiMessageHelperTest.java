package org.example.presentation.gui;

import org.example.service.BookingStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiMessageHelperTest {

    @Test
    void toMessage_NullStatus_ReturnsDefaultFailureMessage() {
        assertEquals("Operation failed.", GuiMessageHelper.toMessage(null));
    }

    @Test
    void toMessage_ReturnsExpectedMessageForEachStatus() {
        assertEquals("Operation completed successfully.", GuiMessageHelper.toMessage(BookingStatus.SUCCESS));
        assertEquals("Customer name is required.", GuiMessageHelper.toMessage(BookingStatus.BLANK_CUSTOMER_NAME));
        assertEquals("Phone number is required.", GuiMessageHelper.toMessage(BookingStatus.BLANK_PHONE_NUMBER));
        assertEquals(
                "Please select an appointment date/day/time slot.",
                GuiMessageHelper.toMessage(BookingStatus.BLANK_SLOT_TIME)
        );
        assertEquals("Duration is invalid. Allowed range is 15-120 minutes.", GuiMessageHelper.toMessage(BookingStatus.INVALID_DURATION));
        assertEquals(
                "Participant count is invalid. Allowed range is 1-10.",
                GuiMessageHelper.toMessage(BookingStatus.INVALID_PARTICIPANT_COUNT)
        );
        assertEquals(
                "Phone number is invalid. Use a valid local or international format.",
                GuiMessageHelper.toMessage(BookingStatus.INVALID_PHONE_NUMBER)
        );
        assertEquals(
                "Slot date/time is invalid. Please choose a future date and time.",
                GuiMessageHelper.toMessage(BookingStatus.INVALID_SLOT_DATE_TIME)
        );
        assertEquals(
                "Selected appointment type does not satisfy the booking rules.",
                GuiMessageHelper.toMessage(BookingStatus.INVALID_APPOINTMENT_RULES)
        );
        assertEquals("The selected slot already exists.", GuiMessageHelper.toMessage(BookingStatus.DUPLICATE_SLOT));
        assertEquals("Selected slot was not found.", GuiMessageHelper.toMessage(BookingStatus.SLOT_NOT_FOUND));
        assertEquals("Selected slot is already booked.", GuiMessageHelper.toMessage(BookingStatus.SLOT_ALREADY_BOOKED));
        assertEquals(
                "You are not authorized to perform this action.",
                GuiMessageHelper.toMessage(BookingStatus.UNAUTHORIZED)
        );
        assertEquals("Reservation was not found.", GuiMessageHelper.toMessage(BookingStatus.APPOINTMENT_NOT_FOUND));
        assertEquals(
                "Only future reservations can be changed.",
                GuiMessageHelper.toMessage(BookingStatus.APPOINTMENT_NOT_FUTURE)
        );
        assertEquals(
                "Reservation is already cancelled.",
                GuiMessageHelper.toMessage(BookingStatus.APPOINTMENT_ALREADY_CANCELLED)
        );
        assertEquals(
                "Reservation has already been marked as attended.",
                GuiMessageHelper.toMessage(BookingStatus.APPOINTMENT_ALREADY_ATTENDED)
        );
        assertEquals(
                "Reservation has already been marked as not attended.",
                GuiMessageHelper.toMessage(BookingStatus.APPOINTMENT_ALREADY_NOT_ATTENDED)
        );
        assertEquals(
                "Reservation has already been completed.",
                GuiMessageHelper.toMessage(BookingStatus.APPOINTMENT_ALREADY_COMPLETED)
        );
        assertEquals(
                "Reservation must be marked as attended before it can be completed.",
                GuiMessageHelper.toMessage(BookingStatus.APPOINTMENT_NOT_ATTENDED)
        );
        assertEquals(
                "Reservation update failed. Please try again.",
                GuiMessageHelper.toMessage(BookingStatus.UPDATE_FAILED)
        );
    }
}
