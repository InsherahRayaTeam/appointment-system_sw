package org.example;

import org.example.domain.AdminUser;
import org.example.domain.Credentials;
import org.example.repository.AdminRepository;
import org.example.service.AdminAuthService;
import org.example.service.LoginStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
    void authenticateWithStatus_CredentialsObject_Success_ReturnsSuccess() {
        String username = "admin";
        String password = "admin";
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(new AdminUser(username, password)));

        LoginStatus result = adminAuthService.authenticateWithStatus(new Credentials(" admin ", password));

        assertEquals(LoginStatus.SUCCESS, result);
        verify(adminRepository).findByUsername(username);
    }

    @Test
    void authenticateWithStatus_BlankPassword_ReturnsBlankInputStatus() {
        LoginStatus result = adminAuthService.authenticateWithStatus("admin", "   ");

        assertEquals(LoginStatus.BLANK_INPUT, result);
        verifyNoInteractions(adminRepository);
    }

    @Test
    void authenticateWithStatus_NullCredentials_ReturnsBlankInputStatus() {
        LoginStatus result = adminAuthService.authenticateWithStatus((Credentials) null);

        assertEquals(LoginStatus.BLANK_INPUT, result);
        verifyNoInteractions(adminRepository);
    }

    @Test
    void authenticate_ValidCredentials_ReturnsSuccess() {
        String username = "admin";
        String password = "admin";
        AdminUser adminUser = new AdminUser(username, password);
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        LoginStatus result = adminAuthService.authenticateWithStatus(username, password);

        assertEquals(LoginStatus.SUCCESS, result);
        verify(adminRepository).findByUsername(username);
    }

    @Test
    void authenticate_TrimmedUsername_ValidCredentials_ReturnsSuccess() {
        String username = "admin";
        String password = "admin";
        AdminUser adminUser = new AdminUser(username, password);
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        LoginStatus result = adminAuthService.authenticateWithStatus("  admin  ", password);

        assertEquals(LoginStatus.SUCCESS, result);
        verify(adminRepository).findByUsername(username);
    }

    @Test
    void authenticate_BlankInput_ReturnsBlankInputStatus() {
        LoginStatus result = adminAuthService.authenticateWithStatus("   ", "password");

        assertEquals(LoginStatus.BLANK_INPUT, result);
        verifyNoInteractions(adminRepository);
    }

    @Test
    void authenticate_InvalidPassword_ReturnsInvalidCredentials() {
        String username = "admin";
        String password = "wrong";
        AdminUser adminUser = new AdminUser(username, "admin");
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        LoginStatus result = adminAuthService.authenticateWithStatus(username, password);

        assertEquals(LoginStatus.INVALID_CREDENTIALS, result);
        verify(adminRepository).findByUsername(username);
    }

    @Test
    void authenticate_UserNotFound_ReturnsInvalidCredentials() {
        String username = "unknown";
        String password = "admin";
        when(adminRepository.findByUsername(username)).thenReturn(Optional.empty());

        LoginStatus result = adminAuthService.authenticateWithStatus(username, password);

        assertEquals(LoginStatus.INVALID_CREDENTIALS, result);
        verify(adminRepository).findByUsername(username);
    }

    @Test
    void authenticate_BackwardCompatibleBooleanApi_ReturnsTrueForSuccess() {
        String username = "admin";
        String password = "admin";
        AdminUser adminUser = new AdminUser(username, password);
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        boolean result = adminAuthService.authenticate(username, password);

        assertTrue(result);
    }

    @Test
    void authenticate_BackwardCompatibleBooleanApi_ReturnsFalseForBlankInput() {
        boolean result = adminAuthService.authenticate("   ", "   ");

        assertFalse(result);
        verifyNoInteractions(adminRepository);
    }
}
