package org.example.service;

/**
 * Represents the outcome status of an authentication attempt.
 * SUCCESS: credentials were valid and authentication succeeded.
 * BLANK_INPUT: username or password was blank or empty.
 * INVALID_CREDENTIALS: username not found or password incorrect.
 */
public enum LoginStatus {
    SUCCESS,
    BLANK_INPUT,
    INVALID_CREDENTIALS
}

