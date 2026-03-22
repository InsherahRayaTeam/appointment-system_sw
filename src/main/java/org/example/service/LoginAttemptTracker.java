package org.example.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Tracks failed login attempts and enforces temporary account lockout.
 * After a maximum number of consecutive failures, the account is locked for a specified duration.
 */
public class LoginAttemptTracker {

    private final int maxFailedAttempts;
    private final Duration lockDuration;
    private final Clock clock;

    private int failedAttempts;
    private Instant lockedUntil;

    /**
     * Creates a login attempt tracker with the specified limits.
     *
     * @param maxFailedAttempts the maximum number of failed attempts before lockout
     * @param lockDuration      the duration to lock the account after exceeding max attempts
     * @throws IllegalArgumentException if maxFailedAttempts is less than 1 or lockDuration is not positive
     */
    public LoginAttemptTracker(int maxFailedAttempts, Duration lockDuration) {
        this(maxFailedAttempts, lockDuration, Clock.systemUTC());
    }

    LoginAttemptTracker(int maxFailedAttempts, Duration lockDuration, Clock clock) {
        if (maxFailedAttempts < 1) {
            throw new IllegalArgumentException("maxFailedAttempts must be at least 1");
        }
        if (lockDuration == null || lockDuration.isNegative() || lockDuration.isZero()) {
            throw new IllegalArgumentException("lockDuration must be positive");
        }
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    /**
     * Checks if the account is currently locked.
     *
     * @return true if locked and lock duration has not expired, false otherwise
     */
    public boolean isLocked() {
        return lockedUntil != null && Instant.now(clock).isBefore(lockedUntil);
    }

    /**
     * Returns the remaining seconds until the lock expires.
     *
     * @return remaining lock duration in seconds, or 0 if not locked
     */
    public long getRemainingLockSeconds() {
        if (!isLocked()) {
            return 0;
        }
        return Duration.between(Instant.now(clock), lockedUntil).toSeconds() + 1;
    }

    /**
     * Records a failed login attempt.
     * If max attempts exceeded, triggers account lockout.
     */
    public void recordFailure() {
        if (isLocked()) {
            return;
        }

        failedAttempts++;
        if (failedAttempts >= maxFailedAttempts) {
            lockedUntil = Instant.now(clock).plus(lockDuration);
            failedAttempts = 0;
        }
    }

    /**
     * Records a successful login attempt.
     * Resets failed attempt counter and clears any active lockout.
     */
    public void recordSuccess() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    /**
     * Returns the current count of failed login attempts.
     *
     * @return the number of consecutive failed attempts
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }
}
