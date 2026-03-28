package org.example.service;

import org.example.domain.AdminUser;
import org.example.notification.Observer;
import org.example.repository.AdminRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminAuthServiceMockitoTest {

    @Test
    void shouldAuthenticateSuccessfully() {
        // 🟢 mock repository
        AdminRepository mockRepo = mock(AdminRepository.class);

        // 🟢 fake data
        AdminUser admin = new AdminUser("admin", "1234");

        // 🟢 behavior
        when(mockRepo.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        AdminAuthService service = new AdminAuthService(mockRepo);

        boolean result = service.authenticate("admin", "1234");

        assertTrue(result);
    }

    @Test
    void shouldFailWhenUserNotFound() {
        AdminRepository mockRepo = mock(AdminRepository.class);

        when(mockRepo.findByUsername("admin"))
                .thenReturn(Optional.empty());

        AdminAuthService service = new AdminAuthService(mockRepo);

        boolean result = service.authenticate("admin", "1234");

        assertFalse(result);
    }

    @Test
    void shouldNotifyObserverOnSuccessfulAuthentication() {
        AdminRepository mockRepo = mock(AdminRepository.class);
        when(mockRepo.findByUsername("admin")).thenReturn(Optional.of(new AdminUser("admin", "1234")));

        EventManager eventManager = new EventManager();
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);

        AdminAuthService service = new AdminAuthService(mockRepo, eventManager);

        boolean result = service.authenticate("admin", "1234");

        assertTrue(result);
        verify(observer).update("Admin logged in successfully");
    }

    @Test
    void shouldNotifyObserverOnFailedAuthentication() {
        AdminRepository mockRepo = mock(AdminRepository.class);
        when(mockRepo.findByUsername("admin")).thenReturn(Optional.empty());

        EventManager eventManager = new EventManager();
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);

        AdminAuthService service = new AdminAuthService(mockRepo, eventManager);

        boolean result = service.authenticate("admin", "wrong");

        assertFalse(result);
        verify(observer).update("Failed login attempt");
    }

    @Test
    void shouldNotNotifyObserverWhenInputIsBlank() {
        AdminRepository mockRepo = mock(AdminRepository.class);

        EventManager eventManager = new EventManager();
        Observer observer = mock(Observer.class);
        eventManager.subscribe(observer);

        AdminAuthService service = new AdminAuthService(mockRepo, eventManager);

        boolean result = service.authenticate("   ", "   ");

        assertFalse(result);
        verify(observer, never()).update(anyString());
    }
}