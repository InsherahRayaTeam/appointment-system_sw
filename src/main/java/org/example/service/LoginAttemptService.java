package org.example.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class LoginAttemptService {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final Duration DEFAULT_LOCK_DURATION = Duration.ofSeconds(30);

    private final int maxAttempts;
    private final Duration lockDuration;
    private final Clock clock;

    private int failedAttempts;
    private Instant lockedUntil;

    public LoginAttemptService() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_LOCK_DURATION, Clock.systemUTC());
    }

    public LoginAttemptService(int maxAttempts, Duration lockDuration) {
        this(maxAttempts, lockDuration, Clock.systemUTC());
    }

    LoginAttemptService(int maxAttempts, Duration lockDuration, Clock clock) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be > 0");
        }
        if (lockDuration == null || lockDuration.isNegative() || lockDuration.isZero()) {
            throw new IllegalArgumentException("lockDuration must be positive");
        }

        this.maxAttempts = maxAttempts;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }
        if (Instant.now(clock).isBefore(lockedUntil)) {
            return true;
        }

        lockedUntil = null;
        failedAttempts = 0;
        return false;
    }

    public long getRemainingLockSeconds() {
        if (!isLocked()) {
            return 0;
        }

        Duration remaining = Duration.between(Instant.now(clock), lockedUntil);
        return Math.max(1, remaining.getSeconds());
    }

    public void recordFailure() {
        if (isLocked()) {
            return;
        }

        failedAttempts++;

        if (failedAttempts >= maxAttempts) {
            lockedUntil = Instant.now(clock).plus(lockDuration);
        }
    }

    public void recordSuccess() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    public int getRemainingAttempts() {
        return Math.max(0, maxAttempts - failedAttempts);
    }
}
