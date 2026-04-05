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
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success();

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
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success("admin@example.com", UserRole.ADMIN);

        assertTrue(result.isSuccess());
        assertEquals("admin@example.com", result.getAuthenticatedEmail());
        assertEquals(UserRole.ADMIN, result.getAuthenticatedRole());
        assertNull(result.getAuthenticatedUser());
    }

    @Test
    void success_WithUser_ReturnsSuccessfulResultWithFullContext() {
        SystemUser user = new SystemUser("admin-1", "admin@example.com", "pw", UserRole.ADMIN);

        AuthenticationAttemptResult result = AuthenticationAttemptResult.success(user);

        assertTrue(result.isSuccess());
        assertEquals("admin@example.com", result.getAuthenticatedEmail());
        assertEquals(UserRole.ADMIN, result.getAuthenticatedRole());
        assertEquals(user, result.getAuthenticatedUser());
    }

    @Test
    void success_WithNullUser_FallsBackToDetailFreeSuccess() {
        SystemUser user = null;
        AuthenticationAttemptResult result = AuthenticationAttemptResult.success(user);

        assertTrue(result.isSuccess());
        assertNull(result.getAuthenticatedEmail());
        assertNull(result.getAuthenticatedRole());
        assertNull(result.getAuthenticatedUser());
    }

    @Test
    void failure_WithInvalidCredentialsStatus_ClampsNegativeRemainingAttempts() {
        AuthenticationAttemptResult result = AuthenticationAttemptResult.failure(LoginStatus.INVALID_CREDENTIALS, -3);

        assertFalse(result.isSuccess());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        assertEquals(0, result.getAttemptsRemaining());
        assertFalse(result.isLocked());
    }

    @Test
    void failure_WithSuccessStatus_ThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AuthenticationAttemptResult.failure(LoginStatus.SUCCESS, 1)
        );
    }

    @Test
    void locked_WithPositiveSeconds_ReturnsLockedResult() {
        AuthenticationAttemptResult result = AuthenticationAttemptResult.locked(15);

        assertFalse(result.isSuccess());
        assertTrue(result.isLocked());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, result.getStatus());
        assertEquals(15, result.getRemainingLockSeconds());
        assertEquals(0, result.getAttemptsRemaining());
    }

    @Test
    void locked_WithNegativeSeconds_ClampsToZero() {
        AuthenticationAttemptResult result = AuthenticationAttemptResult.locked(-10);

        assertTrue(result.isLocked());
        assertEquals(0, result.getRemainingLockSeconds());
    }
}

