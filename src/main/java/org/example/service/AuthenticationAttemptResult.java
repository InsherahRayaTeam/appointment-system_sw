package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;

import java.util.Objects;

/**
 * Immutable service-layer result for policy-aware authentication attempts.
 *
 * Carries authentication status, lock state, remaining attempts,
 * and authenticated user details when login succeeds.
 *
 * @author appointment-system
 * @version 1.0
 */
public final class AuthenticationAttemptResult {

    private final LoginStatus status;
    private final boolean locked;
    private final long remainingLockSeconds;
    private final int attemptsRemaining;
    private final String authenticatedEmail;
    private final UserRole authenticatedRole;
    private final SystemUser authenticatedUser;

    /**
     * Creates an immutable authentication attempt result.
     *
     * @param status login status
     * @param locked whether account is currently locked
     * @param remainingLockSeconds remaining lock duration in seconds
     * @param attemptsRemaining remaining attempts before lockout
     * @param authenticatedEmail authenticated email on success
     * @param authenticatedRole authenticated role on success
     * @param authenticatedUser authenticated user on success
     */
    private AuthenticationAttemptResult(
            LoginStatus status,
            boolean locked,
            long remainingLockSeconds,
            int attemptsRemaining,
            String authenticatedEmail,
            UserRole authenticatedRole,
            SystemUser authenticatedUser
    ) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.locked = locked;
        this.remainingLockSeconds = remainingLockSeconds;
        this.attemptsRemaining = attemptsRemaining;
        this.authenticatedEmail = authenticatedEmail;
        this.authenticatedRole = authenticatedRole;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Creates a success result without user details.
     *
     * @return successful authentication result
     */
    public static AuthenticationAttemptResult success() {
        return new AuthenticationAttemptResult(
                LoginStatus.SUCCESS,
                false,
                0,
                0,
                null,
                null,
                null
        );
    }

    /**
     * Creates a success result with authenticated email and role.
     *
     * @param email authenticated email
     * @param role authenticated role
     * @return successful authentication result
     */
    public static AuthenticationAttemptResult success(String email, UserRole role) {
        return new AuthenticationAttemptResult(
                LoginStatus.SUCCESS,
                false,
                0,
                0,
                email,
                role,
                null
        );
    }

    /**
     * Creates a success result with full authenticated user context.
     *
     * @param user authenticated user
     * @return successful authentication result
     */
    public static AuthenticationAttemptResult success(SystemUser user) {
        if (user == null) {
            return success();
        }

        return new AuthenticationAttemptResult(
                LoginStatus.SUCCESS,
                false,
                0,
                0,
                user.getEmail(),
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

        return new AuthenticationAttemptResult(
                status,
                false,
                0,
                Math.max(0, attemptsRemaining),
                null,
                null,
                null
        );
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
     * Indicates whether the account is currently locked.
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
     * Returns authenticated email when login succeeds.
     *
     * @return authenticated email, or null when login failed
     */
    public String getAuthenticatedEmail() {
        return authenticatedEmail;
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
    public SystemUser getAuthenticatedUser() {
        return authenticatedUser;
    }
}