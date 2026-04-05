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

import java.util.Optional;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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
    void authenticateWithStatus_AdminCredentials_ReturnsSuccessAndNotifiesAdminMessage() {
        SystemUser admin = new SystemUser("admin-1", "admin@gmail.com", "admin123", UserRole.ADMIN);
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));

        LoginStatus result = adminAuthService.authenticateWithStatus(new Credentials(" ADMIN@GMAIL.COM ", "admin123"));

        assertEquals(LoginStatus.SUCCESS, result);
        verify(userRepository).findByEmail("admin@gmail.com");
        verify(eventManager).notifyObservers("Admin logged in successfully");
    }

    @Test
    void authenticateWithStatus_UserCredentials_ReturnsSuccessAndNotifiesUserMessage() {
        SystemUser user = new SystemUser("user-1", "user@gmail.com", "user123", UserRole.USER);
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

        LoginStatus result = adminAuthService.authenticateWithStatus("user@gmail.com", "user123");

        assertEquals(LoginStatus.SUCCESS, result);
        verify(userRepository).findByEmail("user@gmail.com");
        verify(eventManager).notifyObservers("User logged in successfully");
    }

    @Test
    void authenticateWithStatus_WrongPassword_ReturnsInvalidCredentials() {
        SystemUser user = new SystemUser("admin-1", "admin@gmail.com", "admin123", UserRole.ADMIN);
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(user));

        LoginStatus result = adminAuthService.authenticateWithStatus("admin@gmail.com", "wrong");

        assertEquals(LoginStatus.INVALID_CREDENTIALS, result);
        verify(userRepository).findByEmail("admin@gmail.com");
        verify(eventManager).notifyObservers("Failed login attempt");
    }

    @Test
    void authenticateWithStatus_NullCredentials_ReturnsBlankInputAndSkipsDependencies() {
        LoginStatus result = adminAuthService.authenticateWithStatus((Credentials) null);

        assertEquals(LoginStatus.BLANK_INPUT, result);
        verifyNoInteractions(userRepository);
        verify(eventManager, never()).notifyObservers(anyString());
    }

    @Test
    void authenticate_RawBlankInput_ThrowsIllegalArgumentExceptionFromCredentials() {
        assertThrows(IllegalArgumentException.class, () -> adminAuthService.authenticate("  ", "pw"));
        assertThrows(IllegalArgumentException.class, () -> adminAuthService.authenticateWithStatus("", "pw"));
    }

    @Test
    void authenticateWithPolicy_Success_ReturnsAuthenticatedUserAndResetsTracker() {
        SystemUser admin = new SystemUser("admin-1", "admin@gmail.com", "admin123", UserRole.ADMIN);
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));

        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(
                new Credentials(" ADMIN@GMAIL.COM ", "admin123")
        );

        assertTrue(result.isSuccess());
        assertEquals("admin@gmail.com", result.getAuthenticatedEmail());
        assertEquals(UserRole.ADMIN, result.getAuthenticatedRole());
        assertEquals(admin, result.getAuthenticatedUser());
        verify(userRepository).findByEmail("admin@gmail.com");
        verify(eventManager).notifyObservers("Admin logged in successfully");
    }

    @Test
    void authenticateWithPolicy_InvalidCredentials_TracksFailuresAndReturnsRemainingAttempts() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(new Credentials("unknown@example.com", "pw"));

        assertFalse(result.isSuccess());
        assertFalse(result.isLocked());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        assertEquals(2, result.getAttemptsRemaining());
        verify(eventManager).notifyObservers("Failed login attempt");
    }

    @Test
    void authenticateWithPolicy_AfterMaxFailures_ReturnsLockedResult() {
        LoginAttemptTracker tracker = new LoginAttemptTracker(2, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(userRepository, eventManager, tracker);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        AuthenticationAttemptResult first = service.authenticateWithPolicy(new Credentials("unknown@example.com", "pw"));
        AuthenticationAttemptResult second = service.authenticateWithPolicy(new Credentials("unknown@example.com", "pw"));
        AuthenticationAttemptResult third = service.authenticateWithPolicy(new Credentials("unknown@example.com", "pw"));

        assertFalse(first.isLocked());
        assertTrue(second.isLocked());
        assertTrue(third.isLocked());
        verify(userRepository, times(2)).findByEmail("unknown@example.com");
    }

    @Test
    void authenticateWithPolicy_NullCredentials_ReturnsBlankInputAndCountsFailure() {
        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(null);

        assertEquals(LoginStatus.BLANK_INPUT, result);
        assertEquals(2, result.getAttemptsRemaining());
        verifyNoInteractions(userRepository);
        verify(eventManager).notifyObservers("Failed login attempt");
    }

    @Test
    void isLockedAndGetRemainingLockSeconds_ReflectTrackerState() {
        LoginAttemptTracker tracker = new LoginAttemptTracker(1, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(userRepository, eventManager, tracker);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertFalse(service.isLocked());

        service.authenticateWithPolicy(new Credentials("unknown@example.com", "pw"));

        assertTrue(service.isLocked());
        assertTrue(service.getRemainingLockSeconds() > 0);
    }
}
