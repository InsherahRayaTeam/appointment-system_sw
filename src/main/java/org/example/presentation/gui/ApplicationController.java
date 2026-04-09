package org.example.presentation.gui;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.ForgotPasswordStatus;
import org.example.service.SessionManager;
import org.example.service.SignUpStatus;
import org.example.service.PasswordRecoveryService;
import org.example.service.UserRegistrationService;

import javax.swing.JFrame;
import java.util.Objects;

/**
 * Represents application controller in the system.
 */
public class ApplicationController {

    private final AdminAuthService authService;
    private final AppointmentService appointmentService;
    private final AppointmentBookingService appointmentBookingService;
    private final SessionManager sessionManager;
    private final UserRegistrationService userRegistrationService;
    private final PasswordRecoveryService passwordRecoveryService;

    private JFrame currentFrame;

    /**
     * Creates a new application controller object with the given values.
     *
     * @param authService service used to run business logic
     * @param appointmentService service used to run business logic
     * @param appointmentBookingService service used to run business logic
     * @param sessionManager manager object used for shared app state
     */
    public ApplicationController(
            AdminAuthService authService,
            AppointmentService appointmentService,
            AppointmentBookingService appointmentBookingService,
            SessionManager sessionManager,
            UserRegistrationService userRegistrationService,
            PasswordRecoveryService passwordRecoveryService
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
        this.userRegistrationService = Objects.requireNonNull(
                userRegistrationService,
                "userRegistrationService cannot be null"
        );
        this.passwordRecoveryService = Objects.requireNonNull(
                passwordRecoveryService,
                "passwordRecoveryService cannot be null"
        );
    }

    /**
     * Starts this part of the application.
     */
    public void start() {
        openLoginFrame();
    }

    /**
     * Opens login frame in the user interface.
     */
    public void openLoginFrame() {
        switchFrame(new LoginFrame(authService, this));
    }

    /**
     * Opens sign up frame in the user interface.
     */
    public void openSignUpFrame() {
        switchFrame(new org.example.presentation.gui.SignUpFrame(this));
    }

    /**
     * Opens forgot password frame in the user interface.
     */
    public void openForgotPasswordFrame() {
        switchFrame(new org.example.presentation.gui.ForgotPasswordFrame(this));
    }

    /**
     * Registers a user through registration service.
     *
     * @param username unique username for login identity
     * @param email email address used for login or matching
     * @param password password text entered by the user
     * @param confirmPassword password confirmation text entered by the user
     * @return status that explains the operation result
     */
    public SignUpStatus registerUser(String username, String email, String password, String confirmPassword) {
        return userRegistrationService.registerUser(username, email, password, confirmPassword);
    }

    /**
     * Requests a password reset code for a user.
     *
     * @param usernameOrEmail username or email used for identity
     * @return status that explains the operation result
     */
    public ForgotPasswordStatus requestPasswordReset(String usernameOrEmail) {
        return passwordRecoveryService.requestReset(usernameOrEmail);
    }

    /**
     * Resets password after validating reset data.
     *
     * @param usernameOrEmail username or email used for identity
     * @param resetCode reset verification code
     * @param newPassword password text entered by the user
     * @param confirmPassword password confirmation text entered by the user
     * @return status that explains the operation result
     */
    public ForgotPasswordStatus resetPassword(
            String usernameOrEmail,
            String resetCode,
            String newPassword,
            String confirmPassword
    ) {
        return passwordRecoveryService.resetPassword(usernameOrEmail, resetCode, newPassword, confirmPassword);
    }

    /**
     * Runs handle successful login for this class.
     *
     * @param result value for result
     */
    public void handleSuccessfulLogin(AuthenticationAttemptResult result) {
        if (result == null || !result.isSuccess() || result.getAuthenticatedUser() == null) {
            throw new IllegalArgumentException("Successful authentication result is required");
        }

        sessionManager.login(result.getAuthenticatedUser());
        openDashboard(result);
    }

    /**
     * Opens dashboard in the user interface.
     *
     * @param result value for result
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
     * Runs logout and open login for this class.
     */
    public void logoutAndOpenLogin() {
        sessionManager.logoutAndNotify();
        openLoginFrame();
    }

    /**
     * Returns the appointment service.
     *
     * @return requested value from this object
     */
    public AppointmentService getAppointmentService() {
        return appointmentService;
    }

    /**
     * Returns the appointment booking service.
     *
     * @return requested value from this object
     */
    public AppointmentBookingService getAppointmentBookingService() {
        return appointmentBookingService;
    }

    /**
     * Runs switch frame for this class.
     *
     * @param newFrame window frame used by the screen
     */
    private void switchFrame(JFrame newFrame) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }

        currentFrame = newFrame;
        currentFrame.setLocationRelativeTo(null);
        currentFrame.setVisible(true);
    }
}
