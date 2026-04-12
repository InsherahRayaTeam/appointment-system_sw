package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.service.AppointmentCalendarExportService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;
import org.example.service.GoogleCalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationsPanelTest extends GuiTestSupport {

    @Mock
    private AppointmentBookingService appointmentBookingService;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentCalendarExportService calendarExportService;

    @Mock
    private GoogleCalendarService googleCalendarService;

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
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("11:00")));
        when(appointmentBookingService.cancelOwnAppointment("res-1")).thenReturn(BookingStatus.SUCCESS);

        ReservationsPanel panel = new ReservationsPanel(
                user,
                appointmentService,
                appointmentBookingService,
                calendarExportService,
                googleCalendarService
        );
        JTable table = Objects.requireNonNull(getPrivateField(panel, "reservationsTable", JTable.class));
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
    void testModifyReservation_usesOwnReservationApi() {
        Appointment reservation = new Appointment(
                "res-7",
                "user@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
        AppointmentSlot replacementSlot = new AppointmentSlot("11:00");
        String replacementSelection = replacementSlot.getDateDayTimeLabel();
        when(appointmentBookingService.getReservationsForCustomer("user@example.com"))
                .thenReturn(Collections.singletonList(reservation));
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(replacementSlot));
        when(appointmentBookingService.modifyOwnAppointment("res-7", replacementSelection)).thenReturn(BookingStatus.SUCCESS);

        ReservationsPanel panel = new ReservationsPanel(
                user,
                appointmentService,
                appointmentBookingService,
                calendarExportService,
                googleCalendarService
        );
        JTable table = Objects.requireNonNull(getPrivateField(panel, "reservationsTable", JTable.class));
        JComboBox<?> slotComboBox = getPrivateField(panel, "slotComboBox", JComboBox.class);
        AbstractButton modifyButton = findButton(panel, "Modify Reservation");

        runOnEdt(() -> {
            table.setRowSelectionInterval(0, 0);
            slotComboBox.setSelectedItem(replacementSelection);
        });

        clickButton(modifyButton);

        verify(appointmentBookingService).modifyOwnAppointment("res-7", replacementSelection);
    }

    @Test
    void testCancelReservation_withoutSelection_showsWarning() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.emptyList());
        when(appointmentBookingService.getReservationsForCustomer("user@example.com")).thenReturn(Collections.emptyList());

        ReservationsPanel panel = new ReservationsPanel(
                user,
                appointmentService,
                appointmentBookingService,
                calendarExportService,
                googleCalendarService
        );
        AbstractButton cancelButton = findButton(panel, "Cancel Reservation");

        // Act
        clickButton(cancelButton);

        // Assert
        verify(appointmentBookingService, never()).cancelOwnAppointment(anyString());
    }

    @Test
    void addToCalendar_enabledForConfirmedReservationAndCallsExportService() throws IOException {
        Appointment reservation = new Appointment(
                "res-cal-1",
                "user@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                1,
                AppointmentStatus.CONFIRMED
        );
        when(appointmentBookingService.getReservationsForCustomer("user@example.com"))
                .thenReturn(Collections.singletonList(reservation));
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("11:00")));
        when(calendarExportService.generateCalendarFile(reservation)).thenReturn(new File("calendar.ics"));

        ReservationsPanel panel = new ReservationsPanel(
                user,
                appointmentService,
                appointmentBookingService,
                calendarExportService,
                googleCalendarService
        );
        JTable table = Objects.requireNonNull(getPrivateField(panel, "reservationsTable", JTable.class));
        AbstractButton addToCalendarButton = Objects.requireNonNull(findButton(panel, "Add to Calendar"));

        runOnEdt(() -> table.setRowSelectionInterval(0, 0));
        assertTrue(addToCalendarButton.isEnabled());

        clickButton(addToCalendarButton);

        verify(calendarExportService).generateCalendarFile(reservation);
    }

    @Test
    void addToCalendar_disabledForCancelledReservation() {
        Appointment cancelled = new Appointment(
                "res-cal-2",
                "user@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                1,
                AppointmentStatus.CANCELLED
        );
        when(appointmentBookingService.getReservationsForCustomer("user@example.com"))
                .thenReturn(Collections.singletonList(cancelled));
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("11:00")));

        ReservationsPanel panel = new ReservationsPanel(
                user,
                appointmentService,
                appointmentBookingService,
                calendarExportService,
                googleCalendarService
        );
        JTable table = Objects.requireNonNull(getPrivateField(panel, "reservationsTable", JTable.class));
        AbstractButton addToCalendarButton = Objects.requireNonNull(findButton(panel, "Add to Calendar"));

        runOnEdt(() -> table.setRowSelectionInterval(0, 0));

        assertFalse(addToCalendarButton.isEnabled());
    }

    @Test
    void addToGoogleCalendar_callsServiceForSelectedReservation() {
        Appointment reservation = new Appointment(
                "res-gcal-1",
                "user@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                1,
                AppointmentStatus.CONFIRMED
        );
        when(appointmentBookingService.getReservationsForCustomer("user@example.com"))
                .thenReturn(Collections.singletonList(reservation));
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("11:00")));

        ReservationsPanel panel = new ReservationsPanel(
                user,
                appointmentService,
                appointmentBookingService,
                calendarExportService,
                googleCalendarService
        );
        JTable table = Objects.requireNonNull(getPrivateField(panel, "reservationsTable", JTable.class));
        AbstractButton addToGoogleCalendarButton = Objects.requireNonNull(findButton(panel, "Add to Google Calendar"));

        runOnEdt(() -> table.setRowSelectionInterval(0, 0));
        assertTrue(addToGoogleCalendarButton.isEnabled());

        clickButton(addToGoogleCalendarButton);

        verify(googleCalendarService).openGoogleCalendarEvent(reservation);
    }

    @Test
    void addToGoogleCalendar_withoutSelection_doesNotCallService() {
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.emptyList());
        when(appointmentBookingService.getReservationsForCustomer("user@example.com")).thenReturn(Collections.emptyList());

        ReservationsPanel panel = new ReservationsPanel(
                user,
                appointmentService,
                appointmentBookingService,
                calendarExportService,
                googleCalendarService
        );
        AbstractButton addToGoogleCalendarButton = findButton(panel, "Add to Google Calendar");

        clickButton(addToGoogleCalendarButton);

        verify(googleCalendarService, never()).openGoogleCalendarEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void leaveFeedbackButton_isVisibleOnlyForCompletedReservation() {
        Appointment completed = new Appointment(
                "res-feedback-1",
                "user@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                1,
                AppointmentStatus.COMPLETED
        );
        when(appointmentBookingService.getReservationsForCustomer("user@example.com"))
                .thenReturn(Collections.singletonList(completed));
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("11:00")));

        ReservationsPanel panel = new ReservationsPanel(
                user,
                appointmentService,
                appointmentBookingService,
                calendarExportService,
                googleCalendarService
        );
        JTable table = getPrivateField(panel, "reservationsTable", JTable.class);
        AbstractButton feedbackButton = Objects.requireNonNull(findButton(panel, "Leave Feedback"));

        runOnEdt(() -> table.setRowSelectionInterval(0, 0));

        assertTrue(feedbackButton.isVisible());
        assertTrue(feedbackButton.isEnabled());
    }

    @Test
    void leaveFeedbackButton_isHiddenForNonCompletedReservation() {
        Appointment confirmed = new Appointment(
                "res-feedback-2",
                "user@example.com",
                LocalDateTime.now().plusDays(1),
                60,
                1,
                AppointmentStatus.CONFIRMED
        );
        when(appointmentBookingService.getReservationsForCustomer("user@example.com"))
                .thenReturn(Collections.singletonList(confirmed));
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("11:00")));

        ReservationsPanel panel = new ReservationsPanel(
                user,
                appointmentService,
                appointmentBookingService,
                calendarExportService,
                googleCalendarService
        );
        JTable table = getPrivateField(panel, "reservationsTable", JTable.class);
        AbstractButton feedbackButton = Objects.requireNonNull(findButton(panel, "Leave Feedback"));

        runOnEdt(() -> table.setRowSelectionInterval(0, 0));

        assertFalse(feedbackButton.isVisible());
    }

}

