package org.example.presentation.gui;

import org.example.service.AdminAuthService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.SessionManager;

import javax.swing.*;

/**
 * Controller for managing GUI application flow.
 *
 * Coordinates transitions between login and dashboard screens,
 * manages window visibility, and delegates business operations
 * to appropriate services.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ApplicationController {

    private final LoginFrame loginFrame;
    private final MainDashboardFrame dashboardFrame;

    /**
     * Creates application controller with GUI/service dependencies.
     *
     * @param authService authentication service
     * @param appointmentService appointment slot service
     * @param appointmentBookingService appointment booking/management service
     * @param sessionManager session manager
     */
    public ApplicationController(
            AdminAuthService authService,
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService,
            SessionManager sessionManager
    ) {
        this.loginFrame = new LoginFrame(
                authService,
                sessionManager,
                this::onLoginSuccess,
                this::onExitRequested
        );

        this.dashboardFrame = new MainDashboardFrame(
                appointmentService,
                appointmentBookingService,
                sessionManager,
                this::onLogoutRequested,
                this::onExitRequested
        );
    }

    /**
     * Starts the application by showing the login frame.
     */
    public void start() {
        SwingUtilities.invokeLater(this::showLoginFrame);
    }

    private void showLoginFrame() {
        dashboardFrame.setVisible(false);
        loginFrame.setVisible(true);
        loginFrame.toFront();
    }

    private void showDashboardFrame() {
        dashboardFrame.resetToMenu();
        dashboardFrame.refreshForCurrentSession();
        loginFrame.setVisible(false);
        dashboardFrame.setVisible(true);
        dashboardFrame.toFront();
    }

    private void onLoginSuccess() {
        showDashboardFrame();
    }

    private void onLogoutRequested() {
        dashboardFrame.resetToMenu();
        loginFrame.reset();
        showLoginFrame();
    }

    private void onExitRequested() {
        loginFrame.dispose();
        dashboardFrame.dispose();
        System.exit(0);
    }
}
