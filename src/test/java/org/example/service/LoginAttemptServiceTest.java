package org.example.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    @Test
    void lockout_ActivatesAfterConfiguredFailures_AndExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        LoginAttemptService attemptService = new LoginAttemptService(3, Duration.ofSeconds(30), clock);

        assertFalse(attemptService.isLocked());

        attemptService.recordFailure();
        attemptService.recordFailure();
        assertFalse(attemptService.isLocked());

        attemptService.recordFailure();
        assertTrue(attemptService.isLocked());

        clock.plusSeconds(31);
        assertFalse(attemptService.isLocked());
    }

    @Test
    void success_ResetsFailedAttemptCount() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        LoginAttemptService attemptService = new LoginAttemptService(3, Duration.ofSeconds(30), clock);

        attemptService.recordFailure();
        assertTrue(attemptService.getRemainingAttempts() == 2);

        attemptService.recordSuccess();
        assertTrue(attemptService.getRemainingAttempts() == 3);
        assertFalse(attemptService.isLocked());
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
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
            return instant;
        }

        void plusSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }
    }
}
