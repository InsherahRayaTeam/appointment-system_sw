package org.example.presentation.gui;

import org.example.service.BookingStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiMessageHelperTest {

    @Test
    void testShowErrorMessage() {
        // Arrange / Act / Assert
        assertEquals("Operation failed.", GuiMessageHelper.toMessage(null));
        assertEquals("Customer name is required.", GuiMessageHelper.toMessage(BookingStatus.BLANK_CUSTOMER_NAME));
        assertEquals("Duration is invalid. Allowed range is 15-120 minutes.", GuiMessageHelper.toMessage(BookingStatus.INVALID_DURATION));
        assertEquals("Reservation was not found.", GuiMessageHelper.toMessage(BookingStatus.APPOINTMENT_NOT_FOUND));
    }

    @Test
    void testShowSuccessMessage() {
        // Arrange / Act / Assert
        assertEquals("Operation completed successfully.", GuiMessageHelper.toMessage(BookingStatus.SUCCESS));
    }
}
