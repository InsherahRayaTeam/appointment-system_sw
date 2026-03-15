package org.example;

import org.example.domain.AdminUser;
import org.example.repository.AdminRepository;
import org.example.service.AdminAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminAuthServiceTest {

    @Mock
    private AdminRepository adminRepository;

    private AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminAuthService = new AdminAuthService(adminRepository);
    }

    @Test
    void authenticate_ValidCredentials_ReturnsTrue() {
        // Arrange
        String username = "admin";
        String password = "admin";
        AdminUser adminUser = new AdminUser(username, password);
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        // Act
        boolean result = adminAuthService.authenticate(username, password);

        // Assert
        assertTrue(result);
        verify(adminRepository).findByUsername(username);
    }

    @Test
    void authenticate_InvalidPassword_ReturnsFalse() {
        // Arrange
        String username = "admin";
        String password = "wrong";
        AdminUser adminUser = new AdminUser(username, "admin");
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        // Act
        boolean result = adminAuthService.authenticate(username, password);

        // Assert
        assertFalse(result);
        verify(adminRepository).findByUsername(username);
    }

    @Test
    void authenticate_UserNotFound_ReturnsFalse() {
        // Arrange
        String username = "unknown";
        String password = "admin";
        when(adminRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean result = adminAuthService.authenticate(username, password);

        // Assert
        assertFalse(result);
        verify(adminRepository).findByUsername(username);
    }

    @Test
    void authenticate_NullUsername_ReturnsFalse() {
        // Act
        boolean result = adminAuthService.authenticate(null, "password");

        // Assert
        assertFalse(result);
        verifyNoInteractions(adminRepository);
    }

    @Test
    void authenticate_NullPassword_ReturnsFalse() {
        // Act
        boolean result = adminAuthService.authenticate("admin", null);

        // Assert
        assertFalse(result);
        verifyNoInteractions(adminRepository);
    }
}
