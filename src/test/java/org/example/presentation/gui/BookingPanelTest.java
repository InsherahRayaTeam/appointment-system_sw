package org.example.presentation.gui;

import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentType;
import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Timeout(10)
class BookingPanelTest extends GuiTestSupport {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentBookingService appointmentBookingService;

    @Mock
    private Runnable onBookingSuccess;

    private SystemUser user;
    private BookingPanel panel;

    @BeforeEach
    void setUp() {
        user = new SystemUser("user-1", "user@example.com", "secret", UserRole.USER);
    }

    @AfterEach
    void tearDown() {
        // Dispose panel if it's a window, and ensure no Swing windows remain
        if (panel != null) {
            disposeIfWindow(panel);
            panel = null;
        }

        // Dispose any remaining windows to avoid blocking the test runner
        java.awt.Window[] windows = java.awt.Window.getWindows();
        for (java.awt.Window w : windows) {
            disposeIfWindow(w);
        }
    }

    @Test
    void testBooking_validAppointment_callsService() {
        // Arrange
        AppointmentSlot slot = new AppointmentSlot("10:00");
        String slotSelection = slot.getDateDayTimeLabel();
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(slot));
        when(appointmentBookingService.bookAppointment(
                "user@example.com",
                "+1 202-555-0199",
                slotSelection,
                "30",
                "2",
                AppointmentType.URGENT
        ))
                .thenReturn(BookingStatus.SUCCESS);

        panel = callOnEdt(() -> new BookingPanel(user, appointmentService, appointmentBookingService, onBookingSuccess, new TestDialogDisplayer()));
        JTextField bookedForNameField = getPrivateField(panel, "bookedForNameField", JTextField.class);
        JTextField phoneNumberField = getPrivateField(panel, "phoneNumberField", JTextField.class);
        JComboBox<?> slotComboBox = getPrivateField(panel, "slotComboBox", JComboBox.class);
        JComboBox<?> typeComboBox = getPrivateField(panel, "typeComboBox", JComboBox.class);
        JTextField durationField = getPrivateField(panel, "durationField", JTextField.class);
        JTextField participantCountField = getPrivateField(panel, "participantCountField", JTextField.class);
        AbstractButton bookButton = findButton(panel, "Book Appointment");

        runOnEdt(() -> {
            bookedForNameField.setText("user@example.com");
            phoneNumberField.setText("+1 202-555-0199");
            slotComboBox.setSelectedIndex(0);
            typeComboBox.setSelectedItem(AppointmentType.URGENT);
            durationField.setText("30");
            participantCountField.setText("2");
        });

        // Act
        clickButton(bookButton);

        // Assert
        verify(appointmentBookingService).bookAppointment(
                "user@example.com",
                "+1 202-555-0199",
                slotSelection,
                "30",
                "2",
                AppointmentType.URGENT
        );
        verify(onBookingSuccess).run();
        verify(appointmentService, org.mockito.Mockito.times(2)).getAvailableSlots();
    }

    @Test
    void testBooking_invalidInput_showsError() {
        // Arrange
        AppointmentSlot slot = new AppointmentSlot("10:00");
        String slotSelection = slot.getDateDayTimeLabel();
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(slot));
        when(appointmentBookingService.bookAppointment(
                "user@example.com",
                "+1 202-555-0100",
                slotSelection,
                "bad",
                "2",
                AppointmentType.NORMAL
        ))
                .thenReturn(BookingStatus.INVALID_DURATION);

        panel = callOnEdt(() -> new BookingPanel(user, appointmentService, appointmentBookingService, onBookingSuccess, new TestDialogDisplayer()));
        JTextField bookedForNameField = getPrivateField(panel, "bookedForNameField", JTextField.class);
        JTextField phoneNumberField = getPrivateField(panel, "phoneNumberField", JTextField.class);
        JComboBox<?> slotComboBox = getPrivateField(panel, "slotComboBox", JComboBox.class);
        JComboBox<?> typeComboBox = getPrivateField(panel, "typeComboBox", JComboBox.class);
        JTextField durationField = getPrivateField(panel, "durationField", JTextField.class);
        JTextField participantCountField = getPrivateField(panel, "participantCountField", JTextField.class);
        AbstractButton bookButton = findButton(panel, "Book Appointment");

        runOnEdt(() -> {
            bookedForNameField.setText("user@example.com");
            phoneNumberField.setText("+1 202-555-0100");
            slotComboBox.setSelectedIndex(0);
            typeComboBox.setSelectedItem(AppointmentType.NORMAL);
            durationField.setText("bad");
            participantCountField.setText("2");
        });

        // Act
        clickButton(bookButton);

        // Assert
        verify(appointmentBookingService).bookAppointment(
                "user@example.com",
                "+1 202-555-0100",
                slotSelection,
                "bad",
                "2",
                AppointmentType.NORMAL
        );
        verifyNoInteractions(onBookingSuccess);
        assertTrue(slotComboBox.isEnabled());
        assertEquals("bad", durationField.getText());
        assertEquals("Duration is invalid. Allowed range is 15-120 minutes.", GuiMessageHelper.toMessage(BookingStatus.INVALID_DURATION));
    }

    @Test
    void testBooking_noAvailableSlots_showsWarning() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.emptyList());

        panel = callOnEdt(() -> new BookingPanel(user, appointmentService, appointmentBookingService, onBookingSuccess, new TestDialogDisplayer()));
        AbstractButton bookButton = findButton(panel, "Book Appointment");

        // Act
        clickButton(bookButton);

        // Assert
        verifyNoInteractions(appointmentBookingService);
        verifyNoInteractions(onBookingSuccess);
    }
}

