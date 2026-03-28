package org.example.service;

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

    private AuthenticationAttemptResult(
            LoginStatus status,
            boolean locked,
            long remainingLockSeconds,
            int attemptsRemaining
    ) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.locked = locked;
        this.remainingLockSeconds = remainingLockSeconds;
        this.attemptsRemaining = attemptsRemaining;
    }

    /**
     * Creates a success result.
     *
     * @return successful authentication result
     */
    public static AuthenticationAttemptResult success() {
        return new AuthenticationAttemptResult(LoginStatus.SUCCESS, false, 0, 0);
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
        return new AuthenticationAttemptResult(status, false, 0, Math.max(0, attemptsRemaining));
    }

    /**
     * Creates a locked result with remaining lock duration.
     *
     * @param remainingLockSeconds remaining lock duration in seconds
     * @return locked authentication result
     */
    public static AuthenticationAttemptResult locked(long remainingLockSeconds) {
        return new AuthenticationAttemptResult(LoginStatus.INVALID_CREDENTIALS, true, Math.max(0, remainingLockSeconds), 0);
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
}
