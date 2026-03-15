package org.example.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginAttemptTrackerTest {

    @Test
    void recordFailure_LocksAfterConfiguredAttempts() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        LoginAttemptTracker tracker = new LoginAttemptTracker(3, Duration.ofSeconds(30), clock);

        tracker.recordFailure();
        tracker.recordFailure();
        assertFalse(tracker.isLocked());

        tracker.recordFailure();

        assertTrue(tracker.isLocked());
        assertTrue(tracker.getRemainingLockSeconds() > 0);
    }

    @Test
    void lock_ExpiresAfterDuration() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        LoginAttemptTracker tracker = new LoginAttemptTracker(1, Duration.ofSeconds(10), clock);

        tracker.recordFailure();
        assertTrue(tracker.isLocked());

        clock.advance(Duration.ofSeconds(10));

        assertFalse(tracker.isLocked());
    }

    @Test
    void recordSuccess_ClearsLockAndFailures() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        LoginAttemptTracker tracker = new LoginAttemptTracker(1, Duration.ofSeconds(30), clock);

        tracker.recordFailure();
        assertTrue(tracker.isLocked());

        tracker.recordSuccess();

        assertFalse(tracker.isLocked());
        assertTrue(tracker.getFailedAttempts() == 0);
    }

    private static class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant initialInstant) {
            this.now = initialInstant;
        }

        void advance(Duration duration) {
            this.now = this.now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
