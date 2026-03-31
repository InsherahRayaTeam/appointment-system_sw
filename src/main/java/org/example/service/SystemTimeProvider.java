package org.example.service;

import java.time.LocalDateTime;

/**
 * Provides the actual system time.
 *
 * @author appointment-system
 * @version 1.0
 */
public class SystemTimeProvider implements TimeProvider {

    /**
     * Returns the current system time.
     *
     * @return current LocalDateTime
     */
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}

