package org.example.service;

import org.example.domain.AdminUser;
import org.example.domain.UserRole;

import java.util.Objects;

/**
 * Immutable service-layer result for policy-aware authentication attempts.
 *
 * @author appointment-system
 * @version 1.0
 */
public final class AuthenticationAttemptResult {

    private final LoginStatus status;
    private final boolean locked;
    private final long remainingLockSeconds;
    private final int attemptsRemaining;
    private final String authenticatedUsername;
    private final UserRole authenticatedRole;
    private final AdminUser authenticatedUser;

    private AuthenticationAttemptResult(
            LoginStatus status,
            boolean locked,
            long remainingLockSeconds,
            int attemptsRemaining,
            String authenticatedUsername,
            UserRole authenticatedRole,
            AdminUser authenticatedUser
    ) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.locked = locked;
        this.remainingLockSeconds = remainingLockSeconds;
        this.attemptsRemaining = attemptsRemaining;
        this.authenticatedUsername = authenticatedUsername;
        this.authenticatedRole = authenticatedRole;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Creates a success result.
     *
     * @return successful authentication result
     */
    public static AuthenticationAttemptResult success() {
        return new AuthenticationAttemptResult(LoginStatus.SUCCESS, false, 0, 0, null, null, null);
    }

    /**
     * Creates a success result with authenticated user details.
     *
     * @param username authenticated username
     * @param role authenticated role
     * @return successful authentication result
     */
    public static AuthenticationAttemptResult success(String username, UserRole role) {
        return new AuthenticationAttemptResult(LoginStatus.SUCCESS, false, 0, 0, username, role, null);
    }

    /**
     * Creates a success result with full authenticated user context.
     *
     * @param user authenticated user
     * @return successful authentication result
     */
    public static AuthenticationAttemptResult success(AdminUser user) {
        if (user == null) {
            return success();
        }
        return new AuthenticationAttemptResult(
                LoginStatus.SUCCESS,
                false,
                0,
                0,
                user.getUsername(),
                user.getRole(),
                user
        );
    }

    /**
     * Creates a failure result with remaining attempt count.
     *
     * @param status failure status (must not be SUCCESS)
     * @param attemptsRemaining remaining failed-attempt budget
     * @return failure authentication result
     */
    public static AuthenticationAttemptResult failure(LoginStatus status, int attemptsRemaining) {
        if (status == LoginStatus.SUCCESS) {
            throw new IllegalArgumentException("status must represent a failure");
        }
        return new AuthenticationAttemptResult(status, false, 0, Math.max(0, attemptsRemaining), null, null, null);
    }

    /**
     * Creates a locked result with remaining lock duration.
     *
     * @param remainingLockSeconds remaining lock duration in seconds
     * @return locked authentication result
     */
    public static AuthenticationAttemptResult locked(long remainingLockSeconds) {
        return new AuthenticationAttemptResult(
                LoginStatus.INVALID_CREDENTIALS,
                true,
                Math.max(0, remainingLockSeconds),
                0,
                null,
                null,
                null
        );
    }

    /**
     * Returns the underlying login status.
     *
     * @return login status
     */
    public LoginStatus getStatus() {
        return status;
    }

    /**
     * Indicates whether authentication succeeded.
     *
     * @return true when status is SUCCESS, otherwise false
     */
    public boolean isSuccess() {
        return status == LoginStatus.SUCCESS;
    }

    /**
     * Indicates whether account is currently locked.
     *
     * @return true when locked, otherwise false
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Returns remaining lock duration in seconds.
     *
     * @return remaining lock seconds
     */
    public long getRemainingLockSeconds() {
        return remainingLockSeconds;
    }

    /**
     * Returns remaining attempts before lockout.
     *
     * @return remaining attempt count
     */
    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }

    /**
     * Returns authenticated username when login succeeds.
     *
     * @return authenticated username, or null when login failed
     */
    public String getAuthenticatedUsername() {
        return authenticatedUsername;
    }

    /**
     * Returns authenticated user role when login succeeds.
     *
     * @return authenticated role, or null when login failed
     */
    public UserRole getAuthenticatedRole() {
        return authenticatedRole;
    }

    /**
     * Returns authenticated user when login succeeds.
     *
     * @return authenticated user, or null when login failed
     */
    public AdminUser getAuthenticatedUser() {
        return authenticatedUser;
    }
}
