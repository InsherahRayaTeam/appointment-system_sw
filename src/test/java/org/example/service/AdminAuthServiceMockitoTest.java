package org.example.service;

import org.example.domain.AdminUser;
import org.example.domain.UserRole;
import org.example.repository.AdminRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminAuthServiceMockitoTest {

    @Test
    void shouldAuthenticateSuccessfully() {
        AdminRepository mockRepo = mock(AdminRepository.class);
        AdminUser admin = new AdminUser("admin-1", "admin", "1234", UserRole.ADMIN);
        when(mockRepo.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        EventManager eventManager = mock(EventManager.class);
        LoginAttemptTracker loginAttemptTracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(mockRepo, eventManager, loginAttemptTracker);

        boolean result = service.authenticate("admin", "1234");

        assertTrue(result);
        verify(eventManager, times(1)).notifyObservers("Admin logged in successfully");
    }

    @Test
    void shouldFailWhenUserNotFound() {
        AdminRepository mockRepo = mock(AdminRepository.class);

        when(mockRepo.findByUsername("admin"))
                .thenReturn(Optional.empty());

        EventManager eventManager = mock(EventManager.class);
        LoginAttemptTracker loginAttemptTracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(mockRepo, eventManager, loginAttemptTracker);

        boolean result = service.authenticate("admin", "1234");

        assertFalse(result);
        verify(eventManager, times(1)).notifyObservers("Failed login attempt");
    }

    @Test
    void shouldNotifyObserverOnSuccessfulAuthentication() {
        AdminRepository mockRepo = mock(AdminRepository.class);
        when(mockRepo.findByUsername("admin")).thenReturn(
                Optional.of(new AdminUser("admin-1", "admin", "1234", UserRole.ADMIN))
        );

        EventManager eventManager = mock(EventManager.class);
        LoginAttemptTracker loginAttemptTracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(mockRepo, eventManager, loginAttemptTracker);

        boolean result = service.authenticate("admin", "1234");

        assertTrue(result);
        verify(eventManager).notifyObservers("Admin logged in successfully");
    }

    @Test
    void shouldNotifyObserverOnFailedAuthentication() {
        AdminRepository mockRepo = mock(AdminRepository.class);
        when(mockRepo.findByUsername("admin")).thenReturn(Optional.empty());

        EventManager eventManager = mock(EventManager.class);
        LoginAttemptTracker loginAttemptTracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(mockRepo, eventManager, loginAttemptTracker);

        boolean result = service.authenticate("admin", "wrong");

        assertFalse(result);
        verify(eventManager).notifyObservers("Failed login attempt");
    }

    @Test
    void shouldNotNotifyObserverWhenInputIsBlank() {
        AdminRepository mockRepo = mock(AdminRepository.class);

        EventManager eventManager = mock(EventManager.class);
        LoginAttemptTracker loginAttemptTracker = new LoginAttemptTracker(3, Duration.ofSeconds(30));
        AdminAuthService service = new AdminAuthService(mockRepo, eventManager, loginAttemptTracker);

        boolean result = service.authenticate("   ", "   ");

        assertFalse(result);
        verify(eventManager, never()).notifyObservers(anyString());
    }
}