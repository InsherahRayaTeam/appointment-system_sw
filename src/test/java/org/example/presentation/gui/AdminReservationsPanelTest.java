package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReservationsPanelTest extends GuiTestSupport {

    @Mock
    private AppointmentBookingService appointmentBookingService;

    @Mock
    private AppointmentService appointmentService;

    @Test
    @SuppressWarnings("unchecked")
    void testCancelReservation_updatesState() {
        // Arrange
        Appointment reservation = new Appointment(
                "res-1",
                "customer@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
        when(appointmentBookingService.canCurrentUserManageReservations()).thenReturn(true);
        AtomicInteger reservationsRefreshCount = new AtomicInteger();
        when(appointmentBookingService.getManagedReservations())
                .thenAnswer(invocation -> reservationsRefreshCount.getAndIncrement() == 0
                        ? Collections.singletonList(reservation)
                        : Collections.emptyList());
        AtomicInteger slotsRefreshCount = new AtomicInteger();
        when(appointmentService.getAvailableSlots())
                .thenAnswer(invocation -> slotsRefreshCount.getAndIncrement() == 0
                        ? Collections.singletonList(new AppointmentSlot("11:00"))
                        : Collections.singletonList(new AppointmentSlot("11:00")));
        when(appointmentBookingService.cancelAppointment("res-1")).thenReturn(BookingStatus.SUCCESS);

        AdminReservationsPanel panel = new AdminReservationsPanel(appointmentBookingService, appointmentService);
        JTable table = getPrivateField(panel, "reservationsTable", JTable.class);
        DefaultTableModel model = getPrivateField(panel, "tableModel", DefaultTableModel.class);
        AbstractButton cancelButton = getPrivateField(panel, "cancelButton", AbstractButton.class);

        runOnEdt(() -> table.setRowSelectionInterval(0, 0));

        // Act
        clickButton(cancelButton);

        // Assert
        verify(appointmentBookingService).cancelAppointment("res-1");
        assertEquals(0, model.getRowCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testModifyReservation_validCase() {
        // Arrange
        Appointment reservation = new Appointment(
                "res-1",
                "customer@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
        Appointment modifiedReservation = new Appointment(
                "res-1",
                "customer@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                2,
                AppointmentStatus.MODIFIED
        );
        when(appointmentBookingService.canCurrentUserManageReservations()).thenReturn(true);
        AtomicInteger reservationsRefreshCount = new AtomicInteger();
        when(appointmentBookingService.getManagedReservations())
                .thenAnswer(invocation -> reservationsRefreshCount.getAndIncrement() == 0
                        ? Collections.singletonList(reservation)
                        : Collections.singletonList(modifiedReservation));
        AtomicInteger slotsRefreshCount = new AtomicInteger();
        when(appointmentService.getAvailableSlots())
                .thenAnswer(invocation -> slotsRefreshCount.getAndIncrement() == 0
                        ? Collections.singletonList(new AppointmentSlot("11:00"))
                        : Collections.singletonList(new AppointmentSlot("11:00")));
        when(appointmentBookingService.modifyAppointment("res-1", "11:00")).thenReturn(BookingStatus.SUCCESS);

        AdminReservationsPanel panel = new AdminReservationsPanel(appointmentBookingService, appointmentService);
        JTable table = getPrivateField(panel, "reservationsTable", JTable.class);
        DefaultTableModel model = getPrivateField(panel, "tableModel", DefaultTableModel.class);
        JComboBox slotComboBox = getPrivateField(panel, "slotComboBox", JComboBox.class);
        AbstractButton modifyButton = getPrivateField(panel, "modifyButton", AbstractButton.class);

        runOnEdt(() -> {
            table.setRowSelectionInterval(0, 0);
            slotComboBox.setSelectedItem("11:00");
        });

        // Act
        clickButton(modifyButton);

        // Assert
        verify(appointmentBookingService).modifyAppointment("res-1", "11:00");
        assertEquals(1, model.getRowCount());
        assertEquals("11:00", model.getValueAt(0, 2));
    }
}

