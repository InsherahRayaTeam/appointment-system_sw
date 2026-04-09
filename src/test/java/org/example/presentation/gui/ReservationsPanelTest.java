package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.service.AppointmentBookingService;
import org.example.service.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationsPanelTest extends GuiTestSupport {

    @Mock
    private AppointmentBookingService appointmentBookingService;

    private SystemUser user;

    @BeforeEach
    void setUp() {
        user = new SystemUser("user-1", "user@example.com", "secret", UserRole.USER);
    }

    @Test
    void testCancelReservation_updatesState() {
        // Arrange
        Appointment reservation = new Appointment(
                "res-1",
                "user@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
        AtomicInteger refreshCount = new AtomicInteger();
        when(appointmentBookingService.getReservationsForCustomer("user@example.com"))
                .thenAnswer(invocation -> refreshCount.getAndIncrement() == 0
                        ? Collections.singletonList(reservation)
                        : Collections.emptyList());
        when(appointmentBookingService.cancelOwnAppointment("res-1")).thenReturn(BookingStatus.SUCCESS);

        ReservationsPanel panel = new ReservationsPanel(user, appointmentBookingService);
        JTable table = getPrivateField(panel, "reservationsTable", JTable.class);
        DefaultTableModel model = getPrivateField(panel, "tableModel", DefaultTableModel.class);
        JLabel infoLabel = getPrivateField(panel, "infoLabel", JLabel.class);
        AbstractButton cancelButton = findButton(panel, "Cancel Reservation");

        runOnEdt(() -> table.setRowSelectionInterval(0, 0));

        // Act
        clickButton(cancelButton);

        // Assert
        verify(appointmentBookingService).cancelOwnAppointment("res-1");
        assertEquals(0, model.getRowCount());
        assertTrue(infoLabel.getText().contains("No reservations found"));
    }

    @Test
    void testModifyReservationButton_notPresentForUserFlow() {
        ReservationsPanel panel = new ReservationsPanel(user, appointmentBookingService);
        AbstractButton modifyButton = findButton(panel, "Modify Reservation");

        assertNull(modifyButton);
        verify(appointmentBookingService, never()).modifyOwnAppointment(anyString(), anyString());
    }

    @Test
    void testCancelReservation_withoutSelection_showsWarning() {
        // Arrange

        ReservationsPanel panel = new ReservationsPanel(user, appointmentBookingService);
        AbstractButton cancelButton = findButton(panel, "Cancel Reservation");

        // Act
        clickButton(cancelButton);

        // Assert
        verifyNoInteractions(appointmentBookingService);
    }
}

