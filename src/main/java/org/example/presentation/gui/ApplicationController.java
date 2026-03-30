package org.example.presentation.gui;

import org.example.service.AdminAuthService;
import org.example.service.AppointmentService;
import org.example.service.SessionManager;

import javax.swing.*;

/**
 * Controller for managing GUI application flow.
 *
 * Coordinates transitions between login and dashboard screens,
 * manages window visibility, and delegates business operations
 * to appropriate services.
 */
public class ApplicationController {

    private final LoginFrame loginFrame;
    private final MainDashboardFrame dashboardFrame;

    public ApplicationController(
            AdminAuthService authService,
            AppointmentService appointmentService,
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
        dashboardFrame.refreshForCurrentSession();
        loginFrame.setVisible(false);
        dashboardFrame.setVisible(true);
        dashboardFrame.toFront();
    }

    private void onLoginSuccess() {
        showDashboardFrame();
    }

    private void onLogoutRequested() {
        loginFrame.reset();
        showLoginFrame();
    }

    private void onExitRequested() {
        loginFrame.dispose();
        dashboardFrame.dispose();
        System.exit(0);
    }
}
