package org.example.service;

/**
 * Lists the possible values for sign up status.
 */
public enum SignUpStatus {
    SUCCESS,
    BLANK_USERNAME,
    BLANK_EMAIL,
    BLANK_PASSWORD,
    PASSWORD_MISMATCH,
    INVALID_EMAIL,
    WEAK_PASSWORD,
    USERNAME_ALREADY_EXISTS,
    EMAIL_ALREADY_EXISTS
}

