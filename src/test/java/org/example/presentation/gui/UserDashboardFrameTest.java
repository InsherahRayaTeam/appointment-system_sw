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
import javax.swing.JButton;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDashboardFrameTest extends GuiTestSupport {

    @Mock
    private ApplicationController appController;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentBookingService appointmentBookingService;

    private SystemUser user;
    private UserDashboardFrame frame;

    @BeforeEach
    void setUp() {
        user = new SystemUser("user-1", "user@example.com", "secret", UserRole.USER);
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
        when(appointmentBookingService.getReservationsForCustomer("user@example.com")).thenReturn(Collections.emptyList());

        // Act
        frame = new UserDashboardFrame(user, appController);

        // Assert
        SlotsPanel slotsPanel = getPrivateField(frame, "slotsPanel", SlotsPanel.class);
        BookingPanel bookingPanel = getPrivateField(frame, "bookingPanel", BookingPanel.class);
        ReservationsPanel reservationsPanel = getPrivateField(frame, "reservationsPanel", ReservationsPanel.class);

        assertTrue(slotsPanel.isVisible());
        assertFalse(bookingPanel.isVisible());
        assertFalse(reservationsPanel.isVisible());
    }

    @Test
    void testBookAppointmentButton_switchesToBookingPanel() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("10:00")));
        when(appointmentBookingService.getReservationsForCustomer("user@example.com")).thenReturn(Collections.emptyList());

        frame = new UserDashboardFrame(user, appController);
        AbstractButton bookButton = findButton(frame.getContentPane(), "Book Appointment");

        // Act
        clickButton(bookButton);

        // Assert
        SlotsPanel slotsPanel = getPrivateField(frame, "slotsPanel", SlotsPanel.class);
        BookingPanel bookingPanel = getPrivateField(frame, "bookingPanel", BookingPanel.class);
        ReservationsPanel reservationsPanel = getPrivateField(frame, "reservationsPanel", ReservationsPanel.class);

        assertFalse(slotsPanel.isVisible());
        assertTrue(bookingPanel.isVisible());
        assertFalse(reservationsPanel.isVisible());
    }

    @Test
    void testLogout_redirectsToLogin() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(Collections.singletonList(new AppointmentSlot("10:00")));
        when(appointmentBookingService.getReservationsForCustomer("user@example.com")).thenReturn(Collections.emptyList());

        frame = new UserDashboardFrame(user, appController);
        AbstractButton logoutButton = findButton(frame.getContentPane(), "Logout");

        // Act
        clickButton(logoutButton);

        // Assert
        verify(appController).logoutAndOpenLogin();
    }
}

