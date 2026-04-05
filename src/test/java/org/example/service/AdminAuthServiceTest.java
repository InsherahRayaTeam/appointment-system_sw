package org.example.service;

import org.example.domain.Credentials;
import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventManager eventManager;

    private AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        adminAuthService = new AdminAuthService(
                userRepository,
                eventManager,
                new LoginAttemptTracker(3, Duration.ofSeconds(30))
        );
    }

    @Test
    void constructor_WithNullDependencies_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(NullPointerException.class, () -> new AdminAuthService(null, eventManager, new LoginAttemptTracker(1, Duration.ofSeconds(1))));
        assertThrows(NullPointerException.class, () -> new AdminAuthService(userRepository, null, new LoginAttemptTracker(1, Duration.ofSeconds(1))));
        assertThrows(NullPointerException.class, () -> new AdminAuthService(userRepository, eventManager, null));
    }

    @Test
    void authenticateWithStatus_AdminCredentials_ReturnsSuccessAndNotifiesAdminMessage() {
        // Arrange
        SystemUser admin = new SystemUser("admin-1", "admin@gmail.com", "admin123", UserRole.ADMIN);
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));

        // Act
        LoginStatus result = adminAuthService.authenticateWithStatus(new Credentials(" ADMIN@GMAIL.COM ", "admin123"));

        // Assert
        assertEquals(LoginStatus.SUCCESS, result);
        verify(userRepository).findByEmail("admin@gmail.com");
        verify(eventManager).notifyObservers("Admin logged in successfully");
    }

    @Test
    void authenticateWithStatus_UserCredentials_ReturnsSuccessAndNotifiesUserMessage() {
        // Arrange
        SystemUser user = new SystemUser("user-1", "user@gmail.com", "user123", UserRole.USER);
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

        // Act
        LoginStatus result = adminAuthService.authenticateWithStatus(new Credentials(" user@gmail.com ", "user123"));

        // Assert
        assertEquals(LoginStatus.SUCCESS, result);
        verify(eventManager).notifyObservers("User logged in successfully");
    }

    @Test
    void authenticateWithStatus_InvalidPassword_ReturnsInvalidCredentials() {
        // Arrange
        SystemUser user = new SystemUser("user-1", "user@gmail.com", "user123", UserRole.USER);
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

        // Act
        LoginStatus result = adminAuthService.authenticateWithStatus(new Credentials("user@gmail.com", "wrong"));

        // Assert
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result);
        verify(eventManager).notifyObservers("Failed login attempt");
    }

    @Test
    void authenticateWithStatus_NullCredentials_ReturnsBlankInputWithoutRepositoryLookup() {
        // Arrange / Act
        LoginStatus result = adminAuthService.authenticateWithStatus(null);

        // Assert
        assertEquals(LoginStatus.BLANK_INPUT, result);
        verifyNoInteractions(userRepository);
        verify(eventManager, never()).notifyObservers("Failed login attempt");
    }

    @Test
    void authenticate_WithBlankRawEmail_ThrowsIllegalArgumentExceptionFromCredentials() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> adminAuthService.authenticate("   ", "pw"));
        assertThrows(IllegalArgumentException.class, () -> adminAuthService.authenticateWithStatus("", "pw"));
    }

    @Test
    void authenticateWithPolicy_Success_ReturnsAuthenticatedUserContext() {
        // Arrange
        SystemUser admin = new SystemUser("admin-1", "admin@gmail.com", "admin123", UserRole.ADMIN);
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));

        // Act
        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(
                new Credentials(" ADMIN@GMAIL.COM ", "admin123")
        );

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("admin@gmail.com", result.getAuthenticatedEmail());
        assertEquals(UserRole.ADMIN, result.getAuthenticatedRole());
        assertEquals(admin, result.getAuthenticatedUser());
    }

    @Test
    void authenticateWithPolicy_InvalidCredentials_ReturnsFailureAndTracksRemainingAttempts() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act
        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(
                new Credentials("unknown@example.com", "pw")
        );

        // Assert
        assertFalse(result.isSuccess());
        assertFalse(result.isLocked());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        assertEquals(2, result.getAttemptsRemaining());
        verify(eventManager).notifyObservers("Failed login attempt");
    }

    @Test
    void authenticateWithPolicy_NullCredentials_ReturnsBlankInputAndCountsFailure() {
        // Arrange / Act
        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(null);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals(LoginStatus.BLANK_INPUT, result.getStatus());
        assertEquals(2, result.getAttemptsRemaining());
        verifyNoInteractions(userRepository);
        verify(eventManager).notifyObservers("Failed login attempt");
    }

    @Test
    void authenticateWithPolicy_AfterMaxFailures_ReturnsLockedResult() {
        // Arrange
        LoginAttemptTracker tracker = new LoginAttemptTracker(2, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(userRepository, eventManager, tracker);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act
        AuthenticationAttemptResult first = service.authenticateWithPolicy(new Credentials("unknown@example.com", "pw"));
        AuthenticationAttemptResult second = service.authenticateWithPolicy(new Credentials("unknown@example.com", "pw"));
        AuthenticationAttemptResult third = service.authenticateWithPolicy(new Credentials("unknown@example.com", "pw"));

        // Assert
        assertFalse(first.isLocked());
        assertTrue(second.isLocked());
        assertTrue(third.isLocked());
        verify(userRepository, times(2)).findByEmail("unknown@example.com");
    }

    @Test
    void isLockedAndRemainingLockSeconds_ReflectCurrentTrackerState() {
        // Arrange
        LoginAttemptTracker tracker = new LoginAttemptTracker(1, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(userRepository, eventManager, tracker);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act
        service.authenticateWithPolicy(new Credentials("unknown@example.com", "pw"));

        // Assert
        assertTrue(service.isLocked());
        assertTrue(service.getRemainingLockSeconds() > 0);
    }
}
