package org.example.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Tracks failed login attempts and temporary account lock time.
 */
public class LoginAttemptTracker {

    private final int maxFailedAttempts;
    private final Duration lockDuration;
    private final Clock clock;

    private int failedAttempts;
    private Instant lockedUntil;

    /**
     * Creates a new login attempt tracker object with the given values.
     *
     * @param maxFailedAttempts value for max failed attempts
     * @param lockDuration appointment duration in minutes
     */
    public LoginAttemptTracker(int maxFailedAttempts, Duration lockDuration) {
        this(maxFailedAttempts, lockDuration, Clock.systemUTC());
    }

    /**
     * Creates a new login attempt tracker object with the given values.
     *
     * @param maxFailedAttempts value for max failed attempts
     * @param lockDuration appointment duration in minutes
     * @param clock value for clock
     */
    LoginAttemptTracker(int maxFailedAttempts, Duration lockDuration, Clock clock) {
        if (maxFailedAttempts < 1) {
            throw new IllegalArgumentException("maxFailedAttempts must be at least 1");
        }
        if (lockDuration == null || lockDuration.isNegative() || lockDuration.isZero()) {
            throw new IllegalArgumentException("lockDuration must be positive");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock cannot be null");
        }

        this.maxFailedAttempts = maxFailedAttempts;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    /**
     * Checks whether locked is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isLocked() {
        return lockedUntil != null && Instant.now(clock).isBefore(lockedUntil);
    }

    /**
     * Returns the remaining lock seconds.
     *
     * @return numeric result from this method
     */
    public long getRemainingLockSeconds() {
        if (!isLocked()) {
            return 0;
        }
        return Duration.between(Instant.now(clock), lockedUntil).toSeconds() + 1;
    }

    /**
     * Records one failed login attempt.
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
     * Clears failure and lock state after a successful login.
     */
    public void recordSuccess() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    /**
     * Returns the failed attempts.
     *
     * @return numeric result from this method
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Returns the attempts remaining.
     *
     * @return numeric result from this method
     */
    public int getAttemptsRemaining() {
        return Math.max(0, maxFailedAttempts - failedAttempts);
    }
}
