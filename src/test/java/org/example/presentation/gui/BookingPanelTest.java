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
class BookingPanelTest extends GuiTestSupport {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentBookingService appointmentBookingService;

    @Mock
    private Runnable onBookingSuccess;

    private SystemUser user;

    @BeforeEach
    void setUp() {
        user = new SystemUser("user-1", "user@example.com", "secret", UserRole.USER);
    }

    @Test
    void testBooking_validAppointment_callsService() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("10:00")));
        when(appointmentBookingService.bookAppointment("user@example.com", "10:00", "30", "2", AppointmentType.URGENT))
                .thenReturn(BookingStatus.SUCCESS);

        BookingPanel panel = new BookingPanel(user, appointmentService, appointmentBookingService, onBookingSuccess);
        JComboBox<?> slotComboBox = getPrivateField(panel, "slotComboBox", JComboBox.class);
        JComboBox<?> typeComboBox = getPrivateField(panel, "typeComboBox", JComboBox.class);
        JTextField durationField = getPrivateField(panel, "durationField", JTextField.class);
        JTextField participantCountField = getPrivateField(panel, "participantCountField", JTextField.class);
        AbstractButton bookButton = findButton(panel, "Book Appointment");

        runOnEdt(() -> {
            slotComboBox.setSelectedIndex(0);
            typeComboBox.setSelectedItem(AppointmentType.URGENT);
            durationField.setText("30");
            participantCountField.setText("2");
        });

        // Act
        clickButton(bookButton);

        // Assert
        verify(appointmentBookingService).bookAppointment("user@example.com", "10:00", "30", "2", AppointmentType.URGENT);
        verify(onBookingSuccess).run();
        verify(appointmentService, org.mockito.Mockito.times(2)).getAvailableSlots();
    }

    @Test
    void testBooking_invalidInput_showsError() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("10:00")));
        when(appointmentBookingService.bookAppointment("user@example.com", "10:00", "bad", "2", AppointmentType.NORMAL))
                .thenReturn(BookingStatus.INVALID_DURATION);

        BookingPanel panel = new BookingPanel(user, appointmentService, appointmentBookingService, onBookingSuccess);
        JComboBox<?> slotComboBox = getPrivateField(panel, "slotComboBox", JComboBox.class);
        JComboBox<?> typeComboBox = getPrivateField(panel, "typeComboBox", JComboBox.class);
        JTextField durationField = getPrivateField(panel, "durationField", JTextField.class);
        JTextField participantCountField = getPrivateField(panel, "participantCountField", JTextField.class);
        AbstractButton bookButton = findButton(panel, "Book Appointment");

        runOnEdt(() -> {
            slotComboBox.setSelectedIndex(0);
            typeComboBox.setSelectedItem(AppointmentType.NORMAL);
            durationField.setText("bad");
            participantCountField.setText("2");
        });

        // Act
        clickButton(bookButton);

        // Assert
        verify(appointmentBookingService).bookAppointment("user@example.com", "10:00", "bad", "2", AppointmentType.NORMAL);
        verifyNoInteractions(onBookingSuccess);
        assertTrue(slotComboBox.isEnabled());
        assertEquals("bad", durationField.getText());
        assertEquals("Duration is invalid. Allowed range is 15-120 minutes.", GuiMessageHelper.toMessage(BookingStatus.INVALID_DURATION));
    }

    @Test
    void testBooking_noAvailableSlots_showsWarning() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.emptyList());

        BookingPanel panel = new BookingPanel(user, appointmentService, appointmentBookingService, onBookingSuccess);
        AbstractButton bookButton = findButton(panel, "Book Appointment");

        // Act
        clickButton(bookButton);

        // Assert
        verifyNoInteractions(appointmentBookingService);
        verifyNoInteractions(onBookingSuccess);
    }
}

