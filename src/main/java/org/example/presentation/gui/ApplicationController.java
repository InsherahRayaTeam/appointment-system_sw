package org.example.presentation.gui;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.SessionManager;

import javax.swing.JFrame;
import java.util.Objects;

/**
 * Controls GUI startup and navigation between application screens.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ApplicationController {

    private final AdminAuthService authService;
    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;
    private final SessionManager sessionManager;

    private JFrame currentFrame;

    public ApplicationController(
            AdminAuthService authService,
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService,
            SessionManager sessionManager
    ) {
        this.authService = Objects.requireNonNull(authService, "authService cannot be null");
        this.appointmentService = Objects.requireNonNull(
                appointmentService,
                "appointmentService cannot be null"
        );
        this.appointmentBookingService = Objects.requireNonNull(
                appointmentBookingService,
                "appointmentBookingService cannot be null"
        );
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
    }

    /**
     * Starts the GUI application by showing the login screen.
     */
    public void start() {
        openLoginFrame();
    }

    /**
     * Opens the login screen.
     */
    public void openLoginFrame() {
        switchFrame(new LoginFrame(authService, this));
    }

    /**
     * Handles successful authentication by creating a user session and opening
     * the correct dashboard for the authenticated role.
     *
     * @param result successful authentication result
     */
    public void handleSuccessfulLogin(AuthenticationAttemptResult result) {
        if (result == null || !result.isSuccess() || result.getAuthenticatedUser() == null) {
            throw new IllegalArgumentException("Successful authentication result is required");
        }

        sessionManager.login(result.getAuthenticatedUser());
        openDashboard(result);
    }

    /**
     * Opens the correct dashboard based on authenticated user role.
     *
     * @param result authentication result
     */
    public void openDashboard(AuthenticationAttemptResult result) {
        if (result == null || !result.isSuccess()) {
            throw new IllegalArgumentException("Successful authentication result is required");
        }

        SystemUser user = result.getAuthenticatedUser();
        if (user == null) {
            throw new IllegalStateException("Authenticated user is missing");
        }

        if (user.getRole() == UserRole.ADMIN) {
            switchFrame(new AdminDashboardFrame(user, this));
        } else {
            switchFrame(new UserDashboardFrame(user, this));
        }
    }

    /**
     * Logs out the active session and returns to the login screen.
     */
    public void logoutAndOpenLogin() {
        sessionManager.logoutAndNotify();
        openLoginFrame();
    }

    /**
     * Returns appointment service if needed later by panels.
     *
     * @return appointment service
     */
    public AppointmentService getAppointmentService() {
        return appointmentService;
    }

    /**
     * Returns booking service if needed later by panels.
     *
     * @return booking service
     */
    public AppointmentBookingService getAppointmentBookingService() {
        return appointmentBookingService;
    }

    private void switchFrame(JFrame newFrame) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }

        currentFrame = newFrame;
        currentFrame.setLocationRelativeTo(null);
        currentFrame.setVisible(true);
    }
}