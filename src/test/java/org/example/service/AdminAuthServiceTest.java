package org.example;

import org.example.domain.AdminUser;
import org.example.domain.Credentials;
import org.example.repository.AdminRepository;
import org.example.service.AdminAuthService;
import org.example.service.LoginStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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

    // ==================== Parameterized Tests ====================

    /**
     * Parameterized test for valid username and password combinations.
     * Tests that successful authentication returns SUCCESS status and queries repository.
     */
    @ParameterizedTest(name = "Valid credentials: username=''{0}'', password=''{1}''")
    @CsvSource({
        "admin, admin",
        "testuser, testpass",
        "user123, pass123"
    })
    void authenticateWithStatus_ValidCredentials_ReturnsSuccess(String username, String password) {
        AdminUser adminUser = new AdminUser(username, password);
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        LoginStatus result = adminAuthService.authenticateWithStatus(username, password);

        assertEquals(LoginStatus.SUCCESS, result);
        verify(adminRepository).findByUsername(username);
    }

    /**
     * Parameterized test for usernames with leading/trailing whitespace.
     * Tests that whitespace is trimmed before repository lookup.
     */
    @ParameterizedTest(name = "Trimmed username: username=''{0}'' (raw with spaces)")
    @CsvSource({
        "' admin ', admin",
        "'  testuser  ', testpass",
        "'\t user123 \t', pass123"
    })
    void authenticateWithStatus_UsernameWithWhitespace_TrimmedAndAuthenticated(String rawUsername, String password) {
        String trimmedUsername = rawUsername.trim();
        AdminUser adminUser = new AdminUser(trimmedUsername, password);
        when(adminRepository.findByUsername(trimmedUsername)).thenReturn(Optional.of(adminUser));

        LoginStatus result = adminAuthService.authenticateWithStatus(rawUsername, password);

        assertEquals(LoginStatus.SUCCESS, result);
        verify(adminRepository).findByUsername(trimmedUsername);
    }

    /**
     * Parameterized test for blank or whitespace-only passwords.
     * Tests that blank passwords are rejected without repository lookup.
     */
    @ParameterizedTest(name = "Blank password: username='admin', password=''{0}''")
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void authenticateWithStatus_BlankPassword_ReturnsBlankInput(String blankPassword) {
        LoginStatus result = adminAuthService.authenticateWithStatus("admin", blankPassword);

        assertEquals(LoginStatus.BLANK_INPUT, result);
        verifyNoInteractions(adminRepository);
    }

    /**
     * Parameterized test for blank or whitespace-only usernames.
     * Tests that blank usernames are rejected without repository lookup.
     */
    @ParameterizedTest(name = "Blank username: username=''{0}'', password='password'")
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void authenticateWithStatus_BlankUsername_ReturnsBlankInput(String blankUsername) {
        LoginStatus result = adminAuthService.authenticateWithStatus(blankUsername, "password");

        assertEquals(LoginStatus.BLANK_INPUT, result);
        verifyNoInteractions(adminRepository);
    }

    /**
     * Parameterized test for invalid password scenarios.
     * Tests that incorrect passwords return INVALID_CREDENTIALS status.
     */
    @ParameterizedTest(name = "Invalid password: username='admin', password=''{0}''")
    @ValueSource(strings = {"wrong", "incorrect", "123456", "invalid"})
    void authenticateWithStatus_InvalidPassword_ReturnsInvalidCredentials(String invalidPassword) {
        String username = "admin";
        String correctPassword = "admin";
        AdminUser adminUser = new AdminUser(username, correctPassword);
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        LoginStatus result = adminAuthService.authenticateWithStatus(username, invalidPassword);

        assertEquals(LoginStatus.INVALID_CREDENTIALS, result);
        verify(adminRepository).findByUsername(username);
    }

    /**
     * Parameterized test for non-existent usernames.
     * Tests that unknown users return INVALID_CREDENTIALS status.
     */
    @ParameterizedTest(name = "User not found: username=''{0}''")
    @ValueSource(strings = {"unknown", "nonexistent", "hacker", "admin123"})
    void authenticateWithStatus_UserNotFound_ReturnsInvalidCredentials(String unknownUsername) {
        when(adminRepository.findByUsername(unknownUsername)).thenReturn(Optional.empty());

        LoginStatus result = adminAuthService.authenticateWithStatus(unknownUsername, "password");

        assertEquals(LoginStatus.INVALID_CREDENTIALS, result);
        verify(adminRepository).findByUsername(unknownUsername);
    }

    /**
     * Parameterized test for backward-compatible boolean API with valid credentials.
     * Tests that authenticate(username, password) returns true for valid logins.
     */
    @ParameterizedTest(name = "Boolean API - Valid: username=''{0}'', password=''{1}''")
    @CsvSource({
        "admin, admin",
        "user, pass",
        "test, test123"
    })
    void authenticate_BooleanApi_ValidCredentials_ReturnsTrue(String username, String password) {
        AdminUser adminUser = new AdminUser(username, password);
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        boolean result = adminAuthService.authenticate(username, password);

        assertTrue(result);
        verify(adminRepository).findByUsername(username);
    }

    /**
     * Parameterized test for backward-compatible boolean API with invalid credentials.
     * Tests that authenticate(username, password) returns false for failed logins.
     */
    @ParameterizedTest(name = "Boolean API - Invalid: username=''{0}'', password=''{1}''")
    @CsvSource({
        "admin, wrongpass",
        "unknown, pass",
        "user, wrong"
    })
    void authenticate_BooleanApi_InvalidCredentials_ReturnsFalse(String username, String password) {
        when(adminRepository.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = adminAuthService.authenticate(username, password);

        assertFalse(result);
    }

    /**
     * Parameterized test for backward-compatible boolean API with blank inputs.
     * Tests that authenticate(username, password) returns false for blank inputs.
     */
    @ParameterizedTest(name = "Boolean API - Blank: username=''{0}'', password=''{1}''")
    @CsvSource({
        "   , password",
        "admin,    ",
        "  ,   "
    })
    void authenticate_BooleanApi_BlankInput_ReturnsFalse(String username, String password) {
        boolean result = adminAuthService.authenticate(username, password);

        assertFalse(result);
        verifyNoInteractions(adminRepository);
    }
}
