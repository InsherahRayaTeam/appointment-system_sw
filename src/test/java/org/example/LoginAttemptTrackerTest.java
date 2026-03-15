package org.example;

import org.example.service.LoginAttemptTracker;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginAttemptTrackerTest {

    @Test
    void locksAfterMaxFailures() {
        LoginAttemptTracker tracker = new LoginAttemptTracker(3, Duration.ofSeconds(60));

        tracker.recordFailure();
        tracker.recordFailure();
        assertFalse(tracker.isLocked());

        tracker.recordFailure();
        assertTrue(tracker.isLocked());
    }

    @Test
    void unlocksAfterDuration() throws InterruptedException {
        LoginAttemptTracker tracker = new LoginAttemptTracker(1, Duration.ofMillis(50));

        tracker.recordFailure();
        assertTrue(tracker.isLocked());

        Thread.sleep(80);
        assertFalse(tracker.isLocked());
    }

    @Test
    void successResetsFailureState() {
        LoginAttemptTracker tracker = new LoginAttemptTracker(3, Duration.ofSeconds(60));

        tracker.recordFailure();
        tracker.recordSuccess();

        assertFalse(tracker.isLocked());
        assertTrue(tracker.getRemainingAttempts() == 3);
    }
}
