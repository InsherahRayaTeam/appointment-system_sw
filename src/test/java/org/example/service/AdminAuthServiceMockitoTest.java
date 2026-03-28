package org.example.service;

import org.example.domain.AdminUser;
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
}