package org.example.presentation.gui;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.service.AdminAuthService;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.AuthenticationAttemptResult;
import org.example.service.ForgotPasswordStatus;
import org.example.service.PasswordRecoveryService;
import org.example.service.SessionManager;
import org.example.service.SignUpStatus;
import org.example.service.UserRegistrationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.JFrame;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest extends GuiTestSupport {

    @Mock
    private AdminAuthService authService;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentBookingService appointmentBookingService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private UserRegistrationService userRegistrationService;

    @Mock
    private PasswordRecoveryService passwordRecoveryService;

    private ApplicationController controller;

    @BeforeEach
    void setUp() {
        controller = new ApplicationController(
                authService,
                appointmentService,
                appointmentBookingService,
                sessionManager,
                userRegistrationService,
                passwordRecoveryService
        );
    }

    @AfterEach
    void tearDown() {
        disposeIfWindow(getPrivateField(controller, "currentFrame", JFrame.class));
    }

    @Test
    void testStartApplication_showsLoginFrame() {
        // Arrange / Act
        controller.start();

        // Assert
        JFrame currentFrame = getPrivateField(controller, "currentFrame", JFrame.class);
        assertInstanceOf(LoginFrame.class, currentFrame);
        assertTrue(currentFrame.isVisible());
    }

    @Test
    void testLoginSuccess_opensCorrectDashboard() {
        // Arrange
        SystemUser adminUser = new SystemUser("admin-1", "admin@example.com", "secret", UserRole.ADMIN);
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success(adminUser);

        // Act
        controller.handleSuccessfulLogin(result);

        // Assert
        verify(sessionManager).login(adminUser);
        JFrame currentFrame = getPrivateField(controller, "currentFrame", JFrame.class);
        assertInstanceOf(AdminDashboardFrame.class, currentFrame);
        assertTrue(currentFrame.isVisible());
    }

    @Test
    void testLoginSuccess_opensUserDashboard() {
        // Arrange
        SystemUser regularUser = new SystemUser("user-1", "user@example.com", "secret", UserRole.USER);
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success(regularUser);

        // Act
        controller.handleSuccessfulLogin(result);

        // Assert
        verify(sessionManager).login(regularUser);
        JFrame currentFrame = getPrivateField(controller, "currentFrame", JFrame.class);
        assertInstanceOf(UserDashboardFrame.class, currentFrame);
        assertTrue(currentFrame.isVisible());
    }

    @Test
    void testLoginFailure_invalidResult_throwsException() {
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> controller.handleSuccessfulLogin(AuthenticationAttemptResult.failure(
                org.example.service.LoginStatus.INVALID_CREDENTIALS,
                2
        )));
    }

    @Test
    void testLogout_redirectsToLogin() {
        // Arrange
        when(appointmentService.getAvailableSlots()).thenReturn(java.util.Collections.emptyList());
        when(appointmentBookingService.canCurrentUserManageReservations()).thenReturn(true);
        when(appointmentBookingService.getManagedReservations()).thenReturn(java.util.Collections.emptyList());
        controller.openDashboard(AuthenticationAttemptResult.success(
                new SystemUser("admin-1", "admin@example.com", "secret", UserRole.ADMIN)
        ));

        // Act
        controller.logoutAndOpenLogin();

        // Assert
        verify(sessionManager).logoutAndNotify();
        JFrame currentFrame = getPrivateField(controller, "currentFrame", JFrame.class);
        assertInstanceOf(LoginFrame.class, currentFrame);
        assertTrue(currentFrame.isVisible());
    }

    @Test
    void openSignUpFrame_displaysSignUpFrame() {
        controller.openSignUpFrame();

        JFrame currentFrame = getPrivateField(controller, "currentFrame", JFrame.class);
        assertInstanceOf(SignUpFrame.class, currentFrame);
        assertTrue(currentFrame.isVisible());
    }

    @Test
    void openForgotPasswordFrame_displaysForgotPasswordFrame() {
        controller.openForgotPasswordFrame();

        JFrame currentFrame = getPrivateField(controller, "currentFrame", JFrame.class);
        assertInstanceOf(ForgotPasswordFrame.class, currentFrame);
        assertTrue(currentFrame.isVisible());
    }

    @Test
    void registerUser_delegatesToRegistrationService() {
        when(userRegistrationService.registerUser("alice", "alice@example.com", "pass123", "pass123"))
                .thenReturn(SignUpStatus.SUCCESS);

        SignUpStatus result = controller.registerUser("alice", "alice@example.com", "pass123", "pass123");

        assertSame(SignUpStatus.SUCCESS, result);
        verify(userRegistrationService).registerUser("alice", "alice@example.com", "pass123", "pass123");
    }

    @Test
    void forgotPasswordFlows_delegateToRecoveryService() {
        when(passwordRecoveryService.requestReset("alice@example.com")).thenReturn(ForgotPasswordStatus.RESET_REQUESTED);
        when(passwordRecoveryService.resetPassword("alice@example.com", "123456", "new123", "new123"))
                .thenReturn(ForgotPasswordStatus.PASSWORD_RESET_SUCCESS);

        ForgotPasswordStatus requestStatus = controller.requestPasswordReset("alice@example.com");
        ForgotPasswordStatus resetStatus = controller.resetPassword("alice@example.com", "123456", "new123", "new123");

        assertSame(ForgotPasswordStatus.RESET_REQUESTED, requestStatus);
        assertSame(ForgotPasswordStatus.PASSWORD_RESET_SUCCESS, resetStatus);
        verify(passwordRecoveryService).requestReset("alice@example.com");
        verify(passwordRecoveryService).resetPassword("alice@example.com", "123456", "new123", "new123");
    }
}

