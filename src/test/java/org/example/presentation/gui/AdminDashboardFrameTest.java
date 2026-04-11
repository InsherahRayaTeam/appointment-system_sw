package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.AbstractButton;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardFrameTest extends GuiTestSupport {

    @Mock
    private ApplicationController appController;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentBookingService appointmentBookingService;

    private SystemUser adminUser;
    private AdminDashboardFrame frame;

    @BeforeEach
    void setUp() {
        adminUser = new SystemUser("admin-1", "admin@example.com", "secret", UserRole.ADMIN);
        when(appController.getAppointmentService()).thenReturn(appointmentService);
        when(appController.getAppointmentBookingService()).thenReturn(appointmentBookingService);
    }

    @AfterEach
    void tearDown() {
        disposeIfWindow(frame);
    }

    @Test
    void testInitialLoad_showsSlotsPanel() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("10:00")));
        when(appointmentBookingService.canCurrentUserManageReservations()).thenReturn(true);
        when(appointmentBookingService.getManagedReservations()).thenReturn(Collections.emptyList());

        // Act
        frame = new AdminDashboardFrame(adminUser, appController);

        // Assert
        SlotsPanel slotsPanel = getPrivateField(frame, "slotsPanel", SlotsPanel.class);
        AdminReservationsPanel reservationsPanel = getPrivateField(frame, "adminReservationsPanel", AdminReservationsPanel.class);
        assertTrue(slotsPanel.isVisible());
        assertFalse(reservationsPanel.isVisible());
    }

    @Test
    void testManageReservationsButton_switchesToReservationsPanel() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("10:00")));
        when(appointmentBookingService.canCurrentUserManageReservations()).thenReturn(true);
        when(appointmentBookingService.getManagedReservations()).thenReturn(Collections.singletonList(
                new Appointment("res-1", "customer@example.com", LocalDateTime.now().plusDays(1), 60, 2, org.example.domain.AppointmentStatus.CONFIRMED)
        ));

        frame = new AdminDashboardFrame(adminUser, appController);
        AbstractButton manageReservationsButton = findButton(frame.getContentPane(), "Manage Reservations");

        // Act
        clickButton(manageReservationsButton);

        // Assert
        SlotsPanel slotsPanel = getPrivateField(frame, "slotsPanel", SlotsPanel.class);
        AdminReservationsPanel reservationsPanel = getPrivateField(frame, "adminReservationsPanel", AdminReservationsPanel.class);
        DefaultTableModel tableModel = getPrivateField(reservationsPanel, "tableModel", DefaultTableModel.class);

        assertFalse(slotsPanel.isVisible());
        assertTrue(reservationsPanel.isVisible());
        org.junit.jupiter.api.Assertions.assertEquals(1, tableModel.getRowCount());
    }

    @Test
    void testLogout_redirectsToLogin() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("10:00")));
        when(appointmentBookingService.canCurrentUserManageReservations()).thenReturn(true);
        when(appointmentBookingService.getManagedReservations()).thenReturn(Collections.emptyList());

        frame = new AdminDashboardFrame(adminUser, appController);
        AbstractButton logoutButton = findButton(frame.getContentPane(), "Logout");

        // Act
        clickButton(logoutButton);

        // Assert
        verify(appController).logoutAndOpenLogin();
    }

    @Test
    void testCalendarViewButton_switchesToCalendarPanel() {
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("10:00")));
        when(appointmentBookingService.canCurrentUserManageReservations()).thenReturn(true);
        when(appointmentBookingService.getManagedReservations()).thenReturn(Collections.emptyList());

        frame = new AdminDashboardFrame(adminUser, appController);
        AbstractButton calendarButton = findButton(frame.getContentPane(), "Calendar View");

        clickButton(calendarButton);

        SlotsPanel slotsPanel = getPrivateField(frame, "slotsPanel", SlotsPanel.class);
        AdminReservationsPanel reservationsPanel = getPrivateField(frame, "adminReservationsPanel", AdminReservationsPanel.class);
        CalendarPanel calendarPanel = getPrivateField(frame, "calendarPanel", CalendarPanel.class);

        assertFalse(slotsPanel.isVisible());
        assertFalse(reservationsPanel.isVisible());
        assertTrue(calendarPanel.isVisible());
    }
}

