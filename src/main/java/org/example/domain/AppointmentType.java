package org.example.domain;

/**
 * Represents the type of appointment for Sprint 5 preparation.
 *
 * @author appointment-system
 * @version 1.0
 */
public enum AppointmentType {
    /**
     * Urgent appointment requiring immediate attention.
     */
    URGENT,

    /**
     * Follow-up appointment from previous consultation.
     */
    FOLLOW_UP,

    /**
     * Assessment or diagnostic appointment.
     */
    ASSESSMENT,

    /**
     * Virtual/online appointment.
     */
    VIRTUAL,

    /**
     * In-person appointment.
     */
    IN_PERSON,

    /**
     * Individual appointment (single participant).
     */
    INDIVIDUAL,

    /**
     * Group appointment (multiple participants).
     */
    GROUP
}

