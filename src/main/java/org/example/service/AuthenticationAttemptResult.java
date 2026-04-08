package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;

import java.util.Objects;

/**
 * Represents authentication attempt result in the system.
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
     * Creates a new authentication attempt result object with the given values.
     *
     * @param status status value used for this operation
     * @param locked value for locked
     * @param remainingLockSeconds value for remaining lock seconds
     * @param attemptsRemaining value for attempts remaining
     * @param authenticatedEmail email address used for login or matching
     * @param authenticatedRole role value used for access control
     * @param authenticatedUser user involved in this action
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
     * Runs success for this class.
     *
     * @return result produced by this method
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
     * Runs success for this class.
     *
     * @param email email address used for login or matching
     * @param role role value used for access control
     *
     * @return result produced by this method
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
     * Runs success for this class.
     *
     * @param user user involved in this action
     *
     * @return result produced by this method
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
     * Runs failure for this class.
     *
     * @param status status value used for this operation
     * @param attemptsRemaining value for attempts remaining
     *
     * @return result produced by this method
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
     * Runs locked for this class.
     *
     * @param remainingLockSeconds value for remaining lock seconds
     *
     * @return result produced by this method
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
     * Returns the status.
     *
     * @return status that explains the operation result
     */
    public LoginStatus getStatus() {
        return status;
    }

    /**
     * Checks whether success is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isSuccess() {
        return status == LoginStatus.SUCCESS;
    }

    /**
     * Checks whether locked is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Returns the remaining lock seconds.
     *
     * @return numeric result from this method
     */
    public long getRemainingLockSeconds() {
        return remainingLockSeconds;
    }

    /**
     * Returns the attempts remaining.
     *
     * @return numeric result from this method
     */
    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }

    /**
     * Returns the authenticated email.
     *
     * @return text result from this method
     */
    public String getAuthenticatedEmail() {
        return authenticatedEmail;
    }

    /**
     * Returns the authenticated role.
     *
     * @return requested value from this object
     */
    public UserRole getAuthenticatedRole() {
        return authenticatedRole;
    }

    /**
     * Returns the authenticated user.
     *
     * @return requested value from this object
     */
    public SystemUser getAuthenticatedUser() {
        return authenticatedUser;
    }
}
