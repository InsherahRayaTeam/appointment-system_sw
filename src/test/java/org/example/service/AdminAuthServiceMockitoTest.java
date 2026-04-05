package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAuthServiceMockitoTest {

    @Test
    void constructor_WithNullDependencies_ThrowsNullPointerException() {
        EventManager eventManager = mock(EventManager.class);
        LoginAttemptTracker tracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));

        assertThrows(NullPointerException.class, () -> new AdminAuthService(null, eventManager, tracker));
        assertThrows(NullPointerException.class, () -> new AdminAuthService(mock(UserRepository.class), null, tracker));
        assertThrows(
                NullPointerException.class,
                () -> new AdminAuthService(mock(UserRepository.class), eventManager, null)
        );
    }

    @Test
    void authenticate_BooleanApi_WithValidUser_ReturnsTrue() {
        UserRepository mockRepo = mock(UserRepository.class);
        SystemUser admin = new SystemUser("admin-1", "admin@gmail.com", "1234", UserRole.ADMIN);
        when(mockRepo.findByEmail("admin@gmail.com")).thenReturn(Optional.of(admin));

        EventManager eventManager = mock(EventManager.class);
        LoginAttemptTracker tracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(mockRepo, eventManager, tracker);

        boolean result = service.authenticate(" ADMIN@GMAIL.COM ", "1234");

        assertTrue(result);
        verify(mockRepo).findByEmail("admin@gmail.com");
        verify(eventManager).notifyObservers("Admin logged in successfully");
    }

    @Test
    void authenticateWithPolicy_LocksAfterThreshold_ExposesLockSeconds() {
        UserRepository mockRepo = mock(UserRepository.class);
        when(mockRepo.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        EventManager eventManager = mock(EventManager.class);
        LoginAttemptTracker tracker = new LoginAttemptTracker(1, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(mockRepo, eventManager, tracker);

        AuthenticationAttemptResult result = service.authenticateWithPolicy(
                new org.example.domain.Credentials("unknown@example.com", "pw")
        );

        assertTrue(result.isLocked());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        assertTrue(service.isLocked());
        assertTrue(service.getRemainingLockSeconds() > 0);
    }
}