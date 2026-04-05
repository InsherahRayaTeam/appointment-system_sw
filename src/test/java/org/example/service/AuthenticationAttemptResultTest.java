package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationAttemptResultTest {

    @Test
    void success_WithoutDetails_ReturnsSuccessfulResult() {
        // Arrange / Act
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(LoginStatus.SUCCESS, result.getStatus());
        assertFalse(result.isLocked());
        assertEquals(0, result.getAttemptsRemaining());
        assertEquals(0, result.getRemainingLockSeconds());
        assertNull(result.getAuthenticatedEmail());
        assertNull(result.getAuthenticatedRole());
        assertNull(result.getAuthenticatedUser());
    }

    @Test
    void success_WithEmailAndRole_ReturnsSuccessfulResultWithIdentity() {
        // Arrange / Act
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success("admin@example.com", UserRole.ADMIN);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("admin@example.com", result.getAuthenticatedEmail());
        assertEquals(UserRole.ADMIN, result.getAuthenticatedRole());
        assertNull(result.getAuthenticatedUser());
    }

    @Test
    void success_WithUser_ReturnsSuccessfulResultWithFullContext() {
        // Arrange
        SystemUser user = new SystemUser("admin-1", "admin@example.com", "pw", UserRole.ADMIN);

        // Act
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success(user);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("admin@example.com", result.getAuthenticatedEmail());
        assertEquals(UserRole.ADMIN, result.getAuthenticatedRole());
        assertEquals(user, result.getAuthenticatedUser());
    }

    @Test
    void success_WithNullUser_FallsBackToDetailFreeSuccess() {
        // Arrange / Act
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success((SystemUser) null);

        // Assert
        assertTrue(result.isSuccess());
        assertNull(result.getAuthenticatedEmail());
        assertNull(result.getAuthenticatedRole());
        assertNull(result.getAuthenticatedUser());
    }

    @Test
    void failure_WithInvalidCredentialsStatus_ClampsNegativeRemainingAttempts() {
        // Arrange / Act
        AuthenticationAttemptResult result = AuthenticationAttemptResult.failure(LoginStatus.INVALID_CREDENTIALS, -3);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        assertEquals(0, result.getAttemptsRemaining());
        assertFalse(result.isLocked());
    }

    @Test
    void failure_WithBlankInputStatus_PreservesProvidedAttemptCount() {
        // Arrange / Act
        AuthenticationAttemptResult result = AuthenticationAttemptResult.failure(LoginStatus.BLANK_INPUT, 2);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals(LoginStatus.BLANK_INPUT, result.getStatus());
        assertEquals(2, result.getAttemptsRemaining());
    }

    @Test
    void failure_WithSuccessStatus_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> AuthenticationAttemptResult.failure(LoginStatus.SUCCESS, 1)
        );
    }

    @Test
    void failure_WithNullStatus_ThrowsException() {
        // Arrange / Act / Assert
        assertThrows(NullPointerException.class, () -> AuthenticationAttemptResult.failure(null, 1));
    }

    @Test
    void locked_WithPositiveSeconds_ReturnsLockedResult() {
        // Arrange / Act
        AuthenticationAttemptResult result = AuthenticationAttemptResult.locked(15);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.isLocked());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        assertEquals(15, result.getRemainingLockSeconds());
        assertEquals(0, result.getAttemptsRemaining());
    }

    @Test
    void locked_WithNegativeSeconds_ClampsToZero() {
        // Arrange / Act
        AuthenticationAttemptResult result = AuthenticationAttemptResult.locked(-10);

        // Assert
        assertTrue(result.isLocked());
        assertEquals(0, result.getRemainingLockSeconds());
    }
}
