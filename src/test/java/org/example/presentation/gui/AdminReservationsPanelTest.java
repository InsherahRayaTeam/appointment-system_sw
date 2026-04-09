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
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("11:00")));
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
                LocalDate.now().plusDays(1).atTime(11, 0),
                60,
                2,
                AppointmentStatus.RESCHEDULED
        );
        AppointmentSlot replacementSlot = new AppointmentSlot(LocalDate.now().plusDays(1), LocalTime.of(11, 0));
        String replacementSelection = replacementSlot.getDateDayTimeLabel();
        when(appointmentBookingService.canCurrentUserManageReservations()).thenReturn(true);
        AtomicInteger reservationsRefreshCount = new AtomicInteger();
        when(appointmentBookingService.getManagedReservations())
                .thenAnswer(invocation -> reservationsRefreshCount.getAndIncrement() == 0
                        ? Collections.singletonList(reservation)
                        : Collections.singletonList(modifiedReservation));
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(replacementSlot));
        when(appointmentBookingService.modifyAppointment("res-1", replacementSelection)).thenReturn(BookingStatus.SUCCESS);

        AdminReservationsPanel panel = new AdminReservationsPanel(appointmentBookingService, appointmentService);
        JTable table = getPrivateField(panel, "reservationsTable", JTable.class);
        DefaultTableModel model = getPrivateField(panel, "tableModel", DefaultTableModel.class);
        JComboBox<?> slotComboBox = getPrivateField(panel, "slotComboBox", JComboBox.class);
        AbstractButton modifyButton = getPrivateField(panel, "modifyButton", AbstractButton.class);

        runOnEdt(() -> {
            table.setRowSelectionInterval(0, 0);
            slotComboBox.setSelectedItem(replacementSelection);
        });

        // Act
        clickButton(modifyButton);

        // Assert
        verify(appointmentBookingService).modifyAppointment("res-1", replacementSelection);
        assertEquals(1, model.getRowCount());
        assertEquals("11:00", model.getValueAt(0, 4));
        assertEquals(AppointmentStatus.RESCHEDULED, model.getValueAt(0, 7));
    }

    @Test
    void testMarkAttendedAndCompleted_updatesStatus() {
        Appointment reservation = new Appointment(
                "res-2",
                "customer@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
        Appointment attendedReservation = new Appointment(
                "res-2",
                "customer@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                2,
                AppointmentStatus.ATTENDED
        );
        Appointment completedReservation = new Appointment(
                "res-2",
                "customer@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                2,
                AppointmentStatus.COMPLETED
        );
        when(appointmentBookingService.canCurrentUserManageReservations()).thenReturn(true);
        AtomicInteger reservationsRefreshCount = new AtomicInteger();
        when(appointmentBookingService.getManagedReservations())
                .thenAnswer(invocation -> {
                    int call = reservationsRefreshCount.getAndIncrement();
                    if (call == 0) {
                        return Collections.singletonList(reservation);
                    }
                    if (call == 1) {
                        return Collections.singletonList(attendedReservation);
                    }
                    return Collections.singletonList(completedReservation);
                });
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("11:00")));
        when(appointmentBookingService.markAppointmentAsAttended("res-2")).thenReturn(BookingStatus.SUCCESS);
        when(appointmentBookingService.markAppointmentAsCompleted("res-2")).thenReturn(BookingStatus.SUCCESS);

        AdminReservationsPanel panel = new AdminReservationsPanel(appointmentBookingService, appointmentService);
        JTable table = getPrivateField(panel, "reservationsTable", JTable.class);
        DefaultTableModel model = getPrivateField(panel, "tableModel", DefaultTableModel.class);
        AbstractButton attendedButton = getPrivateField(panel, "attendedButton", AbstractButton.class);
        AbstractButton completedButton = getPrivateField(panel, "completedButton", AbstractButton.class);

        runOnEdt(() -> table.setRowSelectionInterval(0, 0));
        clickButton(attendedButton);
        assertEquals(AppointmentStatus.ATTENDED, model.getValueAt(0, 7));

        clickButton(completedButton);
        assertEquals(AppointmentStatus.COMPLETED, model.getValueAt(0, 7));
        verify(appointmentBookingService).markAppointmentAsAttended("res-2");
        verify(appointmentBookingService).markAppointmentAsCompleted("res-2");
    }
}

