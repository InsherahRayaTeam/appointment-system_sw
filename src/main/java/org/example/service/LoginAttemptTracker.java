package org.example.service;

import java.time.Duration;
import java.time.Instant;

public class LoginAttemptTracker {

    private final int maxAttempts;
    private final Duration lockDuration;
    private int failedAttempts;
    private Instant lockedUntil;

    public LoginAttemptTracker(int maxAttempts, Duration lockDuration) {
        this.maxAttempts = maxAttempts;
        this.lockDuration = lockDuration;
    }

    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }

        if (Instant.now().isBefore(lockedUntil)) {
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

        return Math.max(1, Duration.between(Instant.now(), lockedUntil).toSeconds());
    }

    public int getRemainingAttempts() {
        return Math.max(0, maxAttempts - failedAttempts);
    }

    public void recordFailure() {
        if (isLocked()) {
            return;
        }

        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            lockedUntil = Instant.now().plus(lockDuration);
        }
    }

    public void recordSuccess() {
        failedAttempts = 0;
        lockedUntil = null;
    }
}
