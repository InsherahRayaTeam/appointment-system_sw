package org.example.presentation.gui;

import org.example.service.AdminAuthService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.SessionManager;

/**
 * Controls application startup and navigation.
 */
public class ApplicationController {

    private final AdminAuthService authService;
    private final AppointmentService appointmentService;
    private final AppointmentBookingService bookingService;
    private final SessionManager sessionManager;

    public ApplicationController(
            AdminAuthService authService,
            AppointmentService appointmentService,
            AppointmentBookingService bookingService,
            SessionManager sessionManager
    ) {
        this.authService = authService;
        this.appointmentService = appointmentService;
        this.bookingService = bookingService;
        this.sessionManager = sessionManager;
    }

    public void start() {
        // أول شاشة رح نعرضها
        LoginFrame loginFrame = new LoginFrame(authService, sessionManager, this);
        loginFrame.setVisible(true);
    }

    public void openDashboard() {
        // رح نضيفها بعدين حسب role
        System.out.println("Open Dashboard...");
    }
}