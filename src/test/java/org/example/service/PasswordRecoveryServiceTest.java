package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.notification.NotificationService;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

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
                () -> "654321"
        );
    }

    @Test
    void requestReset_ExistingUserByEmail_ReturnsResetRequestedAndSendsNotification() {
        SystemUser user = new SystemUser("alice", "alice@example.com", "pass123", UserRole.USER);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        ForgotPasswordStatus status = passwordRecoveryService.requestReset("alice@example.com");

        assertEquals(ForgotPasswordStatus.RESET_REQUESTED, status);
        verify(notificationService).send(
                "alice@example.com",
                "Password Reset Request",
                "Use this reset code to update your password: 654321 . If you did not request this, ignore this email."
        );
    }

    @Test
    void requestReset_UnknownUser_ReturnsUnknownUser() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        when(userRepository.findById("missing@example.com")).thenReturn(Optional.empty());

        ForgotPasswordStatus status = passwordRecoveryService.requestReset("missing@example.com");

        assertEquals(ForgotPasswordStatus.UNKNOWN_USER, status);
    }

    @Test
    void resetPassword_ValidRequest_UpdatesPassword() {
        SystemUser user = new SystemUser("alice", "alice@example.com", "pass123", UserRole.USER);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(userRepository.updatePassword("alice", "new1234")).thenReturn(true);

        ForgotPasswordStatus requestStatus = passwordRecoveryService.requestReset("alice@example.com");
        ForgotPasswordStatus resetStatus = passwordRecoveryService.resetPassword(
                "alice@example.com",
                "654321",
                "new1234",
                "new1234"
        );

        assertEquals(ForgotPasswordStatus.RESET_REQUESTED, requestStatus);
        assertEquals(ForgotPasswordStatus.PASSWORD_RESET_SUCCESS, resetStatus);
        verify(userRepository).updatePassword("alice", "new1234");
    }

    @Test
    void resetPassword_InvalidCode_ReturnsInvalidResetCode() {
        SystemUser user = new SystemUser("alice", "alice@example.com", "pass123", UserRole.USER);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        passwordRecoveryService.requestReset("alice@example.com");
        ForgotPasswordStatus resetStatus = passwordRecoveryService.resetPassword(
                "alice@example.com",
                "000000",
                "new1234",
                "new1234"
        );

        assertEquals(ForgotPasswordStatus.INVALID_RESET_CODE, resetStatus);
    }

    @Test
    void resetPassword_MismatchedPassword_ReturnsPasswordMismatch() {
        SystemUser user = new SystemUser("alice", "alice@example.com", "pass123", UserRole.USER);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        passwordRecoveryService.requestReset("alice@example.com");
        ForgotPasswordStatus resetStatus = passwordRecoveryService.resetPassword(
                "alice@example.com",
                "654321",
                "new1234",
                "other1234"
        );

        assertEquals(ForgotPasswordStatus.PASSWORD_MISMATCH, resetStatus);
    }

    @Test
    void requestReset_WithUsernameLookup_Succeeds() {
        when(userRepository.findByEmail("alice")).thenReturn(Optional.empty());
        when(userRepository.findById("alice")).thenReturn(
                Optional.of(new SystemUser("alice", "alice@example.com", "pass123", UserRole.USER))
        );

        ForgotPasswordStatus status = passwordRecoveryService.requestReset("alice");

        assertEquals(ForgotPasswordStatus.RESET_REQUESTED, status);
        verify(notificationService).send(
                org.mockito.ArgumentMatchers.eq("alice@example.com"),
                org.mockito.ArgumentMatchers.eq("Password Reset Request"),
                contains("654321")
        );
    }
}

