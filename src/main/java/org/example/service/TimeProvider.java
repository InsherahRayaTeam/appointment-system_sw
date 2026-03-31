package org.example.service;

import java.time.LocalDateTime;

/**
 * Contract for providing the current time, allowing injection of mock time in tests.
 *
 * @author appointment-system
 * @version 1.0
 */
public interface TimeProvider {

    /**
     * Returns the current date and time.
     *
     * @return current LocalDateTime
     */
    LocalDateTime now();
}

