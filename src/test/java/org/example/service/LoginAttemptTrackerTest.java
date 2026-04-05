package org.example.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptTrackerTest {

    @Test
    void constructor_WithInvalidArguments_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new LoginAttemptTracker(0, Duration.ofSeconds(30)));
        assertThrows(IllegalArgumentException.class, () -> new LoginAttemptTracker(1, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new LoginAttemptTracker(1, Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> new LoginAttemptTracker(1, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoginAttemptTracker(1, Duration.ofSeconds(1), null)
        );
    }

    @Test
    void recordFailure_IncrementsFailedAttemptsAndReducesAttemptsRemaining() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        LoginAttemptTracker tracker = new LoginAttemptTracker(3, Duration.ofSeconds(30), clock);

        tracker.recordFailure();

        assertEquals(1, tracker.getFailedAttempts());
        assertEquals(2, tracker.getAttemptsRemaining());
        assertFalse(tracker.isLocked());
    }

    @Test
    void recordFailure_LocksAfterConfiguredAttempts() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        LoginAttemptTracker tracker = new LoginAttemptTracker(3, Duration.ofSeconds(30), clock);

        tracker.recordFailure();
        tracker.recordFailure();
        assertFalse(tracker.isLocked());

        tracker.recordFailure();

        assertTrue(tracker.isLocked());
        assertEquals(0, tracker.getFailedAttempts());
        assertEquals(3, tracker.getAttemptsRemaining());
        assertTrue(tracker.getRemainingLockSeconds() > 0);
    }

    @Test
    void lock_ExpiresAtBoundaryAndRemainingSecondsBecomesZero() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        LoginAttemptTracker tracker = new LoginAttemptTracker(1, Duration.ofSeconds(10), clock);

        tracker.recordFailure();
        assertTrue(tracker.isLocked());

        clock.advance(Duration.ofSeconds(10));

        assertFalse(tracker.isLocked());
        assertEquals(0, tracker.getRemainingLockSeconds());
    }

    @Test
    void recordFailure_WhileLocked_DoesNotChangeState() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        LoginAttemptTracker tracker = new LoginAttemptTracker(1, Duration.ofSeconds(10), clock);

        tracker.recordFailure();
        long remainingBefore = tracker.getRemainingLockSeconds();

        tracker.recordFailure();

        assertTrue(tracker.isLocked());
        assertTrue(tracker.getRemainingLockSeconds() <= remainingBefore);
        assertEquals(0, tracker.getFailedAttempts());
    }

    @Test
    void recordSuccess_ClearsLockAndFailures() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        LoginAttemptTracker tracker = new LoginAttemptTracker(1, Duration.ofSeconds(30), clock);

        tracker.recordFailure();
        assertTrue(tracker.isLocked());

        tracker.recordSuccess();

        assertFalse(tracker.isLocked());
        assertEquals(0, tracker.getFailedAttempts());
        assertEquals(1, tracker.getAttemptsRemaining());
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
