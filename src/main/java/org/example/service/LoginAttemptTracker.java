package org.example.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class LoginAttemptTracker {

    private final int maxFailedAttempts;
    private final Duration lockDuration;
    private final Clock clock;

    private int failedAttempts;
    private Instant lockedUntil;

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

    public boolean isLocked() {
        return lockedUntil != null && Instant.now(clock).isBefore(lockedUntil);
    }

    public long getRemainingLockSeconds() {
        if (!isLocked()) {
            return 0;
        }
        return Duration.between(Instant.now(clock), lockedUntil).toSeconds() + 1;
    }

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

    public void recordSuccess() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }
}
