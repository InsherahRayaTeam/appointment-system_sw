package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.notification.MockNotificationService;
import org.example.notification.NotificationService;
import org.example.repository.InMemoryUserRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    private static final String ALICE_ID = "alice";
    private static final String ALICE_EMAIL = "alice@example.com";
    private static final String MISSING_EMAIL = "missing@example.com";
    private static final String RESET_CODE = "654321";
    private static final String NEW_PASSWORD = "new1234";
    private static final String OTHER_PASSWORD = "other1234";
    private static final String PASSWORD_RESET_SUBJECT = "Password Reset Request";

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EventManager eventManager;

    private PasswordRecoveryService passwordRecoveryService;

    @BeforeEach
    void setUp() {
        passwordRecoveryService = new PasswordRecoveryService(
                userRepository,
                notificationService,
                eventManager,
                () -> RESET_CODE
        );
    }

    @Test
    void requestReset_ExistingUserByEmail_ReturnsResetRequestedAndSendsNotification() {
        SystemUser user = new SystemUser(ALICE_ID, ALICE_EMAIL, "pass123", UserRole.USER);
        when(userRepository.findByEmail(ALICE_EMAIL)).thenReturn(Optional.of(user));

        ForgotPasswordStatus status = passwordRecoveryService.requestReset(ALICE_EMAIL);

        assertEquals(ForgotPasswordStatus.RESET_REQUESTED, status);
        verify(notificationService).send(
                ALICE_EMAIL,
                PASSWORD_RESET_SUBJECT,
                "Use this reset code to update your password: " + RESET_CODE
                        + " . If you did not request this, ignore this email."
        );
    }

    @Test
    void requestReset_UnknownUser_ReturnsUnknownUser() {
        when(userRepository.findByEmail(MISSING_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.findById(MISSING_EMAIL)).thenReturn(Optional.empty());

        ForgotPasswordStatus status = passwordRecoveryService.requestReset(MISSING_EMAIL);

        assertEquals(ForgotPasswordStatus.UNKNOWN_USER, status);
    }

    @Test
    void resetPassword_ValidRequest_UpdatesPassword() {
        SystemUser user = new SystemUser(ALICE_ID, ALICE_EMAIL, "pass123", UserRole.USER);
        when(userRepository.findByEmail(ALICE_EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.update(any(SystemUser.class))).thenReturn(true);

        ForgotPasswordStatus requestStatus = passwordRecoveryService.requestReset(ALICE_EMAIL);
        ForgotPasswordStatus resetStatus = passwordRecoveryService.resetPassword(
                ALICE_EMAIL,
                RESET_CODE,
                NEW_PASSWORD,
                NEW_PASSWORD
        );

        assertEquals(ForgotPasswordStatus.RESET_REQUESTED, requestStatus);
        assertEquals(ForgotPasswordStatus.PASSWORD_RESET_SUCCESS, resetStatus);
        verify(userRepository).update(org.mockito.ArgumentMatchers.any(SystemUser.class));
    }

    @Test
    void resetPassword_InvalidCode_ReturnsInvalidResetCode() {
        SystemUser user = new SystemUser(ALICE_ID, ALICE_EMAIL, "pass123", UserRole.USER);
        when(userRepository.findByEmail(ALICE_EMAIL)).thenReturn(Optional.of(user));

        passwordRecoveryService.requestReset(ALICE_EMAIL);
        ForgotPasswordStatus resetStatus = passwordRecoveryService.resetPassword(
                ALICE_EMAIL,
                "000000",
                NEW_PASSWORD,
                NEW_PASSWORD
        );

        assertEquals(ForgotPasswordStatus.INVALID_RESET_CODE, resetStatus);
    }

    @Test
    void resetPassword_MismatchedPassword_ReturnsPasswordMismatch() {
        SystemUser user = new SystemUser(ALICE_ID, ALICE_EMAIL, "pass123", UserRole.USER);
        when(userRepository.findByEmail(ALICE_EMAIL)).thenReturn(Optional.of(user));

        passwordRecoveryService.requestReset(ALICE_EMAIL);
        ForgotPasswordStatus resetStatus = passwordRecoveryService.resetPassword(
                ALICE_EMAIL,
                RESET_CODE,
                NEW_PASSWORD,
                OTHER_PASSWORD
        );

        assertEquals(ForgotPasswordStatus.PASSWORD_MISMATCH, resetStatus);
    }

    @Test
    void requestReset_WithUsernameLookup_Succeeds() {
        when(userRepository.findByEmail(ALICE_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(ALICE_ID)).thenReturn(
                Optional.of(new SystemUser(ALICE_ID, ALICE_EMAIL, "pass123", UserRole.USER))
        );

        ForgotPasswordStatus status = passwordRecoveryService.requestReset(ALICE_ID);

        assertEquals(ForgotPasswordStatus.RESET_REQUESTED, status);
        verify(notificationService).send(
                org.mockito.ArgumentMatchers.eq(ALICE_EMAIL),
                org.mockito.ArgumentMatchers.eq(PASSWORD_RESET_SUBJECT),
                contains(RESET_CODE)
        );
    }

    @Test
    void resetPassword_DirectFlow_UpdatesStoredPassword() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        EventManager localEventManager = new EventManager();
        PasswordRecoveryService recoveryService = new PasswordRecoveryService(
                repository,
                new MockNotificationService(),
                localEventManager,
                () -> RESET_CODE
        );

        UserRegistrationService registrationService = new UserRegistrationService(repository, localEventManager);
        SignUpStatus signUpStatus = registrationService.signUp("recover@example.com", "start123");
        ForgotPasswordStatus resetStatus = recoveryService.resetPassword("recover@example.com", "new1234");

        assertEquals(SignUpStatus.SUCCESS, signUpStatus);
        assertEquals(ForgotPasswordStatus.PASSWORD_RESET_SUCCESS, resetStatus);
        assertEquals("new1234", repository.findByEmail("recover@example.com").orElseThrow().getPassword());
    }

    @Test
    void login_AfterReset_UsesNewPasswordAndRejectsOldPassword() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        EventManager localEventManager = new EventManager();
        UserRegistrationService registrationService = new UserRegistrationService(repository, localEventManager);
        PasswordRecoveryService recoveryService = new PasswordRecoveryService(
                repository,
                new MockNotificationService(),
                localEventManager,
                () -> RESET_CODE
        );
        AdminAuthService authService = new AdminAuthService(
                repository,
                localEventManager,
                new LoginAttemptTracker(3, Duration.ofSeconds(30))
        );

        SignUpStatus signUpStatus = registrationService.signUp("authreset@example.com", "old1234");
        ForgotPasswordStatus resetStatus = recoveryService.resetPassword("authreset@example.com", "new1234");

        assertEquals(SignUpStatus.SUCCESS, signUpStatus);
        assertEquals(ForgotPasswordStatus.PASSWORD_RESET_SUCCESS, resetStatus);
        assertEquals(LoginStatus.INVALID_CREDENTIALS, authService.authenticateWithStatus("authreset@example.com", "old1234"));
        assertEquals(LoginStatus.SUCCESS, authService.authenticateWithStatus("authreset@example.com", "new1234"));
        assertTrue(repository.findByEmail("authreset@example.com").isPresent());
    }
}

