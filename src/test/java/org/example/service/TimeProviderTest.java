package org.example.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TimeProvider implementations.
 */
class TimeProviderTest {

    @Test
    void testSystemTimeProviderReturnsCurrentTime() {
        SystemTimeProvider provider = new SystemTimeProvider();
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime time = provider.now();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(time);
        assertTrue(time.isEqual(before) || time.isAfter(before));
        assertTrue(time.isEqual(after) || time.isBefore(after));
    }

    @Test
    void testSystemTimeProviderMultipleCalls() {
        SystemTimeProvider provider = new SystemTimeProvider();
        LocalDateTime time1 = provider.now();
        LocalDateTime time2 = provider.now();

        // Second call should be same or later than first
        assertTrue(time2.isEqual(time1) || time2.isAfter(time1));
    }
}

