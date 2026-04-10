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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServicePolicyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventManager eventManager;

    @Mock
    private LoginAttemptTracker loginAttemptTracker;

    private AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        adminAuthService = new AdminAuthService(userRepository, eventManager, loginAttemptTracker);
    }

    @Test
    void authenticateWithPolicy_TrackerAlreadyLocked_ReturnsLockedWithoutRepositoryLookup() {
        when(loginAttemptTracker.isLocked()).thenReturn(true);
        when(loginAttemptTracker.getRemainingLockSeconds()).thenReturn(21L);

        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(
                new Credentials("user@example.com", "pw1234")
        );

        assertTrue(result.isLocked());
        assertEquals(21L, result.getRemainingLockSeconds());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(eventManager);
        verify(loginAttemptTracker, never()).recordFailure();
        verify(loginAttemptTracker, never()).recordSuccess();
    }

    @Test
    void authenticateWithPolicy_InvalidCredentialsNotLocked_ReturnsFailureAndAttemptsRemaining() {
        when(loginAttemptTracker.isLocked()).thenReturn(false, false);
        when(loginAttemptTracker.getAttemptsRemaining()).thenReturn(2);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(
                new Credentials("missing@example.com", "pw1234")
        );

        assertFalse(result.isSuccess());
        assertFalse(result.isLocked());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        assertEquals(2, result.getAttemptsRemaining());
        verify(loginAttemptTracker).recordFailure();
        verify(eventManager).notifyObservers("Failed login attempt");
    }

    @Test
    void authenticateWithPolicy_InvalidCredentialsTriggersLock_ReturnsLockedAfterFailure() {
        when(loginAttemptTracker.isLocked()).thenReturn(false, true);
        when(loginAttemptTracker.getRemainingLockSeconds()).thenReturn(30L);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(
                new Credentials("missing@example.com", "pw1234")
        );

        assertTrue(result.isLocked());
        assertEquals(30L, result.getRemainingLockSeconds());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        verify(loginAttemptTracker).recordFailure();
        verify(eventManager).notifyObservers("Failed login attempt");
    }

    @Test
    void authenticateWithPolicy_ValidUser_ReturnsSuccessAndClearsFailureState() {
        when(loginAttemptTracker.isLocked()).thenReturn(false);
        SystemUser user = new SystemUser("id-1", "employee@example.com", "pw1234", UserRole.USER);
        when(userRepository.findByEmail("employee@example.com")).thenReturn(Optional.of(user));

        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(
                new Credentials(" EMPLOYEE@example.com ", "pw1234")
        );

        assertTrue(result.isSuccess());
        assertEquals(LoginStatus.SUCCESS, result.getStatus());
        assertEquals("employee@example.com", result.getAuthenticatedEmail());
        assertEquals(UserRole.USER, result.getAuthenticatedRole());
        assertEquals(user, result.getAuthenticatedUser());
        verify(loginAttemptTracker).recordSuccess();
        verify(loginAttemptTracker, never()).recordFailure();
        verify(eventManager).notifyObservers("User logged in successfully");
    }

    @Test
    void authenticateWithPolicy_NullCredentials_ReturnsBlankInputAndNoRepositoryLookup() {
        when(loginAttemptTracker.isLocked()).thenReturn(false, false);
        when(loginAttemptTracker.getAttemptsRemaining()).thenReturn(1);

        AuthenticationAttemptResult result = adminAuthService.authenticateWithPolicy(null);

        assertFalse(result.isSuccess());
        assertEquals(LoginStatus.BLANK_INPUT, result.getStatus());
        assertEquals(1, result.getAttemptsRemaining());
        assertNull(result.getAuthenticatedUser());
        verify(loginAttemptTracker).recordFailure();
        verifyNoInteractions(userRepository);
        verify(eventManager).notifyObservers("Failed login attempt");
    }
}

